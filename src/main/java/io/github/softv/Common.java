package io.github.softv;

import io.github.softv.internal.LoggerInside;

public class Common {
    public static LoggerInside logger = null;
    public static boolean onRealRobot = true;

    public static float power = 0.0f;

    // some things
    public static float spiTimeDev = 0.0f;
    public static float rxSpiTimeDev = 0.0f;
    public static float txSpiTimeDev = 0.0f;
    public static float spiCountDev = 0.0f;
    public static float comTimeDev = 0.0f;
    public static float rxComTimeDev = 0.0f;
    public static float txComTimeDev = 0.0f;
    public static float comCountDev = 0.0f;
    public static float temperature = 0.0f;
    public static float memoryLoad = 0.0f;
    public static float cpuLoad = 0.0f;

    public static final short LOG_ALL = 0;
    public static final short LOG_EXC_INFO = 1;
    public static final short LOG_EXC_WARN = 2;
    public static final short LOG_NOTHING = 3;

    public static short LOG_LEVEL = LOG_EXC_WARN;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
}
