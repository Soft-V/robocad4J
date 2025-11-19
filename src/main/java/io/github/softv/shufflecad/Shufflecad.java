package io.github.softv.shufflecad;

import io.github.softv.internal.common.Robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shufflecad {
    private final ConnectionHelper connectionHelper;

    public List<ShuffleVariable> variablesArray = new ArrayList<>();
    public List<CameraVariable> cameraVariablesArray = new ArrayList<>();
    public Map<String, Integer> joystickValues = new HashMap<String, Integer>();
    public List<String> printArray = new ArrayList<>();

    public Shufflecad(Robot robot) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            robot.writeLog("Program stopped");
            robot.writeLog("Signal handler called with signal (IDK it's Java)");
            this.stop();
            // maybe exception should be thrown ?
        }));
        connectionHelper = new ConnectionHelper(this, robot);
    }

    public void stop(){
        connectionHelper.stop();
    }

    public IVariable addVar(IVariable variable){
        if (variable instanceof CameraVariable){
            cameraVariablesArray.add((CameraVariable) variable);
        }
        else{
            variablesArray.add((ShuffleVariable) variable);
        }
        return variable;
    }

    public void printToLog(String var, String color){
        printArray.add(var + color);
    }

    public void printToLog(String var){
        printToLog(var, "#cccccc");
    }

    public List<String> getPrintArray(){
        return printArray;
    }

    public void clearPrintArray(){
        printArray.clear();
    }
}
