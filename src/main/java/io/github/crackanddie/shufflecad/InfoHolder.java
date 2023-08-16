package io.github.crackanddie.shufflecad;

import io.github.crackanddie.common.LoggerInside;

public class InfoHolder {
    public static LoggerInside logger = null;
    public static boolean onRealRobot = true;

    public static String power = "0";

    // some things
    public static String spiTimeDev = "0";
    public static String rxSpiTimeDev = "0";
    public static String txSpiTimeDev = "0";
    public static String spiCountDev = "0";
    public static String comTimeDev = "0";
    public static String rxComTimeDev = "0";
    public static String txComTimeDev = "0";
    public static String comCountDev = "0";
    public static String temperature = "0";
    public static String memoryLoad = "0";
    public static String cpuLoad = "0";
}
