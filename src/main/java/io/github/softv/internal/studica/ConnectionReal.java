package io.github.softv.internal.studica;

import io.github.softv.Common;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

public class ConnectionReal extends ConnectionBase{
    private VideoCapture cameraInstance = null;

    private Thread robotInfoThread = null;
    private boolean stopRobotInfoThread = false;

    @Override
    public void start() {
        try{
            cameraInstance = new VideoCapture(0);
        }
        catch (Exception e){
            if (Common.LOG_LEVEL < Common.LOG_EXC_WARN)
            {
                System.out.println(Common.ANSI_YELLOW + "Exception while creating camera instance" + Common.ANSI_RESET);
            }
        }

        SPI.startSPI();
        COM.startCOM();
        try{
            Process piBlasterProcess = Runtime.getRuntime().exec("sudo /home/pi/pi-blaster/pi-blaster");
        }
        catch (Exception e){
            if (Common.LOG_LEVEL < Common.LOG_EXC_WARN)
            {
                System.out.println(Common.ANSI_YELLOW + "Exception while running pi-blaster" + Common.ANSI_RESET);
            }
        }
        this.stopRobotInfoThread = false;
        this.robotInfoThread = new Thread(this::updateRPICringe);
        this.robotInfoThread.setDaemon(true);
        this.robotInfoThread.start();
    }

    @Override
    public void stop() {
        SPI.stopThread = true;
        if (SPI.th != null){
            try { SPI.th.join(); }
            catch (InterruptedException e){}
        }
        COM.stopThread = true;
        if (COM.th != null){
            try { COM.th.join(); }
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

    private void updateRPICringe()
    {
        // todo: update some things
    }
}
