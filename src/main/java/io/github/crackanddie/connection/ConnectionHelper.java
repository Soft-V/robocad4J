package io.github.crackanddie.connection;

import java.util.List;

public class ConnectionHelper
{
    private TalkPort otherChannel;
    private TalkPort motorsChannel;
    private TalkPort omsChannel;
    private TalkPort resetsChannel;
    private ListenPort encsChannel;
    private ListenPort sensorsChannel;
    private ListenPort buttonsChannel;
    private ListenPort cameraChannel;

    private final int setting;

    public ConnectionHelper(int setting)
    {
        this.setting = setting;

        if ((this.setting & Holder.CONN_OTHER) > 0)
        {
            this.otherChannel = new TalkPort(65431);
        }
        if ((this.setting & Holder.CONN_MOTORS_AND_ENCS) > 0)
        {
            this.motorsChannel = new TalkPort(65432);
            this.encsChannel = new ListenPort(65435);
        }
        if ((this.setting & Holder.CONN_OMS) > 0)
        {
            this.omsChannel = new TalkPort(65433);
        }
        if ((this.setting & Holder.CONN_RESETS) > 0)
        {
            this.resetsChannel = new TalkPort(65434);
        }
        if ((this.setting & Holder.CONN_SENS) > 0)
        {
            this.sensorsChannel = new ListenPort(65436);
        }
        if ((this.setting & Holder.CONN_BUTTONS) > 0)
        {
            this.buttonsChannel = new ListenPort(65437);
        }
        if ((this.setting & Holder.CONN_CAMERA) > 0)
        {
            this.cameraChannel = new ListenPort(65438, true);
        }
    }

    public void startChannels()
    {
        if ((this.setting & Holder.CONN_OTHER) > 0)
        {
            this.otherChannel.startTalking();
        }
        if ((this.setting & Holder.CONN_MOTORS_AND_ENCS) > 0)
        {
            this.motorsChannel.startTalking();
            this.encsChannel.startListening();
        }
        if ((this.setting & Holder.CONN_OMS) > 0)
        {
            this.omsChannel.startTalking();
        }
        if ((this.setting & Holder.CONN_RESETS) > 0)
        {
            this.resetsChannel.startTalking();
        }
        if ((this.setting & Holder.CONN_SENS) > 0)
        {
            this.sensorsChannel.startListening();
        }
        if ((this.setting & Holder.CONN_BUTTONS) > 0)
        {
            this.buttonsChannel.startListening();
        }
        if ((this.setting & Holder.CONN_CAMERA) > 0)
        {
            this.cameraChannel.startListening();
        }
    }

    public void stopChannels()
    {
        if ((this.setting & Holder.CONN_OTHER) > 0)
        {
            this.otherChannel.stopTalking();
        }
        if ((this.setting & Holder.CONN_MOTORS_AND_ENCS) > 0)
        {
            this.motorsChannel.stopTalking();
            this.encsChannel.stopListening();
        }
        if ((this.setting & Holder.CONN_OMS) > 0)
        {
            this.omsChannel.stopTalking();
        }
        if ((this.setting & Holder.CONN_RESETS) > 0)
        {
            this.resetsChannel.stopTalking();
        }
        if ((this.setting & Holder.CONN_SENS) > 0)
        {
            this.sensorsChannel.stopListening();
        }
        if ((this.setting & Holder.CONN_BUTTONS) > 0)
        {
            this.buttonsChannel.stopListening();
        }
        if ((this.setting & Holder.CONN_CAMERA) > 0)
        {
            this.cameraChannel.stopListening();
        }
    }

    public void setOther(List<Float> lst)
    {
        this.otherChannel.outString = ParseChannels.JoinFloatChannel(lst);
    }

    public void setMotors(List<Float> lst)
    {
        this.motorsChannel.outString = ParseChannels.JoinFloatChannel(lst);
    }

    public void setOMS(List<Float> lst)
    {
        this.omsChannel.outString = ParseChannels.JoinFloatChannel(lst);
    }

    public void setResets(List<Boolean> lst)
    {
        this.resetsChannel.outString = ParseChannels.JoinBoolChannel(lst);
    }

    public List<Float> getEncs()
    {
        return ParseChannels.ParseFloatChannel(this.encsChannel.outString);
    }

    public List<Float> getSens()
    {
        return ParseChannels.ParseFloatChannel(this.sensorsChannel.outString);
    }

    public List<Boolean> getButtons()
    {
        return ParseChannels.ParseBoolChannel(this.buttonsChannel.outString);
    }

    public byte[] getCamera()
    {
        return this.cameraChannel.outBytes;
    }
}
