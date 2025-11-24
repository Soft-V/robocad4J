package io.github.softv.internal;

import io.github.softv.RobotVmxTitan;
import io.github.softv.internal.studica.DefaultStudicaConfiguration;
import io.github.softv.internal.common.ConnectionBase;
import io.github.softv.internal.common.ConnectionReal;
import io.github.softv.internal.common.UpdaterRpi;
import io.github.softv.internal.common.ConnectionSim;
import io.github.softv.internal.studica.RobocadConnectionStudica;
import io.github.softv.internal.studica.TitanCOMStudica;
import io.github.softv.internal.studica.VmxSPIStudica;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class StudicaInternal {
    private static final int[] HCDIO_CONST_ARRAY = { 4, 18, 17, 27, 23, 22, 24, 25, 7, 5 };
    private final RobotVmxTitan robot;

    // from Titan
    public float speedMotor0 = 0;
    public float speedMotor1 = 0;
    public float speedMotor2 = 0;
    public float speedMotor3 = 0;

    public int encMotor0 = 0;
    public int encMotor1 = 0;
    public int encMotor2 = 0;
    public int encMotor3 = 0;

    public int rawEncMotor0 = 0;
    public int rawEncMotor1 = 0;
    public int rawEncMotor2 = 0;
    public int rawEncMotor3 = 0;

    public boolean limitL0 = false;
    public boolean limitH0 = false;
    public boolean limitL1 = false;
    public boolean limitH1 = false;
    public boolean limitL2 = false;
    public boolean limitH2 = false;
    public boolean limitL3 = false;
    public boolean limitH3 = false;

    // from vmx
    public float yaw = 0;
    public float yaw_unlim = 0;
    public boolean calib_imu = false;

    public float ultrasound1 = 0;
    public float ultrasound2 = 0;

    public float analog1 = 0;
    public float analog2 = 0;
    public float analog3 = 0;
    public float analog4 = 0;

    public boolean flex0 = false;
    public boolean flex1 = false;
    public boolean flex2 = false;
    public boolean flex3 = false;
    public boolean flex4 = false;
    public boolean flex5 = false;
    public boolean flex6 = false;
    public boolean flex7 = false;
    public Float[] hcdioValues = new Float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

    private final ConnectionBase connection;
    private RobocadConnectionStudica robocadConnection;
    private TitanCOMStudica titanCom;
    private VmxSPIStudica vmxSpi;

    public StudicaInternal(RobotVmxTitan robot, DefaultStudicaConfiguration conf) {
        this.robot = robot;
        if (!robot.onRealRobot) {
            this.connection = new ConnectionSim(this.robot);
            this.robocadConnection = new RobocadConnectionStudica();
            this.robocadConnection.start((ConnectionSim)this.connection, this.robot, this);
        }
        else {
            UpdaterRpi updater = new UpdaterRpi(this.robot);
            this.connection = new ConnectionReal(this.robot, updater, conf);
            this.titanCom = new TitanCOMStudica();
            this.titanCom.startCom((ConnectionReal)this.connection, this.robot, this, conf);
            this.vmxSpi = new VmxSPIStudica();
            this.vmxSpi.startSpi((ConnectionReal)this.connection, this.robot, this, conf);
        }
    }

    public void stop() {
        this.connection.stop();
        if (!robot.onRealRobot) {
            if (this.robocadConnection != null) {
                this.robocadConnection.stop();
            }
        }
        else {
            if (this.titanCom != null){
                this.titanCom.stop();
            }
            if (this.vmxSpi != null){
                this.vmxSpi.stop();
            }
        }
    }

    public Mat getCamera() {
        return connection.getCamera();
    }

    public void setServoAngle(float angle, int pin){
        double dut = 0.000666 * angle + 0.05;
        hcdioValues[pin] = (float)dut;
        echoToFile(HCDIO_CONST_ARRAY[pin] + "=" + dut);
    }

    public void setLedState(boolean state, int pin){
        double dut = state ? 0.2 : 0.0;
        hcdioValues[pin] = (float)dut;
        echoToFile(HCDIO_CONST_ARRAY[pin] + "=" + dut);
    }

    public void setServoPWM(float dut, int pin){
        hcdioValues[pin] = (float)dut;
        echoToFile(HCDIO_CONST_ARRAY[pin] + "=" + dut);
    }

    public void disableServo(int pin){
        hcdioValues[pin] = 0.0f;
        echoToFile(HCDIO_CONST_ARRAY[pin] + "=" + "0.0");
    }

    private void echoToFile(String val){
        if (!this.robot.onRealRobot)
            return;
        final String file = "/dev/pi-blaster";
        try (PrintWriter out = new PrintWriter(new FileOutputStream(file), true)) {
            out.println(val);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
