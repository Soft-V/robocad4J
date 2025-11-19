package io.github.softv.internal.common;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;

public class ConnectionReal extends ConnectionBase {
    private final Robot robot;
    private final Updater updater;
    private final JavaWrapper lib;

    private VideoCapture cameraInstance = null;

    private Thread robotInfoThread = null;

    public ConnectionReal(Robot robot, Updater updater, RobotConfiguration conf) {
        this.robot = robot;
        this.updater = updater;
        this.lib = new JavaWrapper();

        try {
            cameraInstance = new VideoCapture(conf.cameraIndex);
        }
        catch (Exception e) {
            this.robot.writeLog("Exception while creating camera instance: ");
            this.robot.writeLog(e.getMessage());
        }

        // TODO: lidar instance

        if (conf.withPiBlaster)
        {
            try {
                Process piBlasterProcess = Runtime.getRuntime().exec("sudo /home/pi/pi-blaster/pi-blaster");
            }
            catch (Exception e) {
                this.robot.writeLog("Exception while running pi-blaster: ");
                this.robot.writeLog(e.getMessage());
            }
        }

        this.robotInfoThread = new Thread(this.updater::update);
        this.robotInfoThread.setDaemon(true);
        this.robotInfoThread.start();
    }

    @Override
    public void stop() {
        // TODO: stop lidar
        this.updater.stopRobotInfoThread = true;
        try {
            this.robotInfoThread.join();
        } catch (InterruptedException ignored) { }
    }

    @Override
    public Mat getCamera() {
        Mat m = new Mat();
        if (cameraInstance.read(m)){
            return m;
        }
        return null;
    }

    @Override
    public ArrayList<Integer> getLidar() {
        // TODO: return normal value
        return null;
    }

    public int spiIni(String path, int channel, int speed, int mode) {
        return this.lib.startSPI(path, channel, speed, mode);
    }

    public int comIni(String path, int baud) {
        return this.lib.startUSB(path, baud);
    }

    public byte[] spiRw(byte[] data) {
        return this.lib.readWriteSPI(data, data.length);
    }

    public byte[] comRw(byte[] data) {
        return this.lib.readWriteUSB(data, data.length);
    }

    public void spiStop() {
        this.lib.stopSPI();
    }

    public void comStop() {
        this.lib.stopUSB();
    }
}
