package io.github.softv.internal.studica;

import io.github.softv.Common;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class ConnectionReal extends ConnectionBase {
    private VideoCapture cameraInstance = null;

    @Override
    public void start() {
        try
        {
            cameraInstance = new VideoCapture(0);
        }
        catch (Exception e)
        {
            if (Common.LOG_LEVEL < Common.LOG_EXC_WARN)
            {
                System.out.println(Common.ANSI_YELLOW + "Exception while creating camera instance" + Common.ANSI_RESET);
            }
        }

        SPI.startSPI();
        COM.startCOM();
        // todo: run pi-blaster
        // todo: RPI cringe
    }

    @Override
    public void stop() {
        SPI.stopThread = true;
        if (SPI.thread != null){
            try { SPI.thread.join(); }
            catch (InterruptedException e){}
        }
        COM.stopThread = true;
        if (COM.thread != null){
            try { COM.thread.join(); }
            catch (InterruptedException e){}
        }
    }

    @Override
    public Mat getCamera() {
        Mat m = new Mat();
        if (cameraInstance.read(m)){
            return m;
        }
        return null;
    }
}
