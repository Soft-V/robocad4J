package io.github.softv.shufflecad;

import io.github.softv.internal.common.Robot;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHelper {
    // Камеры: список (имя;ширина:высота) идёт по TCP, а кадры — по UDP чанками.
    private static final int CAMERA_UDP_PORT = 63260;
    private static final int CAMERA_UDP_CHUNK = 1400;

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

    // Индекс выбранной клиентом камеры — её кадры уходят по UDP.
    private volatile int selectedCamera = 0;

    private final ICallback cameraVarsCallback = () -> {
        // Отдаём весь список камер каждый цикл — клиент всегда видит актуальный набор.
        List<String> segments = new ArrayList<>();
        for (CameraVariable c : this.shufflecad.cameraVariablesArray) {
            segments.add(String.format("%1$s;%2$s:%3$s", c.name, (int) c.shape.width, (int) c.shape.height));
        }
        cameraVariablesChannel.outString = segments.isEmpty() ? "null" : String.join("&", segments);

        try {
            selectedCamera = Integer.parseInt(cameraVariablesChannel.strFromClient);
        } catch (NumberFormatException ignored) { }
    };

    private final ICallback joyVarsCallback = () -> {
        String data = joyVariablesChannel.outString;
        if (data == null || data.isEmpty() || data.equals("null")) return;

        for (String item : data.split("&")) {
            String[] parts = item.split(";");
            if (parts.length != 2) continue;

            int val;
            try {
                val = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                continue;
            }

            switch (parts[0]) {
                case "A":
                    this.shufflecad.joystickData.BtnA = val == 1;
                    break;
                case "X":
                    this.shufflecad.joystickData.BtnX = val == 1;
                    break;
                case "Y":
                    this.shufflecad.joystickData.BtnY = val == 1;
                    break;
                case "B":
                    this.shufflecad.joystickData.BtnB = val == 1;
                    break;
                case "RightShoulder":
                    this.shufflecad.joystickData.RightShoulder = val == 1;
                    break;
                case "LeftShoulder":
                    this.shufflecad.joystickData.LeftShoulder = val == 1;
                    break;
                case "DPad_Up":
                    this.shufflecad.joystickData.DpudUp = val == 1;
                    break;
                case "DPad_Down":
                    this.shufflecad.joystickData.DpudDown = val == 1;
                    break;
                case "DPad_Right":
                    this.shufflecad.joystickData.DpudRight = val == 1;
                    break;
                case "DPad_Left":
                    this.shufflecad.joystickData.DpudLeft = val == 1;
                    break;
                case "LeftTrigger":
                    this.shufflecad.joystickData.LeftTrigger = (byte) val;
                    break;
                case "RightTrigger":
                    this.shufflecad.joystickData.RightTrigger = (byte) val;
                    break;
                case "LeftThumbstick_X":
                    this.shufflecad.joystickData.LeftStickX = val;
                    break;
                case "LeftThumbstick_Y":
                    this.shufflecad.joystickData.LeftStickY = val;
                    break;
                case "RightThumbstick_X":
                    this.shufflecad.joystickData.RightStickX = val;
                    break;
                case "RightThumbstick_Y":
                    this.shufflecad.joystickData.RightStickY = val;
                    break;
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
        // Камера-канал теперь отдаёт только метаданные (список камер), без картинок.
        cameraVariablesChannel = new TalkPort(robot, 63254, cameraVarsCallback, 30, false);
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
        startCameraUdp();
    }

    public void stop(){
        outVariablesChannel.stopTalking();
        inVariablesChannel.stopListening();
        chartVariablesChannel.stopTalking();
        outcadVariablesChannel.stopTalking();
        rpiVariablesChannel.stopTalking();
        cameraVariablesChannel.stopTalking();
        joyVariablesChannel.stopListening();
        stopCameraUdp();
    }

    private DatagramSocket cameraUdpSocket;
    private Thread cameraUdpThread;
    private volatile boolean stopCameraUdp = false;

    private void startCameraUdp() {
        try {
            cameraUdpSocket = new DatagramSocket();
        } catch (Exception e) {
            this.robot.writeLog("Shufflecad: failed to open camera UDP socket: " + e.getMessage());
            return;
        }
        stopCameraUdp = false;
        cameraUdpThread = new Thread(this::cameraUdpLoop);
        cameraUdpThread.setDaemon(true);
        cameraUdpThread.start();
    }

    private void stopCameraUdp() {
        stopCameraUdp = true;
        if (cameraUdpSocket != null) cameraUdpSocket.close();
    }

    // Кадр выбранной камеры режем на чанки и шлём по UDP: заголовок
    // (frameId:u32, cameraIndex:u16, chunkIndex:u16, chunkCount:u16) + данные.
    private void cameraUdpLoop() {
        long frameId = 0;
        while (!stopCameraUdp) {
            try {
                String target = cameraVariablesChannel.clientAddress;
                List<CameraVariable> cams = this.shufflecad.cameraVariablesArray;
                if (target != null && !cams.isEmpty()) {
                    int idx = selectedCamera;
                    if (idx < 0 || idx >= cams.size()) idx = 0;
                    CameraVariable cam = cams.get(idx);
                    if (cam.shape.width > 0 && cam.shape.height > 0) {
                        byte[] data = cam.getValue();
                        if (isJpeg(data)) {
                            sendFrameUdp(target, idx, data, frameId);
                            frameId = (frameId + 1) & 0xFFFFFFFFL;
                        }
                    }
                }
            } catch (Exception ignored) { }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private static boolean isJpeg(byte[] d) {
        return d != null && d.length >= 2 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8;
    }

    private void sendFrameUdp(String target, int cameraIndex, byte[] data, long frameId) {
        try {
            InetAddress addr = InetAddress.getByName(target);
            int total = (data.length + CAMERA_UDP_CHUNK - 1) / CAMERA_UDP_CHUNK;
            if (total < 1) total = 1;
            for (int i = 0; i < total; i++) {
                int off = i * CAMERA_UDP_CHUNK;
                int len = Math.min(CAMERA_UDP_CHUNK, data.length - off);
                byte[] pkt = new byte[10 + len];
                // заголовок little-endian
                pkt[0] = (byte) (frameId & 0xFF);
                pkt[1] = (byte) ((frameId >>> 8) & 0xFF);
                pkt[2] = (byte) ((frameId >>> 16) & 0xFF);
                pkt[3] = (byte) ((frameId >>> 24) & 0xFF);
                pkt[4] = (byte) (cameraIndex & 0xFF);
                pkt[5] = (byte) ((cameraIndex >>> 8) & 0xFF);
                pkt[6] = (byte) (i & 0xFF);
                pkt[7] = (byte) ((i >>> 8) & 0xFF);
                pkt[8] = (byte) (total & 0xFF);
                pkt[9] = (byte) ((total >>> 8) & 0xFF);
                System.arraycopy(data, off, pkt, 10, len);
                cameraUdpSocket.send(new DatagramPacket(pkt, pkt.length, addr, CAMERA_UDP_PORT));
            }
        } catch (Exception ignored) { }
    }
}
