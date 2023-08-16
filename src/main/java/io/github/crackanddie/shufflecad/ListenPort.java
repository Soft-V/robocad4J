package io.github.crackanddie.shufflecad;

import io.github.crackanddie.robocadSim.Holder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class ListenPort
{
    private final int port;

    private boolean stopThread = false;
    public String outString = "";
    public byte[] outBytes = new byte[0];

    private ServerSocket sct;
    private Thread thread;

    private final ICallback callbackMethod;

    private final float delay;

    public ListenPort(int port, ICallback callback, float delay)
    {
        this.port = port;
        this.callbackMethod = callback;
        this.delay = delay;
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
            this.sct = new ServerSocket();
            this.sct.bind(new InetSocketAddress("0.0.0.0", this.port));
            this.sct.setReuseAddress(true);
        }
        catch (IOException e)
        {
            // there could be a error
        }

        this.stopThread = false;

        try
        {
            var clientSocket = this.sct.accept();
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // there could be a error
            }

            if (Holder.LOG_LEVEL < Holder.LOG_EXC_INFO)
            {
                System.out.println(Holder.ANSI_CYAN + "Connected " + this.port + Holder.ANSI_RESET);
            }

            while (!this.stopThread)
            {
                if (this.isCamera)
                {
                    out.write("Wait for size".getBytes(StandardCharsets.UTF_16LE));
                    byte[] imgSize = new byte[4];
                    in.readFully(imgSize, 0, 4);

                    int bufferSize = (imgSize[3] & 0xff) << 24 | (imgSize[2] & 0xff) << 16 |
                            (imgSize[1] & 0xff) << 8 | (imgSize[0] & 0xff);
                    out.write("Wait for image".getBytes(StandardCharsets.UTF_16LE));
                    byte[] imageBytes = new byte[bufferSize];
                    in.readFully(imageBytes, 0, bufferSize);
                    this.outBytes = imageBytes;
                }
                else
                {
                    out.write("Wait for data".getBytes(StandardCharsets.UTF_16LE));
                    byte[] dataSize = new byte[4];
                    in.readFully(dataSize, 0, 4);

                    int length = (dataSize[3] & 0xff) << 24 | (dataSize[2] & 0xff) << 16 |
                            (dataSize[1] & 0xff) << 8 | (dataSize[0] & 0xff);
                    if(length > 0)
                    {
                        byte[] message = new byte[length];
                        in.readFully(message, 0, length);
                        this.outString = new String(message, StandardCharsets.UTF_16LE);
                    }
                }
                Thread.sleep(4);
            }

            if (Holder.LOG_LEVEL < Holder.LOG_EXC_INFO)
            {
                System.out.println(Holder.ANSI_CYAN + "Disconnected " + this.port + Holder.ANSI_RESET);
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
        this.outString = "";
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
                        if (Holder.LOG_LEVEL < Holder.LOG_EXC_WARN)
                        {
                            System.out.println(Holder.ANSI_YELLOW + "Warning: Something went wrong. Rude disconnection on port " +
                                    this.port + Holder.ANSI_RESET);
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
