package io.github.softv.shufflecad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShufflecadHolder {
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
