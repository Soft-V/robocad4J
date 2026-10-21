package io.github.softv.internal.studica;

import io.github.softv.internal.common.RobotConfiguration;

public class DefaultStudicaConfiguration extends RobotConfiguration {
    // pigpiod is managed by the OS, with its FIFO interface enabled.
    public String pigpioPipePath = "/dev/pigpio";
    public String pigpioResponsePath = "/dev/pigout";
    public int hcdioPwmFrequency = 100;
    public double pigpioTimeout = 1.0;
    public String titanPort = "/dev/ttyACM0";
    public int titanBaud = 115200;
    public String vmxPort = "/dev/spidev1.2";
    public int vmxChannel = 2;
    public int vmxSpeed = 1000000;
    public int vmxMode = 0;

    public DefaultStudicaConfiguration() {

    }
}
