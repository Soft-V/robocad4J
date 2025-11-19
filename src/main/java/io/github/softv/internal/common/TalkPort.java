package io.github.softv.internal.common;

import io.github.softv.internal.LowLevelFuncad;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;

public class TalkPort
{
    private final int port;
    private final Robot robot;

    private boolean stopThread = false;
    public byte[] outBytes = new byte[0];

    private Socket sct;
    private Thread thread;

    public TalkPort(Robot robot, int port)
    {
        this.port = port;
        this.robot = robot;
    }

    public void startTalking()
    {
        this.thread = new Thread(this::talking);
        this.thread.start();
    }

    private void talking()
    {
        try
        {
            this.sct = new Socket("localhost", this.port);
        }
        catch (Exception e)
        {
            // there could be a error
            this.robot.writeLog("TP: Failed to connect on port " + this.port);
            System.out.println("TP: Failed to connect on port " + this.port);
            try
            {
                this.sct.shutdownInput();
                this.sct.shutdownOutput();
                this.sct.close();
            }
            catch (Exception ignored) {}
            return;
        }

        try
        {
            DataInputStream in = new DataInputStream(this.sct.getInputStream());
            DataOutputStream out = new DataOutputStream(this.sct.getOutputStream());

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ignored) { }

            this.robot.writeLog("TP: Connected " + this.port);

            while (!this.stopThread)
            {
                LowLevelFuncad.writeBytes(out, this.outBytes);
                byte[] _unused_ = LowLevelFuncad.readBytes(in);

                Thread.sleep(4);
            }

            this.robot.writeLog("TP: Disconnected " + this.port);

            this.sct.shutdownInput();
            this.sct.shutdownOutput();
            this.sct.close();
        }
        catch (IOException | InterruptedException e)
        {
            // there could be a error
        }
    }

    private void resetOut()
    {
        this.outBytes = new byte[0];
    }

    public void stopTalking()
    {
        this.stopThread = true;
        this.resetOut();
        if (this.sct != null)
        {
            try
            {
                this.sct.shutdownInput();
                this.sct.shutdownOutput();
            }
            catch (IOException e)
            {
                // there could be a error
                this.robot.writeLog("Something went wrong while shutting down socket on port " + this.port);
            }

            if (this.thread != null)
            {
                int stTime = LocalDateTime.now().toLocalTime().toSecondOfDay();
                while (this.thread.isAlive())
                {
                    if (LocalDateTime.now().toLocalTime().toSecondOfDay() - stTime > 1)
                    {
                        this.robot.writeLog("Something went wrong. Rude disconnection on port " + this.port);
                        try
                        {
                            this.sct.close();
                        }
                        catch (IOException e)
                        {
                            // there could be a error
                            this.robot.writeLog("Something went wrong while closing socket on port " + this.port);
                        }
                    }
                }
            }
        }
    }
}
