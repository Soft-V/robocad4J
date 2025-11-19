package io.github.softv.shufflecad;

import io.github.softv.internal.common.Robot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TalkPort {
    private final int port;
    private final Robot robot;

    private boolean stopThread = false;
    public String outString = "null";
    public byte[] outBytes = "null".getBytes(StandardCharsets.UTF_8);
    public String strFromClient = "-1";

    private ServerSocket sct;
    private Thread thread;

    private final ICallback callbackMethod;

    private final int delay;

    private final boolean isCamera;

    public TalkPort(Robot robot, int port, ICallback callback, int delay, boolean isCamera)
    {
        this.robot = robot;
        this.port = port;
        this.callbackMethod = callback;
        this.delay = delay;
        this.isCamera = isCamera;
    }

    public void startTalking()
    {
        this.thread = new Thread(this::talking);
        this.thread.start();
    }

    private void eventCall(){
        if (callbackMethod != null){
            callbackMethod.onCall();
        }
    }

    private void talking()
    {
        try
        {
            this.sct = new ServerSocket();
            this.sct.bind(new InetSocketAddress("0.0.0.0", this.port));
            this.sct.setReuseAddress(true);
        }
        catch (Exception e)
        {
            // there could be a error
            try
            {
                this.sct.close();
            }
            catch (Exception ignored) { }
            this.robot.writeLog("Shufflecad TP: Failed to connect on port " + this.port);
            return;
        }

        try
        {
            Socket clientSocket = this.sct.accept();
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ignored) { }

            while (!this.stopThread)
            {
                eventCall();

                if (this.isCamera){
                    // System.out.println(this.outString);
                    byte[] data = this.outString.getBytes(StandardCharsets.UTF_8);
                    ReadWriteSocketHelper.write(out, data);
                    // no use
                    ReadWriteSocketHelper.read(in);
                    ReadWriteSocketHelper.write(out, this.outBytes);
                    // System.out.println(this.outBytes.length);
                    this.strFromClient = new String(ReadWriteSocketHelper.read(in), StandardCharsets.UTF_8);
                }
                else{
                    // System.out.println(this.outString);
                    byte[] data = this.outString.getBytes(StandardCharsets.UTF_8);
                    ReadWriteSocketHelper.write(out, data);
                    // no use
                    this.strFromClient = new String(ReadWriteSocketHelper.read(in), StandardCharsets.UTF_8);
                }

                Thread.sleep(this.delay);
            }

            this.sct.close();
        }
        catch (IOException | InterruptedException | NullPointerException e)
        {
        }
    }

    private void resetOut()
    {
        this.outBytes = "null".getBytes(StandardCharsets.UTF_8);
        this.outString = "null";
    }

    public void stopTalking()
    {
        this.stopThread = true;
        this.resetOut();
        if (this.sct != null)
        {
            try
            {
                this.sct.close();
            }
            catch (IOException | NullPointerException e)
            {
                // there could be a error
            }

            if (this.thread != null)
            {
                try {
                    this.thread.interrupt();
                }
                catch (Exception e){

                }
//                int stTime = LocalDateTime.now().toLocalTime().toSecondOfDay();
//                while (this.thread.isAlive())
//                {
//                    if (LocalDateTime.now().toLocalTime().toSecondOfDay() - stTime > 1)
//                    {
//                        if (Holder.LOG_LEVEL < Holder.LOG_EXC_WARN)
//                        {
//                            System.out.println(Holder.ANSI_YELLOW + "Warning: Something went wrong. Rude disconnection on port " +
//                                    this.port + Holder.ANSI_RESET);
//                        }
//                        try
//                        {
//                            this.sct.close();
//                            this.sct = null;
//                        }
//                        catch (IOException | NullPointerException e)
//                        {
//                            // there could be a error
//                        }
//
//                    }
//                }
            }
        }
    }
}
