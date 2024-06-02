package io.github.softv.internal.studica;

import io.github.softv.Common;
import io.github.softv.internal.studica.shared.TitanStatic;
import io.github.softv.internal.studica.shared.VmxStatic;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConnectionSim extends  ConnectionBase
{
    public static final int MAX_DATA_RECEIVE = 27;
    public static final int MAX_DATA_TRANSMIT = 14;

    private final int PORT_SET_DATA = 65431;
    private final int PORT_GET_DATA = 65432;
    private final int PORT_CAMERA = 65438;

    private TalkPort talkChannel;
    private ListenPort listenChannel;
    private ListenPort cameraChannel;

    private Thread updateThread = null;
    private boolean stopUpdateThread = false;

    public ConnectionSim()
    {
    }

    @Override
    public void start()
    {
        this.talkChannel = new TalkPort(PORT_SET_DATA);
        this.listenChannel = new ListenPort(PORT_GET_DATA);
        this.cameraChannel = new ListenPort(PORT_CAMERA);

        this.talkChannel.startTalking();
        this.listenChannel.startListening();
        this.cameraChannel.startListening();

        this.stopUpdateThread = false;
        this.updateThread = new Thread(this::update);
        this.updateThread.setDaemon(true);
        this.updateThread.start();

        // todo: update power
        Common.power = 12;
    }

    @Override
    public void stop()
    {
        this.stopUpdateThread = true;
        if (updateThread != null){
            try { updateThread.join(); }
            catch (InterruptedException e){}
        }

        this.talkChannel.stopTalking();
        this.listenChannel.stopListening();
        this.cameraChannel.stopListening();
    }

    private void setData(List<Float> lst)
    {
        ByteBuffer bb = ByteBuffer.allocate(lst.size() * 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (Float i: lst) {
            bb.putFloat(i);
        }
        this.talkChannel.outBytes = bb.array();
    }

    @Override
    public Mat getCamera()
    {
        byte[] data = this.cameraChannel.outBytes;
        if (data.length == 921600)
        {
            Mat newMat = new Mat(480, 640, CvType.CV_8UC3);
            newMat.put(0, 0, data);
            return newMat;
        }
        return null;
    }

    private void update(){
        while (!stopUpdateThread){
            List<Float> lst = new ArrayList<>(Arrays.asList(TitanStatic.speedMotor0, TitanStatic.speedMotor1,
                    TitanStatic.speedMotor2, TitanStatic.speedMotor3));
            lst.addAll(Arrays.asList(VmxStatic.hcdioValues));
            this.setData(lst);

            byte[] values = this.listenChannel.outBytes.clone();
            if (values.length == 52){
                ByteBuffer bb = ByteBuffer.wrap(values);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                TitanStatic.encMotor0 = bb.getInt();
                TitanStatic.encMotor1 = bb.getInt();
                TitanStatic.encMotor2 = bb.getInt();
                TitanStatic.encMotor3 = bb.getInt();
                VmxStatic.ultrasound1 = bb.getFloat();
                VmxStatic.ultrasound2 = bb.getFloat();
                VmxStatic.analog1 = bb.getShort();
                VmxStatic.analog2 = bb.getShort();
                VmxStatic.analog3 = bb.getShort();
                VmxStatic.analog4 = bb.getShort();
                VmxStatic.yaw = bb.getFloat();

                TitanStatic.limitH0 = bb.get() == 1;
                TitanStatic.limitL0 = bb.get() == 1;
                TitanStatic.limitH1 = bb.get() == 1;
                TitanStatic.limitL1 = bb.get() == 1;
                TitanStatic.limitH2 = bb.get() == 1;
                TitanStatic.limitL2 = bb.get() == 1;
                TitanStatic.limitH3 = bb.get() == 1;
                TitanStatic.limitL3 = bb.get() == 1;

                VmxStatic.flex0 = bb.get() == 1;
                VmxStatic.flex1 = bb.get() == 1;
                VmxStatic.flex2 = bb.get() == 1;
                VmxStatic.flex3 = bb.get() == 1;
                VmxStatic.flex4 = bb.get() == 1;
                VmxStatic.flex5 = bb.get() == 1;
                VmxStatic.flex6 = bb.get() == 1;
                VmxStatic.flex7 = bb.get() == 1;
            }

            try { Thread.sleep(4); }
            catch (InterruptedException e){}
        }
    }
}
