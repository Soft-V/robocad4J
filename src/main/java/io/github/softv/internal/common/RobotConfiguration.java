package io.github.softv.internal.common;

public class RobotConfiguration {
    public int cameraIndex = 0;
    public String libHolderFirstPath = "/home/pi";
    public boolean withPiBlaster = true;
    public LidarTypes lidarType = LidarTypes.N10_LIDAR;
    public String lidarPort = "/dev/ttyUSB0";

    public String simLogPath = "./robocad.log";
    public String realLogPath = "/var/tmp/robocad.log";
}

