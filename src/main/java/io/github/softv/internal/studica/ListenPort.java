package io.github.softv.internal.studica;

import io.github.softv.Common;
import io.github.softv.internal.LowLevelFuncad;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class ListenPort
{
    private final int port;

    private boolean stopThread = false;
    public byte[] outBytes = new byte[0];

    private Socket sct;
    private Thread thread;

    public ListenPort(int port)
    {
        this.port = port;
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
        catch (IOException e)
        {
            // there could be an error
        }

        this.stopThread = false;

        try
        {
            DataInputStream in = new DataInputStream(this.sct.getInputStream());
            DataOutputStream out = new DataOutputStream(this.sct.getOutputStream());

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // there could be a error
            }

            if (Common.LOG_LEVEL < Common.LOG_EXC_INFO)
            {
                System.out.println(Common.ANSI_CYAN + "Connected " + this.port + Common.ANSI_RESET);
            }

            while (!this.stopThread)
            {
                LowLevelFuncad.writeBytes(out, "Wait for data".getBytes(StandardCharsets.UTF_16LE));
                this.outBytes = LowLevelFuncad.readBytes(in);

                Thread.sleep(4);
            }

            if (Common.LOG_LEVEL < Common.LOG_EXC_INFO)
            {
                System.out.println(Common.ANSI_CYAN + "Disconnected " + this.port + Common.ANSI_RESET);
            }

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
            }

            if (this.thread != null)
            {
                int stTime = LocalDateTime.now().toLocalTime().toSecondOfDay();
                while (this.thread.isAlive())
                {
                    if (LocalDateTime.now().toLocalTime().toSecondOfDay() - stTime > 1)
                    {
                        if (Common.LOG_LEVEL < Common.LOG_EXC_WARN)
                        {
                            System.out.println(Common.ANSI_YELLOW + "Warning: Something went wrong. Rude disconnection on port " +
                                    this.port + Common.ANSI_RESET);
                        }
                        try
                        {
                            this.sct.close();
                        }
                        catch (IOException e)
                        {
                            // there could be a error
                        }

                    }
                }
            }
        }
    }
}
