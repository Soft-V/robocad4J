package io.github.softv;

import io.github.softv.internal.LoggerInside;
import io.github.softv.internal.StudicaInternal;
import io.github.softv.internal.common.*;
import io.github.softv.shufflecad.Shufflecad;
import org.opencv.core.Mat;

import java.io.IOException;

public class RobotVmxTitan extends Robot {
    private final StudicaInternal studicaInternal;

    public RobotVmxTitan() throws IOException { this(true, null); }
    public RobotVmxTitan(boolean isRealRobot) throws IOException { this(isRealRobot, null); }
    public RobotVmxTitan(boolean isRealRobot, DefaultStudicaConfiguration conf) throws IOException {
        if (conf == null) {
            conf = new DefaultStudicaConfiguration();
        }
        init(isRealRobot, conf); // fucking java shite
        studicaInternal = new StudicaInternal(this, conf);

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
        studicaInternal.stop();
    }

    public void setMotorSpeed0(float speed) {
        studicaInternal.speedMotor0 = speed;
    }

    public void setMotorSpeed1(float speed) {
        studicaInternal.speedMotor1 = speed;
    }

    public void setMotorSpeed2(float speed) {
        studicaInternal.speedMotor2 = speed;
    }

    public void setMotorSpeed3(float speed) {
        studicaInternal.speedMotor3 = speed;
    }

    public float getMotorEnc0() {
        return studicaInternal.encMotor0;
    }

    public float getMotorEnc1() {
        return studicaInternal.encMotor1;
    }

    public float getMotorEnc2() {
        return studicaInternal.encMotor2;
    }

    public float getMotorEnc3() {
        return studicaInternal.encMotor3;
    }

    public float getYaw() {
        return studicaInternal.yaw;
    }

    public float getUltrasound1() {
        return studicaInternal.ultrasound1;
    }

    public float getUltrasound2() {
        return studicaInternal.ultrasound2;
    }

    public float getAnalog1() {
        return studicaInternal.analog1;
    }

    public float getAnalog2() {
        return studicaInternal.analog2;
    }

    public float getAnalog3() {
        return studicaInternal.analog3;
    }

    public float getAnalog4() {
        return studicaInternal.analog4;
    }

    public boolean[] getTitanLimits() {
        return new boolean[] { studicaInternal.limitH0, studicaInternal.limitL0,
                studicaInternal.limitH1, studicaInternal.limitL1,
                studicaInternal.limitH2, studicaInternal.limitL2,
                studicaInternal.limitH3, studicaInternal.limitL3,};
    }

    public boolean[] getVmxFlex() {
        return new boolean[] { studicaInternal.flex0, studicaInternal.flex1, studicaInternal.flex2, studicaInternal.flex3,
                studicaInternal.flex4, studicaInternal.flex5, studicaInternal.flex6, studicaInternal.flex7 };
    }

    public Mat getCameraImage() {
        return studicaInternal.getCamera();
    }

    /*
        ports are from 1 to 10 included
    */
    public void setAngleHCDIO(float value, int port) {
        studicaInternal.setServoAngle(value, port - 1);
    }

    public void setPwmHCDIO(float value, int port) {
        studicaInternal.setServoPWM(value, port - 1);
    }

    public void setBoolHCDIO(boolean value, int port) {
        studicaInternal.setLedState(value, port - 1);
    }
}
