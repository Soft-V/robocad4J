package io.github.crackanddie.shufflecad;

import io.github.crackanddie.common.LoggerInside;

public class Shufflecad {
    public static void start(){
        InfoHolder.logger = new LoggerInside();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                InfoHolder.logger.writeMainLog("Program stopped");
                InfoHolder.logger.writeMainLog("Signal handler called with signal (IDK it's Java)");
                ConnectionHelper.stop();
                // maybe exception should be thrown ?
            }
        });
        ConnectionHelper.initAndStart();
    }

    public static void stop(){
        ConnectionHelper.stop();
    }

    public static IVariable addVar(IVariable variable){
        if (variable instanceof CameraVariable){
            InfoHolder.cameraVariablesArray.add((CameraVariable) variable);
        }
        else{
            InfoHolder.variablesArray.add((ShuffleVariable) variable);
        }
        return variable;
    }
}
