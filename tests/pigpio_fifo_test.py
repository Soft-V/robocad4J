"""Linux integration tests with real temporary FIFOs; no robot/pigpiod needed.

Usage: python3 tests/pigpio_fifo_test.py PROBE [PROBE_ARGUMENTS ...]
The probe receives the temporary command and response FIFO paths last.
"""
import os
from pathlib import Path
import select
import subprocess
import sys
import tempfile
import threading
import time
import unittest

PROBE = sys.argv[1:]
sys.argv[1:] = []
GPIOS = sorted([4, 18, 17, 27, 23, 22, 24, 25, 7, 5])


class Daemon:
    def __init__(self, directory):
        self.input = str(Path(directory) / 'pigpio')
        self.output = str(Path(directory) / 'pigout')
        os.mkfifo(self.input)
        os.mkfifo(self.output)
        self.input_fd = os.open(self.input, os.O_RDWR | os.O_NONBLOCK)
        self.output_fd = os.open(self.output, os.O_RDWR | os.O_NONBLOCK)
        self.commands = []
        self.frequencies = {}
        self.ranges = {}
        self.reply = self.normal_reply
        self.done = threading.Event()
        self.thread = threading.Thread(target=self.run, daemon=True)
        self.thread.start()

    def normal_reply(self, command):
        fields = command.split()
        gpio = int(fields[1])
        if fields[0] == 'PFS':
            self.frequencies[gpio] = int(fields[2])
            return fields[2] + '\n'
        if fields[0] == 'PRS':
            self.ranges[gpio] = int(fields[2])
            return '250\n'
        if fields[0] == 'PFG':
            return str(self.frequencies.get(gpio, 800)) + '\n'
        if fields[0] == 'PRG':
            return str(self.ranges.get(gpio, 255)) + '\n'
        return '0\n'

    def run(self):
        pending = b''
        while not self.done.is_set():
            if not select.select([self.input_fd], [], [], 0.02)[0]:
                continue
            pending += os.read(self.input_fd, 4096)
            while b'\n' in pending:
                line, pending = pending.split(b'\n', 1)
                command = line.decode('ascii')
                self.commands.append(command)
                response = self.reply(command)
                if response is not None:
                    # Fragment replies to exercise assembling complete lines.
                    for byte in response.encode('ascii'):
                        os.write(self.output_fd, bytes([byte]))

    def close(self):
        self.done.set()
        self.thread.join(timeout=2)
        os.close(self.input_fd)
        os.close(self.output_fd)


class FifoContract(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory(prefix='robocad-pigpio-')
        self.daemon = Daemon(self.directory.name)
        self.process = subprocess.Popen(PROBE + [self.daemon.input, self.daemon.output],
            stdin=subprocess.PIPE, stdout=subprocess.PIPE, text=True, bufsize=1)

    def tearDown(self):
        self.process.stdin.close()
        try:
            self.process.wait(timeout=3)
        except subprocess.TimeoutExpired:
            self.process.kill()
            self.process.wait()
        self.process.stdout.close()
        self.daemon.close()
        self.directory.cleanup()

    def call(self, text, error=False):
        self.process.stdin.write(text + '\n')
        self.process.stdin.flush()
        self.assertTrue(select.select([self.process.stdout], [], [], 4)[0], 'Probe hung: ' + text)
        response = self.process.stdout.readline().strip()
        if error:
            self.assertTrue(response.startswith('error:'), response)
        else:
            self.assertEqual('ok', response)
        return response

    def test_pwm_disable_and_all_outputs_stop(self):
        self.call('set 4 0.15')
        self.call('set 4 0.1')
        self.call('set 4 0')
        self.call('set 4 0.12')
        self.call('set 7 0.2')
        self.call('set 5 0.2')
        self.assertEqual(self.daemon.commands[:8],
            ['P 4 0', 'PFS 4 100', 'PRS 4 10000', 'P 4 1500', 'PFG 4', 'PRG 4', 'P 4 1000', 'W 4 0'])
        self.assertEqual(2, self.daemon.commands.count('PFS 4 100'))
        self.call('stop')
        self.assertEqual(['W %s 0' % pin for pin in GPIOS], self.daemon.commands[-10:])
        before = len(self.daemon.commands)
        self.call('set 4 0.1', error=True)
        self.assertEqual(before, len(self.daemon.commands))
        self.call('set 4 0')
        self.call('stop')

    def test_disable_never_configures_pwm(self):
        self.call('stop')
        self.assertEqual(['W %s 0' % pin for pin in GPIOS], self.daemon.commands)

    def test_invalid_values_do_not_touch_fifo(self):
        for command in ('set -1 0.1', 'set 32 0.1', 'set 4 -0.1', 'set 4 1.1', 'set 4 NaN'):
            self.call(command, error=True)
        self.assertEqual([], self.daemon.commands)

    def test_daemon_restart_reconfigures_pwm(self):
        self.call('set 4 0.15')
        self.daemon.close()
        Path(self.daemon.input).unlink()
        Path(self.daemon.output).unlink()
        self.daemon = Daemon(self.directory.name)
        self.call('set 4 0.12')
        self.assertEqual(['P 4 0', 'PFS 4 100', 'PRS 4 10000', 'P 4 1200'], self.daemon.commands[-4:])

    def test_negative_malformed_and_mismatched_frequency(self):
        for response in ('-5\n', 'garbage\n', '80\n'):
            self.daemon.reply = lambda cmd, r=response: r if cmd.startswith('PFS ') else '0\n'
            self.call('set 4 0.15', error=True)
        self.daemon.reply = self.daemon.normal_reply
        self.call('set 4 0.15')
        self.assertEqual(4, self.daemon.commands.count('PFS 4 100'))

    def test_restart_with_existing_fifos_reconfigures(self):
        self.call('set 4 0.15')
        self.daemon.frequencies.clear()
        self.daemon.ranges.clear()
        self.call('set 4 0.12')
        self.assertEqual(['P 4 0', 'PFS 4 100', 'PRS 4 10000', 'P 4 1200'], self.daemon.commands[-4:])

    def test_lost_reply_still_tracks_output_for_stop(self):
        self.daemon.reply = lambda cmd: None if cmd == 'P 4 1500' else self.daemon.normal_reply(cmd)
        start = time.monotonic()
        self.call('set 4 0.15', error=True)
        self.assertLess(time.monotonic() - start, 2)
        self.daemon.reply = self.daemon.normal_reply
        self.call('stop-used')
        self.assertEqual('W 4 0', self.daemon.commands[-1])

    def test_stop_failure_attempts_every_pin_and_can_retry(self):
        self.daemon.reply = lambda cmd: '-5\n' if cmd == 'W 7 0' else '0\n'
        self.call('stop', error=True)
        self.assertEqual(['W %s 0' % pin for pin in GPIOS], self.daemon.commands)
        self.daemon.reply = self.daemon.normal_reply
        self.call('stop')
        self.assertEqual(20, len(self.daemon.commands))

    def test_stale_replies_are_drained(self):
        os.write(self.daemon.output_fd, b'-5\n')
        self.call('set 4 0.15')

    def test_missing_and_regular_paths_are_not_created_or_overwritten(self):
        path = Path(self.daemon.input)
        path.unlink()
        self.call('set 4 0.15', error=True)
        self.assertFalse(path.exists())
        path.write_text('keep me')
        self.call('set 4 0.15', error=True)
        self.assertEqual('keep me', path.read_text())


if __name__ == '__main__':
    if not PROBE:
        raise SystemExit('Pass the probe executable and its arguments')
    unittest.main(verbosity=2)
