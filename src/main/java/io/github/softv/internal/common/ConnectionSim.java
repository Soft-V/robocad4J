package io.github.softv.internal.common;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;

public class ConnectionSim extends ConnectionBase
{
    private final int PORT_SET_DATA = 65431;
    private final int PORT_GET_DATA = 65432;
    private final int PORT_CAMERA = 65438;

    private final Robot robot;

    private final TalkPort talkChannel;
    private final ListenPort listenChannel;
    private final ListenPort cameraChannel;

    public ConnectionSim(Robot robot)
    {
        this.robot = robot;

        this.talkChannel = new TalkPort(this.robot, PORT_SET_DATA);
        this.listenChannel = new ListenPort(this.robot, PORT_GET_DATA);
        this.cameraChannel = new ListenPort(this.robot, PORT_CAMERA);

        this.talkChannel.startTalking();
        this.listenChannel.startListening();
        this.cameraChannel.startListening();
    }

    @Override
    public void stop()
    {
        this.talkChannel.stopTalking();
        this.listenChannel.stopListening();
        this.cameraChannel.stopListening();
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

    @Override
    public ArrayList<Integer> getLidar()
    {
        return null;
    }

    public void setData(byte[] data)
    {
        this.talkChannel.outBytes = data;
    }

    public byte[] getData()
    {
        return this.listenChannel.outBytes;
    }
}
