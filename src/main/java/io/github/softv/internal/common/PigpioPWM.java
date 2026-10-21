package io.github.softv.internal.common;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/** PWM through pigpiod's text FIFOs; one controlling application at a time. */
public final class PigpioPWM {
    private static final int PWM_RANGE = 10000;
    private final String pipePath, responsePath;
    private final int frequency;
    private final double timeout;
    private final Set<Integer> configured = new HashSet<>();
    private final Set<Integer> used = new HashSet<>();
    private String pipeIdentity;
    private boolean stopped;

    public PigpioPWM(String pipePath, String responsePath, int frequency, double timeout) {
        if (frequency <= 0) throw new IllegalArgumentException("PWM frequency must be positive");
        if (!(timeout > 0 && timeout <= Integer.MAX_VALUE / 1000.0))
            throw new IllegalArgumentException("pigpio timeout must be positive and fit in milliseconds");
        this.pipePath = pipePath;
        this.responsePath = responsePath;
        this.frequency = frequency;
        this.timeout = timeout;
    }

    public PigpioPWM() { this("/dev/pigpio", "/dev/pigout", 100, 1.0); }

    // Loaded lazily: constructing/using a simulated robot never loads libc.
    private interface LibC extends Library {
        LibC INSTANCE = Native.load("c", LibC.class);
        int open(String path, int flags);
        int close(int fd);
        NativeLong read(int fd, byte[] data, NativeLong size);
        NativeLong write(int fd, byte[] data, NativeLong size);
        int poll(Pointer fds, NativeLong count, int timeout);
        int statx(int fd, String path, int flags, int mask, Pointer info);
    }

    private static IllegalStateException nativeError(String operation) {
        return new IllegalStateException(operation + ": errno " + Native.getLastError() +
                ". Start pigpiod with FIFO enabled and check permissions.");
    }

    private static final class Fifo implements AutoCloseable {
        final int fd;
        final String identity;

        Fifo(String path, int mode) {
            // No O_CREAT/O_TRUNC: a missing daemon must never create a file.
            fd = LibC.INSTANCE.open(path, mode | 0x800 | 0x80000); // NONBLOCK | CLOEXEC
            if (fd < 0) throw nativeError("Cannot open " + path);
            try { identity = readIdentity(); }
            catch (RuntimeException | Error error) {
                LibC.INSTANCE.close(fd);
                throw error;
            }
        }

        String readIdentity() {
            try (Memory info = new Memory(256)) {
                // statx has a fixed 256-byte ABI on Linux ARM32/ARM64/x86_64.
                if (LibC.INSTANCE.statx(fd, "", 0x1000, 0x101, info) != 0) // EMPTY_PATH, TYPE | INO
                    throw nativeError("Cannot inspect pigpio FIFO");
                if ((info.getInt(0) & 0x101) != 0x101 || (info.getShort(28) & 0xF000) != 0x1000)
                    throw new IllegalStateException("Not a pigpio FIFO");
                return info.getInt(136) + ":" + info.getInt(140) + ":" + info.getLong(32);
            }
        }

        @Override public void close() { LibC.INSTANCE.close(fd); }
    }

    private int command(int commandFd, int responseFd, String command) {
        byte[] data = (command + "\n").getBytes(StandardCharsets.US_ASCII);
        long written;
        do { written = LibC.INSTANCE.write(commandFd, data, new NativeLong(data.length)).longValue(); }
        while (written < 0 && Native.getLastError() == 4); // EINTR
        if (written != data.length) throw nativeError("Cannot write pigpio command " + command);
        long start = System.nanoTime();
        StringBuilder response = new StringBuilder();
        byte[] buffer = new byte[64];
        try (Memory pollFd = new Memory(8)) {
            while (response.indexOf("\n") < 0) {
                double remaining = timeout - (System.nanoTime() - start) / 1_000_000_000.0;
                if (remaining <= 0) throw new IllegalStateException("Timed out waiting for pigpiod: " + command);
                pollFd.setInt(0, responseFd);
                pollFd.setShort(4, (short)1); // POLLIN
                pollFd.setShort(6, (short)0);
                int ready = LibC.INSTANCE.poll(pollFd, new NativeLong(1), (int)Math.ceil(remaining * 1000));
                if (ready < 0 && Native.getLastError() == 4) continue;
                if (ready < 0) throw nativeError("Cannot poll pigpio response FIFO");
                if (ready == 0) continue;
                long count = LibC.INSTANCE.read(responseFd, buffer, new NativeLong(buffer.length)).longValue();
                int error = Native.getLastError();
                if (count < 0 && (error == 4 || error == 11)) continue;
                if (count <= 0) throw new IllegalStateException("pigpiod closed its response FIFO");
                response.append(new String(buffer, 0, (int)count, StandardCharsets.US_ASCII));
                if (response.length() > 64) throw new IllegalStateException("Oversized pigpio response");
            }
        }
        final int result;
        try { result = Integer.parseInt(response.toString().trim()); }
        catch (NumberFormatException error) { throw new IllegalStateException("Invalid pigpio response: " + response, error); }
        if (result < 0) throw new IllegalStateException("pigpio command '" + command + "' failed with error " + result);
        return result;
    }

