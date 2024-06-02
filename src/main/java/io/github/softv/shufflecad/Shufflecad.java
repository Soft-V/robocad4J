package io.github.softv.shufflecad;

import io.github.softv.Common;
import io.github.softv.internal.LoggerInside;

public class Shufflecad {
    public static void start(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Common.logger.writeMainLog("Program stopped");
                Common.logger.writeMainLog("Signal handler called with signal (IDK it's Java)");
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
            ShufflecadHolder.cameraVariablesArray.add((CameraVariable) variable);
        }
        else{
            ShufflecadHolder.variablesArray.add((ShuffleVariable) variable);
        }
        return variable;
    }
}
