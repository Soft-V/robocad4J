package io.github.softv.studica;

import io.github.softv.Common;
import io.github.softv.internal.studica.*;
import io.github.softv.internal.studica.shared.TitanStatic;
import io.github.softv.internal.studica.shared.VmxStatic;
import io.github.softv.robocadSim.Holder;
import io.github.softv.shufflecad.InfoHolder;
import io.github.softv.shufflecad.Shufflecad;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RobotVmxTitan {
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
    private ConnectionBase connection;

    public RobotVmxTitan() throws IOException { this(true); }
    public RobotVmxTitan(boolean isRealRobot) throws IOException {
        this.isRealRobot = isRealRobot;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.stop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (Common.LOG_LEVEL < Common.LOG_EXC_INFO)
            {
                System.out.println(Common.ANSI_CYAN + "Program stopped" + Common.ANSI_RESET);
            }
        }));

        InfoHolder.onRealRobot = isRealRobot;
        Shufflecad.start();

        if (!this.isRealRobot) {
            connection = new ConnectionSim();
        }
        else {
            connection = new ConnectionReal();
        }
        connection.start();
    }

    public void stop() throws InterruptedException {
        Shufflecad.stop();
        connection.stop();
        InfoHolder.logger.writeMainLog("Program stopped");
    }

    public void setMotorSpeed0(float speed) {
        TitanStatic.speedMotor0 = speed;
    }

    public void setMotorSpeed1(float speed) {
        TitanStatic.speedMotor1 = speed;
    }

    public void setMotorSpeed2(float speed) {
        TitanStatic.speedMotor2 = speed;
    }

    public void setMotorSpeed3(float speed) {
        TitanStatic.speedMotor3 = speed;
    }

    public float getMotorEnc0() {
        return TitanStatic.encMotor0;
    }

    public float getMotorEnc1() {
        return TitanStatic.encMotor1;
    }

    public float getMotorEnc2() {
        return TitanStatic.encMotor2;
    }

    public float getMotorEnc3() {
        return TitanStatic.encMotor3;
    }

    public float getYaw() {
        return VmxStatic.yaw;
    }

    public float getUltrasound1() {
        return VmxStatic.ultrasound1;
    }

    public float getUltrasound2() {
        return VmxStatic.ultrasound2;
    }

    public float getAnalog1() {
        return VmxStatic.analog1;
    }

    public float getAnalog2() {
        return VmxStatic.analog2;
    }

    public float getAnalog3() {
        return VmxStatic.analog3;
    }

    public float getAnalog4() {
        return VmxStatic.analog4;
    }

    public boolean[] getTitanLimits() {
        return new boolean[] { TitanStatic.limitH0, TitanStatic.limitL0,
                TitanStatic.limitH1, TitanStatic.limitL1,
                TitanStatic.limitH2, TitanStatic.limitL2,
                TitanStatic.limitH3, TitanStatic.limitL3,};
    }

    public boolean[] getVMXFlex() {
        return new boolean[] { VmxStatic.flex0, VmxStatic.flex1, VmxStatic.flex2, VmxStatic.flex3,
                VmxStatic.flex4, VmxStatic.flex5, VmxStatic.flex6, VmxStatic.flex7 };
    }

    public Mat getCameraImage() {
        return connection.getCamera();
    }

    /*
        ports are from 1 to 10 included
     */
    public void setAngleHCDIO(float value, int port) {
        VmxStatic.setServoAngle(value, port - 1);
    }

    public void setPwmHCDIO(float value, int port) {
        VmxStatic.setServoPWM(value, port - 1);
    }

    public void setBoolHCDIO(boolean value, int port) {
        VmxStatic.setLedState(value, port - 1);
    }

    private void updateSetData(){
        List<Float> lst = new ArrayList<Float>(Arrays.asList(this.motorSpeed0, this.motorSpeed1, this.motorSpeed2, this.motorSpeed3));
        lst.addAll(new ArrayList<>(Arrays.asList(hcdioValues)));
        this.connHelper.setData(lst);
    }

    private void updateEncs(){
        List<Float> values = this.connHelper.getData();
        if (values.size() == ConnectionSim.MAX_DATA_RECEIVE){
            this.motorEnc0 = values.get(0);
            this.motorEnc1 = values.get(1);
            this.motorEnc2 = values.get(2);
            this.motorEnc3 = values.get(3);
        }
    }

    private void updateSensors(){
        List<Float> values = this.connHelper.getData();
        if (values.size() == ConnectionSim.MAX_DATA_RECEIVE){
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
        List<Float> values = this.connHelper.getData();
        if (values.size() == ConnectionSim.MAX_DATA_RECEIVE){
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
        byte[] data = this.connHelper.getCamera();
        if (data.length == 921600)
        {
            Mat newMat = new Mat(480, 640, CvType.CV_8UC3);
            newMat.put(0, 0, data);
            cameraImage = newMat;
        }
    }
}
