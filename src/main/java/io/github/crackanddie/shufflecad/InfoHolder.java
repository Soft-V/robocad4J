package io.github.crackanddie.shufflecad;

import io.github.crackanddie.common.LoggerInside;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<ShuffleVariable> variablesArray = new ArrayList<>();
    public static List<CameraVariable> cameraVariablesArray = new ArrayList<>();
    public static Map<String, Integer> joystickValues = new HashMap<String, Integer>();
    public static List<String> printArray = new ArrayList<>();

    public static void printToLog(String var){
        printArray.add(var + "#e0d4ab");
    }

    public static List<String> getPrintArray(){
        return printArray;
    }

    public static void clearPrintArray(){
        printArray.clear();
    }
}
