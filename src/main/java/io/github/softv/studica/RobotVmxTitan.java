package io.github.softv.studica;

import io.github.softv.Common;
import io.github.softv.internal.studica.*;
import io.github.softv.internal.studica.shared.TitanStatic;
import io.github.softv.internal.studica.shared.VmxStatic;
import io.github.softv.shufflecad.InfoHolder;
import io.github.softv.shufflecad.Shufflecad;
import org.opencv.core.Mat;

import java.io.IOException;

public class RobotVmxTitan {
    private final ConnectionBase connection;

    public RobotVmxTitan() throws IOException { this(true); }
    public RobotVmxTitan(boolean isRealRobot) throws IOException {
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

        if (!isRealRobot) {
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

    public boolean[] getVmxFlex() {
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
}
