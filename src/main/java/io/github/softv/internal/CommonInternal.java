package io.github.softv.internal;

import io.github.softv.internal.common.ConnectionSim;
import io.github.softv.internal.common.RobotConfiguration;
import io.github.softv.internal.common.Robot;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CommonInternal {
    public float speedMotor0 = 0;
    public float speedMotor1 = 0;
    public float speedMotor2 = 0;
    public float speedMotor3 = 0;
    public float speedMotor4 = 0;
    public float speedMotor5 = 0;
    public float speedMotor6 = 0;
    public float speedMotor7 = 0;

    public int encMotor0 = 0;
    public int encMotor1 = 0;
    public int encMotor2 = 0;
    public int encMotor3 = 0;
    public int encMotor4 = 0;
    public int encMotor5 = 0;
    public int encMotor6 = 0;
    public int encMotor7 = 0;

    public boolean button0 = false;
    public boolean button1 = false;
    public boolean button2 = false;
    public boolean button3 = false;
    public boolean button4 = false;
    public boolean button5 = false;
    public boolean button6 = false;
    public boolean button7 = false;

    public float yaw = 0;

    public float ultrasound1 = 0;
    public float ultrasound2 = 0;
    public float ultrasound3 = 0;
    public float ultrasound4 = 0;

    public int analog1 = 0;
    public int analog2 = 0;
    public int analog3 = 0;
    public int analog4 = 0;
    public int analog5 = 0;
    public int analog6 = 0;
    public int analog7 = 0;
    public int analog8 = 0;

    public boolean led0 = false;
    public boolean led1 = false;
    public boolean led2 = false;
    public boolean led3 = false;

    public float[] servoValues = new float[10];

    private final ConnectionSim connection;
    private final RobocadConnectionCommon robocadConnection;

    public CommonInternal(Robot robot, RobotConfiguration conf) {
        if (robot.onRealRobot) {
            throw new IllegalStateException("CommonRobot could only be used in simulator");
        }

        this.connection = new ConnectionSim(robot);
        this.robocadConnection = new RobocadConnectionCommon();
        this.robocadConnection.start(this.connection, robot, this);
    }

    public void stop() {
        if (this.robocadConnection != null) {
            this.robocadConnection.stop();
        }
        if (this.connection != null) {
            this.connection.stop();
        }
    }

    public Mat getCamera() {
        return this.connection.getCamera();
    }

    public void setServoAngle(float angle, int pin) {
        this.servoValues[pin] = (float)(0.000666 * angle + 0.05);
    }

    public void setServoPwm(float pwm, int pin) {
        this.servoValues[pin] = pwm;
    }

    public void disableServo(int pin) {
        this.servoValues[pin] = 0.0f;
    }

    private static class RobocadConnectionCommon {
        private Thread updateThread = null;
        private volatile boolean stopUpdateThread = false;

        private ConnectionSim connection;
        private CommonInternal robotInternal;

        public void start(ConnectionSim connection, Robot robot, CommonInternal robotInternal) {
            this.connection = connection;
            this.robotInternal = robotInternal;

            robot.power = 12;

            this.stopUpdateThread = false;
            this.updateThread = new Thread(this::update);
            this.updateThread.setDaemon(true);
            this.updateThread.start();
        }

        public void stop() {
            this.stopUpdateThread = true;
            if (updateThread != null) {
                try { updateThread.join(1000); }
                catch (InterruptedException ignored) {}
            }
        }

        private byte[] buildTx() {
            ByteBuffer bb = ByteBuffer.allocate(88);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putFloat(this.robotInternal.speedMotor0);
            bb.putFloat(this.robotInternal.speedMotor1);
            bb.putFloat(this.robotInternal.speedMotor2);
            bb.putFloat(this.robotInternal.speedMotor3);
            bb.putFloat(this.robotInternal.speedMotor4);
            bb.putFloat(this.robotInternal.speedMotor5);
            bb.putFloat(this.robotInternal.speedMotor6);
            bb.putFloat(this.robotInternal.speedMotor7);
            for (int i = 0; i < 10; i++) {
                bb.putFloat(this.robotInternal.servoValues[i]);
            }
            bb.putFloat(this.robotInternal.led0 ? 1f : 0f);
            bb.putFloat(this.robotInternal.led1 ? 1f : 0f);
            bb.putFloat(this.robotInternal.led2 ? 1f : 0f);
            bb.putFloat(this.robotInternal.led3 ? 1f : 0f);
            return bb.array();
        }

        private void update() {
            while (!stopUpdateThread) {
                this.connection.setData(buildTx());

                byte[] values = this.connection.getData();
                if (values.length >= 76) {
                    ByteBuffer bb = ByteBuffer.wrap(values);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    this.robotInternal.encMotor0 = bb.getInt();
                    this.robotInternal.encMotor1 = bb.getInt();
                    this.robotInternal.encMotor2 = bb.getInt();
                    this.robotInternal.encMotor3 = bb.getInt();
                    this.robotInternal.encMotor4 = bb.getInt();
                    this.robotInternal.encMotor5 = bb.getInt();
                    this.robotInternal.encMotor6 = bb.getInt();
                    this.robotInternal.encMotor7 = bb.getInt();
                    this.robotInternal.ultrasound1 = bb.getFloat();
                    this.robotInternal.ultrasound2 = bb.getFloat();
                    this.robotInternal.ultrasound3 = bb.getFloat();
                    this.robotInternal.ultrasound4 = bb.getFloat();
                    this.robotInternal.analog1 = bb.getShort() & 0xFFFF;
                    this.robotInternal.analog2 = bb.getShort() & 0xFFFF;
                    this.robotInternal.analog3 = bb.getShort() & 0xFFFF;
                    this.robotInternal.analog4 = bb.getShort() & 0xFFFF;
                    this.robotInternal.analog5 = bb.getShort() & 0xFFFF;
                    this.robotInternal.analog6 = bb.getShort() & 0xFFFF;
                    this.robotInternal.analog7 = bb.getShort() & 0xFFFF;
                    this.robotInternal.analog8 = bb.getShort() & 0xFFFF;
                    this.robotInternal.yaw = bb.getFloat();
                    this.robotInternal.button0 = bb.get() == 1;
                    this.robotInternal.button1 = bb.get() == 1;
                    this.robotInternal.button2 = bb.get() == 1;
                    this.robotInternal.button3 = bb.get() == 1;
                    this.robotInternal.button4 = bb.get() == 1;
                    this.robotInternal.button5 = bb.get() == 1;
                    this.robotInternal.button6 = bb.get() == 1;
                    this.robotInternal.button7 = bb.get() == 1;
                }

                try { Thread.sleep(4); }
                catch (InterruptedException ignored) {}
            }
        }
    }
}
