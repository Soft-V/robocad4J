package io.github.crackanddie;

import io.github.crackanddie.connection.ConnectionHelper;
import io.github.crackanddie.connection.Holder;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

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

    private Float[] hcdioValues = new Float[10];

    private ConnectionHelper connHelper = null;

    private Mat cameraImage = null;
    private VideoCapture cameraInstance = null;

    public RobocadVMXTitan() { this(true); }
    public RobocadVMXTitan(boolean isRealRobot)
    {
        this.isRealRobot = isRealRobot;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.stop();
            if (Holder.LOG_LEVEL < Holder.LOG_EXC_INFO)
            {
                System.out.println(Holder.ANSI_CYAN + "Program stopped" + Holder.ANSI_RESET);
            }
        }));

        // todo: start shufflecad

        if (!this.isRealRobot)
        {
            connHelper = new ConnectionHelper(Holder.CONN_ALL);
            connHelper.startChannels();
        }
        else
        {
            // todo: for real robot
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
        }
    }

    private static void updateRPICringe()
    {
        // todo: update some things
    }

    public void stop()
    {
        // todo: start shufflecad
        if (!this.isRealRobot)
        {
            connHelper.stopChannels();
        }
    }

    public void setMotorSpeed0(float speed) {
        this.motorSpeed0 = speed;
        if (!this.isRealRobot){
            updateMotors();
        }
    }

    public void setMotorSpeed1(float speed) {
        this.motorSpeed1 = speed;
        if (!this.isRealRobot){
            updateMotors();
        }
    }

    public void setMotorSpeed2(float speed) {
        this.motorSpeed2 = speed;
        if (!this.isRealRobot){
            updateMotors();
        }
    }

    public void setMotorSpeed3(float speed) {
        this.motorSpeed3 = speed;
        if (!this.isRealRobot){
            updateMotors();
        }
    }

    public float getMotorEnc0() {
        if (!this.isRealRobot){
            updateEncs();
        }
        return motorEnc0;
    }

    public float getMotorEnc1() {
        if (!this.isRealRobot){
            updateEncs();
        }
        return motorEnc1;
    }

    public float getMotorEnc2() {
        if (!this.isRealRobot){
            updateEncs();
        }
        return motorEnc2;
    }

    public float getMotorEnc3() {
        if (!this.isRealRobot){
            updateEncs();
        }
        return motorEnc3;
    }

    public float getYaw() {
        if (!this.isRealRobot){
            updateSensors();
        }
        return yaw;
    }

    public float getUltrasound1() {
        if (!this.isRealRobot){
            updateSensors();
        }
        return ultrasound1;
    }

    public float getUltrasound2() {
        if (!this.isRealRobot){
            updateSensors();
        }
        return ultrasound2;
    }

    public float getAnalog1() {
        if (!this.isRealRobot){
            updateSensors();
        }
        return analog1;
    }

    public float getAnalog2() {
        if (!this.isRealRobot){
            updateSensors();
        }
        return analog2;
    }

    public float getAnalog3() {
        if (!this.isRealRobot){
            updateSensors();
        }
        return analog3;
    }

    public float getAnalog4() {
        if (!this.isRealRobot){
            updateSensors();
        }
        return analog4;
    }

    public boolean[] getTitanLimits() {
        if (!this.isRealRobot){
            updateButtons();
        }
        return new boolean[] { this.limitH0, this.limitL0, this.limitH1, this.limitL1,
                this.limitH2, this.limitL2, this.limitH3, this.limitL3 };
    }

    public boolean[] getVMXFlex() {
        if (!this.isRealRobot){
            updateButtons();
        }
        return new boolean[] { this.flex0, this.flex1, this.flex2, this.flex3,
                this.flex4, this.flex5, this.flex6, this.flex7 };
    }

    public Mat getCameraImage() {
        if (!this.isRealRobot){
            updateCamera();
        }
        return cameraImage;
    }

    /*
        ports are from 1 to 10 included
     */
    public void setAngleHCDIO(float value, int port) {
        if (!this.isRealRobot){
            this.hcdioValues[port - 1] = 0.000666f * value + 0.05f;
            updateOMS();
        }
        else{
            // todo: for real robot
        }
    }

    public void setPwmHCDIO(float value, int port) {
        if (!this.isRealRobot){
            this.hcdioValues[port - 1] = value;
            updateOMS();
        }
        else{
            // todo: for real robot
        }
    }

    public void setBoolHCDIO(boolean value, int port) {
        if (!this.isRealRobot){
            this.hcdioValues[port - 1] = value ? 0.2f : 0.0f;
            updateOMS();
        }
        else{
            // todo: for real robot
        }
    }

    private void updateOther(){
        this.connHelper.setOther(Arrays.asList(0f, 0f, 0f));
    }

    private void updateMotors(){
        this.connHelper.setMotors(Arrays.asList(this.motorSpeed0, this.motorSpeed1, this.motorSpeed2, this.motorSpeed3));
    }

    private void updateOMS(){
        this.connHelper.setOMS(Arrays.asList(hcdioValues));
    }

    private void updateResets(){
        this.connHelper.setResets(Arrays.asList(false, false, false));
    }

    private void updateEncs(){
        var values = this.connHelper.getEncs();
        if (values.size() == 4){
            this.motorEnc0 = values.get(0);
            this.motorEnc1 = values.get(1);
            this.motorEnc2 = values.get(2);
            this.motorEnc3 = values.get(3);
        }
    }

    private void updateSensors(){
        var values = this.connHelper.getSens();
        if (values.size() == 7){
            this.ultrasound1 = values.get(0);
            this.ultrasound2 = values.get(1);
            this.analog1 = values.get(2);
            this.analog2 = values.get(3);
            this.analog3 = values.get(4);
            this.analog4 = values.get(5);
            this.yaw = values.get(6);
        }
    }

    private void updateButtons(){
        var values = this.connHelper.getButtons();
        if (values.size() == 16){
            this.limitH0 = values.get(0);
            this.limitL0 = values.get(1);
            this.limitH1 = values.get(2);
            this.limitL1 = values.get(3);
            this.limitH2 = values.get(4);
            this.limitL2 = values.get(5);
            this.limitH3 = values.get(6);
            this.limitL3 = values.get(7);

            this.flex0 = values.get(8);
            this.flex1 = values.get(9);
            this.flex2 = values.get(10);
            this.flex3 = values.get(11);
            this.flex4 = values.get(12);
            this.flex5 = values.get(13);
            this.flex6 = values.get(14);
            this.flex7 = values.get(15);
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
