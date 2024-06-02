package io.github.softv.shufflecad;

import io.github.softv.Common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHelper {
    private static TalkPort outVariablesChannel;
    private static ListenPort inVariablesChannel;
    private static TalkPort chartVariablesChannel;
    private static TalkPort outcadVariablesChannel;
    private static TalkPort rpiVariablesChannel;
    private static TalkPort cameraVariablesChannel;
    private static ListenPort joyVariablesChannel;

    private static final ICallback outVarsCallback = () -> {
        List<String> strings = new ArrayList<>();
        for (ShuffleVariable v : ShufflecadHolder.variablesArray) {
            if (!v.type.equals(ShuffleVariable.CHART_TYPE)){
                strings.add(String.format("%1$s;%2$s;%3$s;%4$s", v.name, v.getString(), v.type, v.direction));
            }
        }
        if (strings.size() > 0){
            outVariablesChannel.outString = String.join("&", strings);
        }
        else{
            outVariablesChannel.outString = "null";
        }
    };

    private static final ICallback inVarsCallback = () -> {
        if (inVariablesChannel.outString.length() > 0 && !inVariablesChannel.outString.equals("null")){
            String[] strings = inVariablesChannel.outString.split("&");
            for (String v : strings){
                String[] params = v.split(";");
                for (ShuffleVariable sv : ShufflecadHolder.variablesArray){
                    if (sv.name.equals(params[0])){
                        sv.setString(params[1]);
                        break;
                    }
                }
            }
        }
    };

    private static final ICallback chartVarsCallback = () -> {
        List<String> strings = new ArrayList<>();
        for (ShuffleVariable v : ShufflecadHolder.variablesArray) {
            if (v.type.equals(ShuffleVariable.CHART_TYPE)){
                strings.add(String.format("%1$s;%2$s", v.name, v.getString()));
            }
        }
        if (strings.size() > 0){
            chartVariablesChannel.outString = String.join("&", strings);
        }
        else{
            chartVariablesChannel.outString = "null";
        }
    };

    private static final ICallback outcadVarsCallback = () -> {
        if (ShufflecadHolder.getPrintArray().size() > 0){
            outcadVariablesChannel.outString = String.join("&", ShufflecadHolder.getPrintArray());
            ShufflecadHolder.clearPrintArray();
        }
        else{
            outcadVariablesChannel.outString = "null";
        }
    };

    private static final ICallback rpiVarsCallback = () -> {
        String[] outArray = { Float.toString(Common.temperature), Float.toString(Common.memoryLoad),
                Float.toString(Common.cpuLoad), Float.toString(Common.power), Float.toString(Common.spiTimeDev),
                Float.toString(Common.rxSpiTimeDev), Float.toString(Common.txSpiTimeDev),
                Float.toString(Common.spiCountDev), Float.toString(Common.comTimeDev),
                Float.toString(Common.rxComTimeDev), Float.toString(Common.txComTimeDev),
                Float.toString(Common.comCountDev)};
        rpiVariablesChannel.outString = String.join("&", outArray);
    };

    private static int cameraToggler = 0;

    private static final ICallback cameraVarsCallback = () -> {
        if (ShufflecadHolder.cameraVariablesArray.size() > 0){
            if (cameraVariablesChannel.strFromClient.equals("-1")){
                CameraVariable currVar = ShufflecadHolder.cameraVariablesArray.get(cameraToggler);
                cameraVariablesChannel.outString = String.format("%1$s;%2$s:%3$s", currVar.name, currVar.shape.width, currVar.shape.height);
                cameraVariablesChannel.outBytes = currVar.getValue();

                if (cameraToggler + 1 == ShufflecadHolder.cameraVariablesArray.size()){
                    cameraToggler = 0;
                }
                else{
                    cameraToggler++;
                }
            }
            else{
                CameraVariable currVar = ShufflecadHolder.cameraVariablesArray.get(Integer.parseInt(cameraVariablesChannel.strFromClient));
                cameraVariablesChannel.outString = String.format("%1$s;%2$s:%3$s", currVar.name, currVar.shape.width, currVar.shape.height);
                cameraVariablesChannel.outBytes = currVar.getValue();
            }
        }
        else{
            cameraVariablesChannel.outString = "null";
            cameraVariablesChannel.outBytes = "null".getBytes(StandardCharsets.UTF_8);
        }
    };

    private static final ICallback joyVarsCallback = () -> {
        if (joyVariablesChannel.outString.length() > 0 && !joyVariablesChannel.outString.equals("null")){
            String[] strings = joyVariablesChannel.outString.split("&");
            for (String v : strings){
                String[] params = v.split(";");
                if (ShufflecadHolder.joystickValues.containsKey(params[0])){
                    ShufflecadHolder.joystickValues.replace(params[0], Integer.parseInt(params[1]));
                }
                else{
                    ShufflecadHolder.joystickValues.put(params[0], Integer.parseInt(params[1]));
                }
            }
        }
    };

    public static void initAndStart(){
        outVariablesChannel = new TalkPort(63253, outVarsCallback, 4, false);
        inVariablesChannel = new ListenPort(63258, inVarsCallback, 4);
        chartVariablesChannel = new TalkPort(63255, chartVarsCallback, 2, false);
        outcadVariablesChannel = new TalkPort(63257, outcadVarsCallback, 100, false);
        rpiVariablesChannel = new TalkPort(63256, rpiVarsCallback, 500, false);
        cameraVariablesChannel = new TalkPort(63254, cameraVarsCallback, 30, true);
        joyVariablesChannel = new ListenPort(63259, joyVarsCallback, 4);

        start();
    }

    private static void start(){
        outVariablesChannel.startTalking();
        inVariablesChannel.startListening();
        chartVariablesChannel.startTalking();
        outcadVariablesChannel.startTalking();
        rpiVariablesChannel.startTalking();
        cameraVariablesChannel.startTalking();
        joyVariablesChannel.startListening();
    }

    public static void stop(){
        outVariablesChannel.stopTalking();
        inVariablesChannel.stopListening();
        chartVariablesChannel.stopTalking();
        outcadVariablesChannel.stopTalking();
        rpiVariablesChannel.stopTalking();
        cameraVariablesChannel.stopTalking();
        joyVariablesChannel.stopListening();
    }
}
