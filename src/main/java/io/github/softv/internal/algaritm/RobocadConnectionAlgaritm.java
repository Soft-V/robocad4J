package io.github.softv.internal.algaritm;

import io.github.softv.internal.AlgaritmInternal;
import io.github.softv.internal.common.ConnectionSim;
import io.github.softv.internal.common.Robot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RobocadConnectionAlgaritm {
    private Thread updateThread = null;
    private boolean stopUpdateThread = false;

    private ConnectionSim connection;
    private Robot robot;
    private AlgaritmInternal robotInternal;

    public void start(ConnectionSim connection, Robot robot, AlgaritmInternal robotInternal) {
        this.connection = connection;
        this.robot = robot;
        this.robotInternal = robotInternal;

        this.robot.power = 12; // TODO: from robocad

        this.stopUpdateThread = false;
        this.updateThread = new Thread(this::update);
        this.updateThread.setDaemon(true);
        this.updateThread.start();
    }

    public void stop() {
        this.stopUpdateThread = true;
        if (updateThread != null){
            try { updateThread.join(); }
            catch (InterruptedException ignored){}
        }
    }

    private void setData(List<Float> lst) {
        ByteBuffer bb = ByteBuffer.allocate(lst.size() * 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (Float i: lst) {
            bb.putFloat(i);
        }
        this.connection.setData(bb.array());
    }

    private byte[] getData() {
        return this.connection.getData();
    }

    private void update() {
        while (!stopUpdateThread) {
            List<Float> lst = new ArrayList<>(Arrays.asList(this.robotInternal.speedMotor0, this.robotInternal.speedMotor1,
                    this.robotInternal.speedMotor2, this.robotInternal.speedMotor3));
            for (int i = 0; i <8; i++) lst.add(this.robotInternal.servoAngles[i] * 0.0011111f + 0.05f);
            lst.add(this.robotInternal.additionalServo1);
            lst.add(this.robotInternal.additionalServo2);
            lst.add((float)this.robotInternal.stepMotor1Steps);
            lst.add((float)this.robotInternal.stepMotor2Steps);
            lst.add((float)this.robotInternal.stepMotor1StepsPerS);
            lst.add((float)this.robotInternal.stepMotor2StepsPerS);
            lst.add((float)(this.robotInternal.stepMotor1Direction ? 1 : 0));
            lst.add((float)(this.robotInternal.stepMotor2Direction ? 1 : 0));
            lst.add((float)(this.robotInternal.usePid ? 1 : 0));
            lst.add(this.robotInternal.pPid);
            lst.add(this.robotInternal.iPid);
            lst.add(this.robotInternal.dPid);
            lst.add((float)(this.robotInternal.outputs[0] ? 1 : 0));
            lst.add((float)(this.robotInternal.outputs[1] ? 1 : 0));
            lst.add((float)(this.robotInternal.outputs[2] ? 1 : 0));
            lst.add((float)(this.robotInternal.outputs[3] ? 1 : 0));
            this.setData(lst);

            byte[] values = this.getData();
            if (values.length == 74){
                ByteBuffer bb = ByteBuffer.wrap(values);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                this.robotInternal.encMotor0 = bb.getInt();
                this.robotInternal.encMotor1 = bb.getInt();
                this.robotInternal.encMotor2 = bb.getInt();
                this.robotInternal.encMotor3 = bb.getInt();
                this.robotInternal.ultrasound1 = bb.getFloat();
                this.robotInternal.ultrasound2 = bb.getFloat();
                this.robotInternal.ultrasound3 = bb.getFloat();
                this.robotInternal.ultrasound4 = bb.getFloat();
                this.robotInternal.analog1 = bb.getShort();
                this.robotInternal.analog2 = bb.getShort();
                this.robotInternal.analog3 = bb.getShort();
                this.robotInternal.analog4 = bb.getShort();
                this.robotInternal.analog5 = bb.getShort();
                this.robotInternal.analog6 = bb.getShort();
                this.robotInternal.analog7 = bb.getShort();
                this.robotInternal.analog8 = bb.getShort();
                this.robotInternal.yaw = bb.getFloat();
                this.robotInternal.pitch = bb.getFloat();
                this.robotInternal.roll = bb.getFloat();

                this.robotInternal.limitH0 = bb.get() == 1;
                this.robotInternal.limitL0 = bb.get() == 1;
                this.robotInternal.limitH1 = bb.get() == 1;
                this.robotInternal.limitL1 = bb.get() == 1;
                this.robotInternal.limitH2 = bb.get() == 1;
                this.robotInternal.limitL2 = bb.get() == 1;
                this.robotInternal.limitH3 = bb.get() == 1;
                this.robotInternal.limitL3 = bb.get() == 1;

                this.robotInternal.inputs[0] = bb.get() == 1;
                this.robotInternal.inputs[1] = bb.get() == 1;
                this.robotInternal.inputs[2] = bb.get() == 1;
                this.robotInternal.inputs[3] = bb.get() == 1;

                this.robotInternal.isStep1Busy = bb.get() == 1;
                this.robotInternal.isStep2Busy = bb.get() == 1;
            }

            try { Thread.sleep(4); }
            catch (InterruptedException ignored){}
        }
    }
}
