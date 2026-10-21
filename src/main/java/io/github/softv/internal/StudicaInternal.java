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

import io.github.softv.internal.common.PigpioPWM;
import java.util.Arrays;

public class StudicaInternal {
    private static final int[] HCDIO_CONST_ARRAY = { 4, 18, 17, 27, 23, 22, 24, 25, 7, 5 };
    private final RobotVmxTitan robot;
    private final PigpioPWM pwm;
    private final Object hcdioLock = new Object();
    private boolean stopped;

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
        pwm = robot.onRealRobot ? new PigpioPWM(conf.pigpioPipePath, conf.pigpioResponsePath,
                conf.hcdioPwmFrequency, conf.pigpioTimeout) : null;
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
        synchronized (hcdioLock) {
            stopped = true;
            Arrays.fill(hcdioValues, 0.0f);
        }
        IllegalStateException errors = new IllegalStateException("Robot stop failed");
        if (pwm != null) cleanup(() -> pwm.stop(HCDIO_CONST_ARRAY), errors);
        // Attempt all cleanups, stopping workers before their connection.
        if (titanCom != null) cleanup(titanCom::stop, errors);
        if (vmxSpi != null) cleanup(vmxSpi::stop, errors);
        if (robocadConnection != null) cleanup(robocadConnection::stop, errors);
        cleanup(connection::stop, errors);
        if (errors.getSuppressed().length != 0) throw errors;
    }

    private static void cleanup(Runnable action, IllegalStateException errors) {
        try { action.run(); }
        catch (RuntimeException error) { errors.addSuppressed(error); }
    }

    public Mat getCamera() {
        return connection.getCamera();
    }

    public void setServoAngle(float angle, int pin) {
        setHcdioDuty(0.000666 * angle + 0.05, pin);
    }

    public void setLedState(boolean state, int pin) {
        setHcdioDuty(state ? 0.2 : 0.0, pin);
    }

    public void setServoPWM(float duty, int pin) {
        setHcdioDuty(duty, pin);
    }

    public void disableServo(int pin) {
        setHcdioDuty(0.0, pin);
    }

    private void setHcdioDuty(double duty, int pin) {
        if (pin < 0 || pin >= HCDIO_CONST_ARRAY.length)
            throw new IllegalArgumentException("HCDIO port must be from 1 to 10");
        if (!(duty >= 0 && duty <= 1))
            throw new IllegalArgumentException("PWM duty cycle must be between 0 and 1");
        synchronized (hcdioLock) {
            if (stopped && duty != 0)
                throw new IllegalStateException("Robot has been stopped; HCDIO PWM cannot be enabled");
            if (pwm != null) pwm.setDutycycle(HCDIO_CONST_ARRAY[pin], duty);
            hcdioValues[pin] = (float)duty;
        }
    }
}
