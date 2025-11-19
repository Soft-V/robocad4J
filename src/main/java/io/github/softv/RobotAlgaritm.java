package io.github.softv;

import io.github.softv.internal.AlgaritmInternal;
import io.github.softv.internal.algaritm.DefaultAlgaritmConfiguration;
import io.github.softv.internal.common.Robot;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;

public class RobotAlgaritm extends Robot {
    private final AlgaritmInternal algaritmInternal;

    public RobotAlgaritm() throws IOException { this(true, null); }
    public RobotAlgaritm(boolean isRealRobot) throws IOException { this(isRealRobot, null); }
    public RobotAlgaritm(boolean isRealRobot, DefaultAlgaritmConfiguration conf) throws IOException {
        if (conf == null) {
            conf = new DefaultAlgaritmConfiguration();
        }
        init(isRealRobot, conf); // fucking java shite
        algaritmInternal = new AlgaritmInternal(this, conf);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            writeLog("Program stopping");
            try {
                this.stop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public void stop() throws InterruptedException {
        algaritmInternal.stop();
    }

    public void setMotorSpeed0(float speed) {
        algaritmInternal.speedMotor0 = speed;
    }

    public void setMotorSpeed1(float speed) {
        algaritmInternal.speedMotor1 = speed;
    }

    public void setMotorSpeed2(float speed) {
        algaritmInternal.speedMotor2 = speed;
    }

    public void setMotorSpeed3(float speed) {
        algaritmInternal.speedMotor3 = speed;
    }

    public float getMotorEnc0() {
        return algaritmInternal.encMotor0;
    }

    public float getMotorEnc1() {
        return algaritmInternal.encMotor1;
    }

    public float getMotorEnc2() {
        return algaritmInternal.encMotor2;
    }

    public float getMotorEnc3() {
        return algaritmInternal.encMotor3;
    }

    public float getYaw() {
        return algaritmInternal.yaw;
    }

    public float getPitch() {
        return algaritmInternal.pitch;
    }

    public float getRoll() {
        return algaritmInternal.roll;
    }

    public float getUltrasound1() {
        return algaritmInternal.ultrasound1;
    }

    public float getUltrasound2() {
        return algaritmInternal.ultrasound2;
    }

    public float getUltrasound3() {
        return algaritmInternal.ultrasound3;
    }

    public float getUltrasound4() {
        return algaritmInternal.ultrasound4;
    }

    public float getAnalog1() {
        return algaritmInternal.analog1;
    }

    public float getAnalog2() {
        return algaritmInternal.analog2;
    }

    public float getAnalog3() {
        return algaritmInternal.analog3;
    }

    public float getAnalog4() {
        return algaritmInternal.analog4;
    }

    public float getAnalog5() {
        return algaritmInternal.analog5;
    }

    public float getAnalog6() {
        return algaritmInternal.analog6;
    }

    public float getAnalog7() {
        return algaritmInternal.analog7;
    }

    public float getAnalog8() {
        return algaritmInternal.analog8;
    }

    public void setAdditionalServo1(float angle) {
        algaritmInternal.additionalServo1 = angle;
    }

    public void setAdditionalServo2(float angle) {
        algaritmInternal.additionalServo2 = angle;
    }

    public void setPidSettings(boolean usePid, float p, float i, float d) {
        algaritmInternal.usePid = usePid;
        algaritmInternal.pPid = p;
        algaritmInternal.iPid = i;
        algaritmInternal.dPid = d;
    }

    /*
        num 1 or 2
    */
    public void stepMotorMove(int num, int steps, int stepsPerS, boolean direction) {
        algaritmInternal.stepMotorMove(num, steps, stepsPerS, direction);
    }

    public boolean isStep1Busy() {
        return algaritmInternal.isStep1Busy;
    }

    public boolean isStep2Busy() {
        return algaritmInternal.isStep2Busy;
    }

    public boolean[] getTitanLimits() {
        return new boolean[] { algaritmInternal.limitH0, algaritmInternal.limitL0,
                algaritmInternal.limitH1, algaritmInternal.limitL1,
                algaritmInternal.limitH2, algaritmInternal.limitL2,
                algaritmInternal.limitH3, algaritmInternal.limitL3,};
    }

    public Mat getCameraImage() {
        return algaritmInternal.getCamera();
    }

    public ArrayList<Integer> getLidarData() {
        return algaritmInternal.getLidar();
    }

    /*
        ports are from 1 to 8 included
    */
    public void setAngleServo(float value, int port) {
        algaritmInternal.setServoAngle(value, port - 1);
    }
}
