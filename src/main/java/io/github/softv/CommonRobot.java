package io.github.softv;

import io.github.softv.internal.CommonInternal;
import io.github.softv.internal.common.RobotConfiguration;
import io.github.softv.internal.common.Robot;
import org.opencv.core.Mat;

import java.io.IOException;

public class CommonRobot extends Robot {
    private final CommonInternal commonInternal;
    private float resetedYawVal = 0;

    public CommonRobot() throws IOException { this(false, null); }
    public CommonRobot(boolean isRealRobot) throws IOException { this(isRealRobot, null); }
    public CommonRobot(boolean isRealRobot, RobotConfiguration conf) throws IOException {
        if (conf == null) {
            conf = new RobotConfiguration();
        }
        init(isRealRobot, conf);
        commonInternal = new CommonInternal(this, conf);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            writeLog("Program stopping");
            this.stop();
        }));
    }

    public void stop() {
        commonInternal.stop();
        writeLog("Program stopped");
    }

    public void setMotorSpeed0(float speed) { commonInternal.speedMotor0 = speed; }
    public void setMotorSpeed1(float speed) { commonInternal.speedMotor1 = speed; }
    public void setMotorSpeed2(float speed) { commonInternal.speedMotor2 = speed; }
    public void setMotorSpeed3(float speed) { commonInternal.speedMotor3 = speed; }
    public void setMotorSpeed4(float speed) { commonInternal.speedMotor4 = speed; }
    public void setMotorSpeed5(float speed) { commonInternal.speedMotor5 = speed; }
    public void setMotorSpeed6(float speed) { commonInternal.speedMotor6 = speed; }
    public void setMotorSpeed7(float speed) { commonInternal.speedMotor7 = speed; }

    public int getMotorEnc0() { return commonInternal.encMotor0; }
    public int getMotorEnc1() { return commonInternal.encMotor1; }
    public int getMotorEnc2() { return commonInternal.encMotor2; }
    public int getMotorEnc3() { return commonInternal.encMotor3; }
    public int getMotorEnc4() { return commonInternal.encMotor4; }
    public int getMotorEnc5() { return commonInternal.encMotor5; }
    public int getMotorEnc6() { return commonInternal.encMotor6; }
    public int getMotorEnc7() { return commonInternal.encMotor7; }

    public float getYaw() {
        return rerangeAngle180(commonInternal.yaw - resetedYawVal);
    }

    public void resetYaw() {
        resetedYawVal = getYaw();
    }

    public float getUltrasound1() { return commonInternal.ultrasound1; }
    public float getUltrasound2() { return commonInternal.ultrasound2; }
    public float getUltrasound3() { return commonInternal.ultrasound3; }
    public float getUltrasound4() { return commonInternal.ultrasound4; }

    public int getAnalog1() { return commonInternal.analog1; }
    public int getAnalog2() { return commonInternal.analog2; }
    public int getAnalog3() { return commonInternal.analog3; }
    public int getAnalog4() { return commonInternal.analog4; }
    public int getAnalog5() { return commonInternal.analog5; }
    public int getAnalog6() { return commonInternal.analog6; }
    public int getAnalog7() { return commonInternal.analog7; }
    public int getAnalog8() { return commonInternal.analog8; }

    public boolean[] getButtons() {
        return new boolean[] { commonInternal.button0, commonInternal.button1,
                commonInternal.button2, commonInternal.button3,
                commonInternal.button4, commonInternal.button5,
                commonInternal.button6, commonInternal.button7 };
    }

    public void setLed0(boolean value) { commonInternal.led0 = value; }
    public void setLed1(boolean value) { commonInternal.led1 = value; }
    public void setLed2(boolean value) { commonInternal.led2 = value; }
    public void setLed3(boolean value) { commonInternal.led3 = value; }

    public boolean getLed0() { return commonInternal.led0; }
    public boolean getLed1() { return commonInternal.led1; }
    public boolean getLed2() { return commonInternal.led2; }
    public boolean getLed3() { return commonInternal.led3; }

    public Mat getCameraImage() {
        return commonInternal.getCamera();
    }

    public void setAngleServo(float value, int port) {
        commonInternal.setServoAngle(value, port - 1);
    }

    public void setPwmServo(float value, int port) {
        commonInternal.setServoPwm(value, port - 1);
    }

    private float rerangeAngle180(float angle) {
        while (angle > 180)
            angle -= 360;
        while (angle < -180)
            angle += 360;
        return angle;
    }
}
