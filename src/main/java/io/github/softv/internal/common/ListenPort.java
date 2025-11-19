package io.github.softv.internal.common;

import io.github.softv.internal.LowLevelFuncad;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class ListenPort
{
    private final int port;
    private final Robot robot;

    private boolean stopThread = false;
    public byte[] outBytes = new byte[0];

    private Socket sct;
    private Thread thread;

    public ListenPort(Robot robot, int port)
    {
        this.port = port;
        this.robot = robot;
    }

    public void startListening()
    {
        this.thread = new Thread(this::listening);
        this.thread.start();
    }

    private void listening()
    {
        try
        {
            this.sct = new Socket("localhost", this.port);
        }
        catch (Exception e)
        {
            // there could be an error
            this.robot.writeLog("LP: Failed to connect on port " + this.port);
            System.out.println("LP: Failed to connect on port " + this.port);
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
            catch (InterruptedException ignored) {}

            this.robot.writeLog("LP: Connected " + this.port);

            while (!this.stopThread)
            {
                LowLevelFuncad.writeBytes(out, "Wait for data".getBytes(StandardCharsets.UTF_16LE));
                this.outBytes = LowLevelFuncad.readBytes(in);

                Thread.sleep(4);
            }

            this.robot.writeLog("LP: Disconnected " + this.port);

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

    public void stopListening()
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
