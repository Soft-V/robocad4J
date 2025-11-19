package io.github.softv.internal.algaritm;

import io.github.softv.internal.common.RobotConfiguration;

public class DefaultAlgaritmConfiguration extends RobotConfiguration {
    public String titanPort = "/dev/ttyACM0";
    public int titanBaud = 115200;
    public String vmxPort = "/dev/spidev0.0";
    public int vmxChannel = 0;
    public int vmxSpeed = 1000000;
    public int vmxMode = 0;

    public DefaultAlgaritmConfiguration() {
        cameraIndex = 2;
        withPiBlaster = false;
    }
}
