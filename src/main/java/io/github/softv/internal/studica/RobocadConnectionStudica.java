package io.github.softv.internal.studica;

import io.github.softv.internal.StudicaInternal;
import io.github.softv.internal.common.ConnectionSim;
import io.github.softv.internal.common.Robot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RobocadConnectionStudica {
    private Thread updateThread = null;
    private boolean stopUpdateThread = false;

    private ConnectionSim connection;
    private Robot robot;
    private StudicaInternal robotInternal;

    public void start(ConnectionSim connection, Robot robot, StudicaInternal robotInternal) {
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
            lst.addAll(Arrays.asList(this.robotInternal.hcdioValues));
            this.setData(lst);

            byte[] values = this.getData();
            if (values.length == 52){
                ByteBuffer bb = ByteBuffer.wrap(values);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                this.robotInternal.encMotor0 = bb.getInt();
                this.robotInternal.encMotor1 = bb.getInt();
                this.robotInternal.encMotor2 = bb.getInt();
                this.robotInternal.encMotor3 = bb.getInt();
                this.robotInternal.ultrasound1 = bb.getFloat();
                this.robotInternal.ultrasound2 = bb.getFloat();
                this.robotInternal.analog1 = bb.getShort();
                this.robotInternal.analog2 = bb.getShort();
                this.robotInternal.analog3 = bb.getShort();
                this.robotInternal.analog4 = bb.getShort();
                this.robotInternal.yaw = bb.getFloat();

                this.robotInternal.limitH0 = bb.get() == 1;
                this.robotInternal.limitL0 = bb.get() == 1;
                this.robotInternal.limitH1 = bb.get() == 1;
                this.robotInternal.limitL1 = bb.get() == 1;
                this.robotInternal.limitH2 = bb.get() == 1;
                this.robotInternal.limitL2 = bb.get() == 1;
                this.robotInternal.limitH3 = bb.get() == 1;
                this.robotInternal.limitL3 = bb.get() == 1;

                this.robotInternal.flex0 = bb.get() == 1;
                this.robotInternal.flex1 = bb.get() == 1;
                this.robotInternal.flex2 = bb.get() == 1;
                this.robotInternal.flex3 = bb.get() == 1;
                this.robotInternal.flex4 = bb.get() == 1;
                this.robotInternal.flex5 = bb.get() == 1;
                this.robotInternal.flex6 = bb.get() == 1;
                this.robotInternal.flex7 = bb.get() == 1;
            }

            try { Thread.sleep(4); }
            catch (InterruptedException ignored){}
        }
    }
}
