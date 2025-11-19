package io.github.softv.internal;

import io.github.softv.RobotAlgaritm;
import io.github.softv.internal.algaritm.DefaultAlgaritmConfiguration;
import io.github.softv.internal.algaritm.TitanCOMAlgaritm;
import io.github.softv.internal.algaritm.VmxSPIAlgaritm;
import io.github.softv.internal.common.*;
import io.github.softv.internal.studica.TitanCOMStudica;
import io.github.softv.internal.studica.VmxSPIStudica;
import org.opencv.core.Mat;

import java.util.ArrayList;

public class AlgaritmInternal {
    private final RobotAlgaritm robot;

    // from Titan
    public float speedMotor0 = 0;
    public float speedMotor1 = 0;
    public float speedMotor2 = 0;
    public float speedMotor3 = 0;

    public int encMotor0 = 0;
    public int encMotor1 = 0;
    public int encMotor2 = 0;
    public int encMotor3 = 0;

    public boolean limitL0 = false;
    public boolean limitH0 = false;
    public boolean limitL1 = false;
    public boolean limitH1 = false;
    public boolean limitL2 = false;
    public boolean limitH2 = false;
    public boolean limitL3 = false;
    public boolean limitH3 = false;

    public float additionalServo1 = 0;
    public float additionalServo2 = 0;

    public boolean isStep1Busy = false;
    public boolean isStep2Busy = false;
    public int stepMotor1Steps = 0;
    public int stepMotor2Steps = 0;
    public int stepMotor1StepsPerS = 0;
    public int stepMotor2StepsPerS = 0;
    public boolean stepMotor1Direction = false;
    public boolean stepMotor2Direction = false;

    public boolean usePid = false;
    public float pPid = 0.14f;
    public float iPid = 0.1f;
    public float dPid = 0.0f;

    // from vmx
    public float yaw = 0;
    public float yaw_unlim = 0;
    public float pitch = 0;
    public float pitch_unlim = 0;
    public float roll = 0;
    public float roll_unlim = 0;

    public float ultrasound1 = 0;
    public float ultrasound2 = 0;
    public float ultrasound3 = 0;
    public float ultrasound4 = 0;

    public float analog1 = 0;
    public float analog2 = 0;
    public float analog3 = 0;
    public float analog4 = 0;
    public float analog5 = 0;
    public float analog6 = 0;
    public float analog7 = 0;
    public float analog8 = 0;

    public Float[] servoAngles = new Float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

    private final ConnectionBase connection;
    private TitanCOMAlgaritm titanCom;
    private VmxSPIAlgaritm vmxSpi;

    public AlgaritmInternal(RobotAlgaritm robot, DefaultAlgaritmConfiguration conf) {
        this.robot = robot;
        if (!robot.onRealRobot) {
            // TODO: sim conn
            this.connection = null;
        }
        else {
            UpdaterRepka updater = new UpdaterRepka(this.robot);
            this.connection = new ConnectionReal(this.robot, updater, conf);
            this.titanCom = new TitanCOMAlgaritm();
            this.titanCom.startCom((ConnectionReal)this.connection, this.robot, this, conf);
            this.vmxSpi = new VmxSPIAlgaritm();
            this.vmxSpi.startSpi((ConnectionReal)this.connection, this.robot, this, conf);
        }
    }

    public void stop() {
        this.connection.stop();
        this.robot.closeLog();
        if (!robot.onRealRobot) {
            // TODO: sim conn
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

    public ArrayList<Integer> getLidar() {
        return connection.getLidar();
    }

    public void setServoAngle(float angle, int pin){
        this.servoAngles[pin] = angle;
    }

    public void stepMotorMove(int num, int steps, int stepsPerS, boolean direction){
        if (num == 1) {
            this.stepMotor1Steps = steps;
            this.stepMotor1StepsPerS = stepsPerS;
            this.stepMotor1Direction = direction;
        }
        else if (num == 2) {
            this.stepMotor2Steps = steps;
            this.stepMotor2StepsPerS = stepsPerS;
            this.stepMotor2Direction = direction;
        }
    }
}
