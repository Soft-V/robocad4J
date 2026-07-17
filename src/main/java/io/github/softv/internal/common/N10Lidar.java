package io.github.softv.internal.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

// Port of robocad-py N10Lidar (n10_lidar.py). LDROBOT N10: header 0xA5 0x5A,
// 58-byte packet, big-endian fields, angles in hundredths of a degree.
//  [0..1] header, [5..6] start angle, [7..54] 16 points x 3 bytes
//  (2 distance + 1 intensity), [55..56] end angle.
//
// The port is opened as a plain device file, so this only works on Linux, and
// the baud rate is applied by shelling out to stty (there is no serial library
// on the classpath).
public class N10Lidar implements LidarBase {
    private static final int PKG_HEADER_0 = 0xA5;
    private static final int PKG_HEADER_1 = 0x5A;
    private static final int MIN_PAYLOAD = 58;
    private static final int POINT_PER_PACK = 16;
    private static final int DEFAULT_BAUD = 230400;

    // Drop the backlog if parsing falls this far behind the sensor.
    private static final int MAX_BUFFERED_PACKETS = 100;

    private final Robot robot;
    private final String port;
    private final int baud;

    private FileInputStream stream = null;
    private volatile boolean shutdown = false;
    private Thread scanThread = null;

    private final Object lock = new Object();
    private final int[] data = new int[360];

    private final byte[] buffer = new byte[MIN_PAYLOAD * MAX_BUFFERED_PACKETS];
    private int bufLen = 0;

    public N10Lidar(Robot robot, String port) {
        this(robot, port, DEFAULT_BAUD);
    }

    public N10Lidar(Robot robot, String port, int baud) {
        this.robot = robot;
        this.port = port;
        this.baud = baud;
    }

    @Override
    public void start() {
        try {
            configurePort();
            this.stream = new FileInputStream(this.port);
        } catch (Exception e) {
            this.robot.writeLog("LiDAR N10: failed to open " + this.port + ": " + e.getMessage());
            return;
        }

        this.shutdown = false;
        this.scanThread = new Thread(this::scanLoop);
        this.scanThread.setDaemon(true);
        this.scanThread.start();
    }

    @Override
    public void stop() {
        this.shutdown = true;
        // Close first: the scan thread is blocked in read() and only wakes up
        // once the stream dies.
        try { if (this.stream != null) this.stream.close(); } catch (IOException ignored) { }
        try { if (this.scanThread != null) this.scanThread.join(1000); } catch (InterruptedException ignored) { }
    }

    @Override
    public ArrayList<Integer> getData() {
        synchronized (this.lock) {
            ArrayList<Integer> out = new ArrayList<>(360);
            for (int i = 0; i < 360; i++) out.add(this.data[i]);
            return out;
        }
    }

    private void configurePort() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("stty", "-F", this.port,
                String.valueOf(this.baud), "raw", "-echo");
        pb.redirectErrorStream(true);
        int code = pb.start().waitFor();
        if (code != 0) {
            this.robot.writeLog("LiDAR N10: stty returned " + code + " for " + this.port);
        }
    }

    private void scanLoop() {
        byte[] buf = new byte[MIN_PAYLOAD * 16];
        while (!this.shutdown) {
            int n;
            try {
                n = this.stream.read(buf);
            } catch (IOException e) {
                break; // stream closed by stop(), or the device went away
            }
            if (n <= 0) continue;

            if (this.bufLen + n > this.buffer.length) this.bufLen = 0;
            System.arraycopy(buf, 0, this.buffer, this.bufLen, n);
            this.bufLen += n;

            parseBuffer();
        }
    }

    private void parseBuffer() {
        while (true) {
            int start = -1;
            for (int i = 0; i + 1 < this.bufLen; i++) {
                if ((this.buffer[i] & 0xFF) == PKG_HEADER_0 && (this.buffer[i + 1] & 0xFF) == PKG_HEADER_1) {
                    start = i;
                    break;
                }
            }

            if (start < 0) {
                // No header yet. Keep the last byte: it may be a header split across reads.
                if (this.bufLen > 1) {
                    this.buffer[0] = this.buffer[this.bufLen - 1];
                    this.bufLen = 1;
                }
                return;
            }

            if (start > 0) {
                System.arraycopy(this.buffer, start, this.buffer, 0, this.bufLen - start);
                this.bufLen -= start;
            }
            if (this.bufLen < MIN_PAYLOAD) return;

            decodePacket();

            System.arraycopy(this.buffer, MIN_PAYLOAD, this.buffer, 0, this.bufLen - MIN_PAYLOAD);
            this.bufLen -= MIN_PAYLOAD;
        }
    }

    private void decodePacket() {
        int startAngle = (this.buffer[5] & 0xFF) * 256 + (this.buffer[6] & 0xFF);
        int endAngle = (this.buffer[55] & 0xFF) * 256 + (this.buffer[56] & 0xFF);

        double step = ((endAngle + 36000 - startAngle) % 36000) / (double) (POINT_PER_PACK - 1) / 100.0;
        double startDeg = startAngle / 100.0;

        synchronized (this.lock) {
            for (int i = 0; i < POINT_PER_PACK; i++) {
                int o = 7 + i * 3;
                int dist = (this.buffer[o] & 0xFF) * 256 + (this.buffer[o + 1] & 0xFF);

                // Math.rint, not Math.round: it rounds half to even, matching
                // Python's round() in the reference decoder.
                int angle = (int) Math.rint(startDeg + step * i) % 360;
                if (angle < 0) angle += 360;

                this.data[angle] = dist;
            }
        }
    }
}
