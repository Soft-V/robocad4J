package io.github.softv.internal.studica;

import io.github.softv.Common;
import io.github.softv.internal.LowLevelFuncad;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class TalkPort
{
    private final int port;

    private boolean stopThread = false;
    public byte[] outBytes = new byte[0];

    private Socket sct;
    private Thread thread;

    public TalkPort(int port)
    {
        this.port = port;
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
        catch (IOException e)
        {
            // there could be a error
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
                LowLevelFuncad.writeBytes(out, this.outBytes);
                byte[] _unused_ = LowLevelFuncad.readBytes(in);

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