    public synchronized void setDutycycle(int gpio, double duty) {
        if (gpio < 0 || gpio > 31) throw new IllegalArgumentException("pigpio GPIO must be from 0 to 31");
        if (!(duty >= 0 && duty <= 1)) throw new IllegalArgumentException("PWM duty cycle must be between 0 and 1");
        if (stopped && duty != 0) throw new IllegalStateException("PWM controller has been stopped");
        if (!Platform.isLinux()) throw new UnsupportedOperationException("pigpio FIFO control requires Linux");
        try (Fifo input = new Fifo(pipePath, 1); Fifo output = new Fifo(responsePath, 0)) {
            if (!input.identity.equals(pipeIdentity)) {
                configured.clear();
                pipeIdentity = input.identity;
            }
            byte[] stale = new byte[4096];
            long start = System.nanoTime();
            while (true) {
                long count = LibC.INSTANCE.read(output.fd, stale, new NativeLong(stale.length)).longValue();
                int error = Native.getLastError();
                if (count == 0 || (count < 0 && error == 11)) break;
                if (count < 0 && error != 4) throw nativeError("Cannot drain pigpio response FIFO");
                if ((System.nanoTime() - start) / 1_000_000_000.0 >= timeout)
                    throw new IllegalStateException("pigpio response FIFO is busy; use one controlling application");
            }
            if (duty == 0) {
                command(input.fd, output.fd, "W " + gpio + " 0");
                configured.remove(gpio);
                used.remove(gpio);
                return;
            }
            // A restarted daemon can reuse the same FIFO inode.
            if (configured.contains(gpio) &&
                    (command(input.fd, output.fd, "PFG " + gpio) != frequency ||
                     command(input.fd, output.fd, "PRG " + gpio) != PWM_RANGE))
                configured.remove(gpio);
            if (!configured.contains(gpio)) {
                command(input.fd, output.fd, "P " + gpio + " 0");
                int actual = command(input.fd, output.fd, "PFS " + gpio + " " + frequency);
                if (actual != frequency)
                    throw new IllegalStateException("pigpio selected " + actual + " Hz instead of " + frequency +
                            "; check sample rate (100 Hz works with -s 5)");
                command(input.fd, output.fd, "PRS " + gpio + " " + PWM_RANGE);
                configured.add(gpio);
            }
            used.add(gpio); // A lost reply does not mean PWM was not applied.
            command(input.fd, output.fd, "P " + gpio + " " + (int)Math.rint(duty * PWM_RANGE));
        } catch (RuntimeException | Error error) {
            configured.clear();
            throw error;
        }
    }

    /** Removes pulses; releasing torque depends on the servo electronics. */
    public synchronized void stop(int... gpios) {
        stopped = true;
        Set<Integer> targets = new TreeSet<>(used);
        for (int gpio : gpios) targets.add(gpio);
        IllegalStateException errors = new IllegalStateException("Failed to disable HCDIO PWM");
        for (int gpio : targets) {
            try { setDutycycle(gpio, 0); }
            catch (RuntimeException error) { errors.addSuppressed(error); }
        }
        if (errors.getSuppressed().length != 0) throw errors;
    }
}
