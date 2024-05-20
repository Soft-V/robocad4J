package io.github.softv.internal.studica;

import io.github.softv.internal.ParseChannels;
import io.github.softv.internal.studica.shared.TitanStatic;
import io.github.softv.internal.studica.shared.VmxStatic;
import io.github.softv.shufflecad.InfoHolder;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

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
        this.cameraChannel = new ListenPort(PORT_CAMERA, true);

        this.talkChannel.startTalking();
        this.listenChannel.startListening();
        this.cameraChannel.startListening();

        this.stopUpdateThread = false;
        this.updateThread = new Thread(this::update);
        this.updateThread.setDaemon(true);
        this.updateThread.start();

        // todo: update power
        InfoHolder.power = "12";
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
        this.talkChannel.outString = ParseChannels.JoinFloatChannel(lst);
    }

    private List<Float> getData()
    {
        return ParseChannels.ParseFloatChannel(this.listenChannel.outString);
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
            List<Float> lst = Arrays.asList(TitanStatic.speedMotor0, TitanStatic.speedMotor1,
                    TitanStatic.speedMotor2, TitanStatic.speedMotor3);
            lst.addAll(Arrays.asList(VmxStatic.hcdioValues));
            this.setData(lst);

            var values = getData();
            if (!values.isEmpty()){
                TitanStatic.encMotor0 = values.get(0);
            }
        }
    }
}
