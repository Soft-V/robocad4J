package io.github.crackanddie;

import io.github.crackanddie.pycad.COM;
import io.github.crackanddie.pycad.SPI;
import io.github.crackanddie.pycad.TitanStatic;
import io.github.crackanddie.pycad.VMXStatic;
import io.github.crackanddie.robocadSim.ConnectionHelperVMXTitan;
import io.github.crackanddie.robocadSim.Holder;

import io.github.crackanddie.shufflecad.InfoHolder;
import io.github.crackanddie.shufflecad.Shufflecad;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RobocadVMXTitan
{
    private final boolean isRealRobot;

    private float motorSpeed0 = 0;
    private float motorSpeed1 = 0;
    private float motorSpeed2 = 0;
    private float motorSpeed3 = 0;

    private float motorEnc0 = 0;
    private float motorEnc1 = 0;
    private float motorEnc2 = 0;
    private float motorEnc3 = 0;

    private boolean limitL0 = false;
    private boolean limitH0 = false;
    private boolean limitL1 = false;
    private boolean limitH1 = false;
    private boolean limitL2 = false;
    private boolean limitH2 = false;
    private boolean limitL3 = false;
    private boolean limitH3 = false;

    private float yaw = 0;

    private float ultrasound1 = 0;
    private float ultrasound2 = 0;

    private float analog1 = 0;
    private float analog2 = 0;
    private float analog3 = 0;
    private float analog4 = 0;

    private boolean flex0 = false;
    private boolean flex1 = false;
    private boolean flex2 = false;
    private boolean flex3 = false;
    private boolean flex4 = false;
    private boolean flex5 = false;
    private boolean flex6 = false;
    private boolean flex7 = false;

    private final Float[] hcdioValues = new Float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

    private ConnectionHelperVMXTitan connHelper = null;

     private Mat cameraImage = null;
     private VideoCapture cameraInstance = null;
     private Thread robotInfoThread = null;
     private boolean stopRobotInfoThread = false;

    public RobocadVMXTitan() throws IOException { this(true); }
    public RobocadVMXTitan(boolean isRealRobot) throws IOException {
        this.isRealRobot = isRealRobot;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.stop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (Holder.LOG_LEVEL < Holder.LOG_EXC_INFO)
            {
                System.out.println(Holder.ANSI_CYAN + "Program stopped" + Holder.ANSI_RESET);
            }
        }));

        InfoHolder.onRealRobot = isRealRobot;
        Shufflecad.start();

        if (!this.isRealRobot)
        {
            connHelper = new ConnectionHelperVMXTitan();
            connHelper.startChannels();

            InfoHolder.power = "12"; // :)
        }
        else
        {
            try
            {
                 cameraInstance = new VideoCapture(0);
            }
            catch (Exception e)
            {
                if (Holder.LOG_LEVEL < Holder.LOG_EXC_WARN)
                {
                    System.out.println(Holder.ANSI_YELLOW + "Exception while creating camera instance" + Holder.ANSI_RESET);
                }
            }

            SPI.startSPI();
            COM.startCOM();
            Process piBlasterProcess = Runtime.getRuntime().exec("sudo /home/pi/pi-blaster/pi-blaster");
            this.stopRobotInfoThread = false;
            this.robotInfoThread = new Thread(this::updateRPICringe);
            this.robotInfoThread.setDaemon(true);
            this.robotInfoThread.start();
        }
    }

    private void updateRPICringe()
    {
        // todo: update some things
    }

    public void stop() throws InterruptedException {
        Shufflecad.stop();
        if (!this.isRealRobot)
        {
            connHelper.stopChannels();
        }
        else{
            this.stopRobotInfoThread = true;
            this.robotInfoThread.join();
            COM.stopThread = true;
            COM.th.join();
            SPI.stopThread = true;
            SPI.th.join();
        }
        InfoHolder.logger.writeMainLog("Program stopped");
    }

    public void setMotorSpeed0(float speed) {
        this.motorSpeed0 = speed;
        if (!this.isRealRobot){
            updateSetData();
        }
        else{
            TitanStatic.speedMotor0 = speed;
        }
    }

    public void setMotorSpeed1(float speed) {
        this.motorSpeed1 = speed;
        if (!this.isRealRobot){
            updateSetData();
        }
        else{
            TitanStatic.speedMotor1 = speed;
        }
    }

    public void setMotorSpeed2(float speed) {
        this.motorSpeed2 = speed;
        if (!this.isRealRobot){
            updateSetData();
        }
        else{
            TitanStatic.speedMotor2 = speed;
        }
    }

    public void setMotorSpeed3(float speed) {
        this.motorSpeed3 = speed;
        if (!this.isRealRobot){
            updateSetData();
        }
        else{
            TitanStatic.speedMotor3 = speed;
        }
    }

    public float getMotorEnc0() {
        if (!this.isRealRobot){
            updateEncs();
            return motorEnc0;
        }
        else{
            return TitanStatic.encMotor0;
        }
    }

    public float getMotorEnc1() {
        if (!this.isRealRobot){
            updateEncs();
            return motorEnc1;
        }
        else{
            return TitanStatic.encMotor1;
        }
    }

    public float getMotorEnc2() {
        if (!this.isRealRobot){
            updateEncs();
            return motorEnc2;
        }
        else{
            return TitanStatic.encMotor2;
        }
    }

    public float getMotorEnc3() {
        if (!this.isRealRobot){
            updateEncs();
            return motorEnc3;
        }
        else{
            return TitanStatic.encMotor3;
        }
    }

    public float getYaw() {
        if (!this.isRealRobot){
            updateSensors();
            return yaw;
        }
        else{
            return VMXStatic.yaw;
        }
    }

    public float getUltrasound1() {
        if (!this.isRealRobot){
            updateSensors();
            return ultrasound1;
        }
        else{
            return VMXStatic.ultrasound1;
        }
    }

    public float getUltrasound2() {
        if (!this.isRealRobot){
            updateSensors();
            return ultrasound2;
        }
        else{
            return VMXStatic.ultrasound2;
        }
    }

    public float getAnalog1() {
        if (!this.isRealRobot){
            updateSensors();
            return analog1;
        }
        else{
            return VMXStatic.analog1;
        }
    }

    public float getAnalog2() {
        if (!this.isRealRobot){
            updateSensors();
            return analog2;
        }
        else{
            return VMXStatic.analog2;
        }
    }

    public float getAnalog3() {
        if (!this.isRealRobot){
            updateSensors();
            return analog3;
        }
        else{
            return VMXStatic.analog3;
        }
    }

    public float getAnalog4() {
        if (!this.isRealRobot){
            updateSensors();
            return analog4;
        }
        else{
            return VMXStatic.analog4;
        }
    }

    public boolean[] getTitanLimits() {
        if (!this.isRealRobot){
            updateButtons();
            return new boolean[] { this.limitH0, this.limitL0, this.limitH1, this.limitL1,
                    this.limitH2, this.limitL2, this.limitH3, this.limitL3 };
        }
        else{
            return new boolean[] { TitanStatic.limitH0, TitanStatic.limitL0,
                                   TitanStatic.limitH1, TitanStatic.limitL1,
                                   TitanStatic.limitH2, TitanStatic.limitL2,
                                   TitanStatic.limitH3, TitanStatic.limitL3,};
        }
    }

    public boolean[] getVMXFlex() {
        if (!this.isRealRobot){
            updateButtons();
            return new boolean[] { this.flex0, this.flex1, this.flex2, this.flex3,
                    this.flex4, this.flex5, this.flex6, this.flex7 };
        }
        else{
            return new boolean[] { VMXStatic.flex0, VMXStatic.flex1, VMXStatic.flex2, VMXStatic.flex3,
                    VMXStatic.flex4, VMXStatic.flex5, VMXStatic.flex6, VMXStatic.flex7 };
        }
    }

    public Mat getCameraImage() {
        if (!this.isRealRobot){
            updateCamera();
        }
        else
        {
            Mat m = new Mat();
            if (cameraInstance.read(m)){
                cameraImage = m;
            }
        }
        return cameraImage;
    }

    /*
        ports are from 1 to 10 included
     */
    public void setAngleHCDIO(float value, int port) {
        if (!this.isRealRobot){
            this.hcdioValues[port - 1] = 0.000666f * value + 0.05f;
            updateSetData();
        }
        else{
            VMXStatic.setServoAngle(value, port - 1);
        }
    }

    public void setPwmHCDIO(float value, int port) {
        if (!this.isRealRobot){
            this.hcdioValues[port - 1] = value;
            updateSetData();
        }
        else{
            VMXStatic.setServoPWM(value, port - 1);
        }
    }

    public void setBoolHCDIO(boolean value, int port) {
        if (!this.isRealRobot){
            this.hcdioValues[port - 1] = value ? 0.2f : 0.0f;
            updateSetData();
        }
        else{
            VMXStatic.setLedState(value, port - 1);
        }
    }

    private void updateSetData(){
        var lst = new ArrayList<Float>(Arrays.asList(this.motorSpeed0, this.motorSpeed1, this.motorSpeed2, this.motorSpeed3));
        lst.addAll(List.of(hcdioValues));
        this.connHelper.setData(lst);
    }

    private void updateEncs(){
        var values = this.connHelper.getData();
        if (values.size() == ConnectionHelperVMXTitan.MAX_DATA_RECEIVE){
            this.motorEnc0 = values.get(0);
            this.motorEnc1 = values.get(1);
            this.motorEnc2 = values.get(2);
            this.motorEnc3 = values.get(3);
        }
    }

    private void updateSensors(){
        var values = this.connHelper.getData();
        if (values.size() == ConnectionHelperVMXTitan.MAX_DATA_RECEIVE){
            this.ultrasound1 = values.get(4);
            this.ultrasound2 = values.get(5);
            this.analog1 = values.get(6);
            this.analog2 = values.get(7);
            this.analog3 = values.get(8);
            this.analog4 = values.get(9);
            this.yaw = values.get(10);
        }
    }

    private void updateButtons(){
        var values = this.connHelper.getData();
        if (values.size() == ConnectionHelperVMXTitan.MAX_DATA_RECEIVE){
            this.limitH0 = values.get(11) == 1;
            this.limitL0 = values.get(12) == 1;
            this.limitH1 = values.get(13) == 1;
            this.limitL1 = values.get(14) == 1;
            this.limitH2 = values.get(15) == 1;
            this.limitL2 = values.get(16) == 1;
            this.limitH3 = values.get(17) == 1;
            this.limitL3 = values.get(18) == 1;

            this.flex0 = values.get(19) == 1;
            this.flex1 = values.get(20) == 1;
            this.flex2 = values.get(21) == 1;
            this.flex3 = values.get(22) == 1;
            this.flex4 = values.get(23) == 1;
            this.flex5 = values.get(24) == 1;
            this.flex6 = values.get(25) == 1;
            this.flex7 = values.get(26) == 1;
        }
    }

    private void updateCamera(){
        var data = this.connHelper.getCamera();
        if (data.length == 921600)
        {
            Mat newMat = new Mat(480, 640, CvType.CV_8UC3);
            newMat.put(0, 0, data);
            cameraImage = newMat;
        }
    }
}
