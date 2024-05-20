package io.github.softv.shufflecad;

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
        for (ShuffleVariable v : InfoHolder.variablesArray) {
            if (!v.type.equals(ShuffleVariable.CHART_TYPE)){

            }
        }
    };

    private static final ICallback inVarsCallback = () -> {

    };

    private static final ICallback chartVarsCallback = () -> {

    };

    private static final ICallback outcadVarsCallback = () -> {

    };

    private static final ICallback rpiVarsCallback = () -> {

    };

    private static final ICallback cameraVarsCallback = () -> {

    };

    private static final ICallback joyVarsCallback = () -> {

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

    private static void stop(){
        outVariablesChannel.stopTalking();
        inVariablesChannel.stopListening();
        chartVariablesChannel.stopTalking();
        outcadVariablesChannel.stopTalking();
        rpiVariablesChannel.stopTalking();
        cameraVariablesChannel.stopTalking();
        joyVariablesChannel.stopListening();
    }
}
