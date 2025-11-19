package io.github.softv.shufflecad;

import io.github.softv.internal.common.Robot;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHelper {
    private TalkPort outVariablesChannel;
    private ListenPort inVariablesChannel;
    private TalkPort chartVariablesChannel;
    private TalkPort outcadVariablesChannel;
    private TalkPort rpiVariablesChannel;
    private TalkPort cameraVariablesChannel;
    private ListenPort joyVariablesChannel;

    private Robot robot;
    private Shufflecad shufflecad;

    private final ICallback outVarsCallback = () -> {
        List<String> strings = new ArrayList<>();
        for (ShuffleVariable v : this.shufflecad.variablesArray) {
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

    private final ICallback inVarsCallback = () -> {
        if (inVariablesChannel.outString.length() > 0 && !inVariablesChannel.outString.equals("null")){
            String[] strings = inVariablesChannel.outString.split("&");
            for (String v : strings){
                String[] params = v.split(";");
                for (ShuffleVariable sv : this.shufflecad.variablesArray){
                    if (sv.name.equals(params[0])){
                        sv.setString(params[1]);
                        break;
                    }
                }
            }
        }
    };

    private final ICallback chartVarsCallback = () -> {
        List<String> strings = new ArrayList<>();
        for (ShuffleVariable v : this.shufflecad.variablesArray) {
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

    private final ICallback outcadVarsCallback = () -> {
        if (this.shufflecad.getPrintArray().size() > 0){
            outcadVariablesChannel.outString = String.join("&", this.shufflecad.getPrintArray());
            this.shufflecad.clearPrintArray();
        }
        else{
            outcadVariablesChannel.outString = "null";
        }
    };

    private final ICallback rpiVarsCallback = () -> {
        String[] outArray = { Float.toString(this.robot.robotInfo.temperature), Float.toString(this.robot.robotInfo.memoryLoad),
                Float.toString(this.robot.robotInfo.cpuLoad), Float.toString(this.robot.power), Float.toString(this.robot.robotInfo.spiTimeDev),
                Float.toString(this.robot.robotInfo.rxSpiTimeDev), Float.toString(this.robot.robotInfo.txSpiTimeDev),
                Float.toString(this.robot.robotInfo.spiCountDev), Float.toString(this.robot.robotInfo.comTimeDev),
                Float.toString(this.robot.robotInfo.rxComTimeDev), Float.toString(this.robot.robotInfo.txComTimeDev),
                Float.toString(this.robot.robotInfo.comCountDev)};
        rpiVariablesChannel.outString = String.join("&", outArray);
    };

    private int cameraToggler = 0;

    private final ICallback cameraVarsCallback = () -> {
        if (this.shufflecad.cameraVariablesArray.size() > 0){
            if (cameraVariablesChannel.strFromClient.equals("-1")){
                CameraVariable currVar = this.shufflecad.cameraVariablesArray.get(cameraToggler);
                cameraVariablesChannel.outString = String.format("%1$s;%2$s:%3$s", currVar.name, currVar.shape.width, currVar.shape.height);
                cameraVariablesChannel.outBytes = currVar.getValue();

                if (cameraToggler + 1 == this.shufflecad.cameraVariablesArray.size()){
                    cameraToggler = 0;
                }
                else{
                    cameraToggler++;
                }
            }
            else{
                CameraVariable currVar = this.shufflecad.cameraVariablesArray.get(Integer.parseInt(cameraVariablesChannel.strFromClient));
                cameraVariablesChannel.outString = String.format("%1$s;%2$s:%3$s", currVar.name, currVar.shape.width, currVar.shape.height);
                cameraVariablesChannel.outBytes = currVar.getValue();
            }
        }
        else{
            cameraVariablesChannel.outString = "null";
            cameraVariablesChannel.outBytes = "null".getBytes(StandardCharsets.UTF_8);
        }
    };

    private final ICallback joyVarsCallback = () -> {
        if (joyVariablesChannel.outString.length() > 0 && !joyVariablesChannel.outString.equals("null")){
            String[] strings = joyVariablesChannel.outString.split("&");
            for (String v : strings){
                String[] params = v.split(";");
                if (this.shufflecad.joystickValues.containsKey(params[0])){
                    this.shufflecad.joystickValues.replace(params[0], Integer.parseInt(params[1]));
                }
                else{
                    this.shufflecad.joystickValues.put(params[0], Integer.parseInt(params[1]));
                }
            }
        }
    };

    public ConnectionHelper(Shufflecad shufflecad, Robot robot){
        this.robot = robot;
        this.shufflecad = shufflecad;

        outVariablesChannel = new TalkPort(robot, 63253, outVarsCallback, 4, false);
        inVariablesChannel = new ListenPort(robot, 63258, inVarsCallback, 4);
        chartVariablesChannel = new TalkPort(robot, 63255, chartVarsCallback, 2, false);
        outcadVariablesChannel = new TalkPort(robot, 63257, outcadVarsCallback, 100, false);
        rpiVariablesChannel = new TalkPort(robot, 63256, rpiVarsCallback, 500, false);
        cameraVariablesChannel = new TalkPort(robot, 63254, cameraVarsCallback, 30, true);
        joyVariablesChannel = new ListenPort(robot, 63259, joyVarsCallback, 4);

        start();
    }

    private void start(){
        outVariablesChannel.startTalking();
        inVariablesChannel.startListening();
        chartVariablesChannel.startTalking();
        outcadVariablesChannel.startTalking();
        rpiVariablesChannel.startTalking();
        cameraVariablesChannel.startTalking();
        joyVariablesChannel.startListening();
    }

    public void stop(){
        outVariablesChannel.stopTalking();
        inVariablesChannel.stopListening();
        chartVariablesChannel.stopTalking();
        outcadVariablesChannel.stopTalking();
        rpiVariablesChannel.stopTalking();
        cameraVariablesChannel.stopTalking();
        joyVariablesChannel.stopListening();
    }
}
