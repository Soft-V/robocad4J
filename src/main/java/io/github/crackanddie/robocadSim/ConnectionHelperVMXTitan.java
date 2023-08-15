package io.github.crackanddie.robocadSim;

import io.github.crackanddie.common.ParseChannels;

import java.util.List;

public class ConnectionHelperVMXTitan
{
    public static final int MAX_DATA_RECEIVE = 27;
    public static final int MAX_DATA_TRANSMIT = 14;

    private final int PORT_SET_DATA = 65431;
    private final int PORT_GET_DATA = 65432;
    private final int PORT_CAMERA = 65438;

    private TalkPort talkChannel;
    private ListenPort listenChannel;
    private ListenPort cameraChannel;

    public ConnectionHelperVMXTitan()
    {
        this.talkChannel = new TalkPort(PORT_SET_DATA);
        this.listenChannel = new ListenPort(PORT_GET_DATA);
        this.cameraChannel = new ListenPort(PORT_CAMERA, true);
    }

    public void startChannels()
    {
        this.talkChannel.startTalking();
        this.listenChannel.startListening();
        this.cameraChannel.startListening();
    }

    public void stopChannels()
    {
        this.talkChannel.stopTalking();
        this.listenChannel.stopListening();
        this.cameraChannel.stopListening();
    }

    public void setData(List<Float> lst)
    {
        this.talkChannel.outString = ParseChannels.JoinFloatChannel(lst);
    }

    public List<Float> getData()
    {
        return ParseChannels.ParseFloatChannel(this.listenChannel.outString);
    }

    public byte[] getCamera()
    {
        return this.cameraChannel.outBytes;
    }
}
