package io.github.softv.internal.studica;

import io.github.softv.internal.LowLevelFuncad;
import io.github.softv.internal.StudicaInternal;
import io.github.softv.internal.common.Robot;
import io.github.softv.internal.common.ConnectionReal;

import java.util.Arrays;

public class TitanCOMStudica {
    public boolean stopThread = false;
    public Thread th = null;

    private ConnectionReal connection;
    private Robot robot;
    private StudicaInternal robotInternal;
    private DefaultStudicaConfiguration conf;

    public void startCom(ConnectionReal connection, Robot robot, StudicaInternal robotInternal, DefaultStudicaConfiguration conf) {
        this.connection = connection;
        this.robot = robot;
        this.robotInternal = robotInternal;
        this.conf = conf;

        th = new Thread(this::comLoop);
        th.setDaemon(true);
        th.start();
    }

    public void stop() {
        this.stopThread = true;
        if (th != null) {
            try { th.join(); }
            catch (InterruptedException ignored){}
        }
    }

    private void comLoop() {
        try {
            int comResult = connection.comIni(conf.titanPort, conf.titanBaud);
            if (comResult != 0) {
                this.robot.writeLog("Failed to open COM");
                return;
            }

            long stTime = System.currentTimeMillis();
            long sendCountTime = System.currentTimeMillis();
            int commCounter = 0;
            while (!stopThread) {
                long txTime = System.currentTimeMillis();
                byte[] txList = setUpTxData();
                robot.robotInfo.txComTimeDev = (System.currentTimeMillis() - txTime) * 10; // TODO: get pure us

                byte[] rxList = connection.comRw(txList);

                long rxTime = System.currentTimeMillis();
                setUpRxData(rxList);
                robot.robotInfo.rxComTimeDev = (System.currentTimeMillis() - rxTime) * 10; // TODO: get pure us

                commCounter++;
                if (System.currentTimeMillis() - sendCountTime > 1000) {
                    sendCountTime = System.currentTimeMillis();
                    robot.robotInfo.comCountDev = commCounter;
                    commCounter = 0;
                }

                Thread.sleep(2);
                robot.robotInfo.comTimeDev = (System.currentTimeMillis() - stTime) * 10; // TODO: get pure us
                stTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            connection.comStop();
            robot.writeLog(e.getMessage());
            robot.writeLog(Arrays.toString(e.getStackTrace()));
        }
    }

    private void setUpRxData(byte[] data) {
        if ((data[0] & 0xff) == 1 && (data[24] & 0xff) == 111){
            int rawEnc0 = (data[2] & 0xff) << 8 | (data[1] & 0xff);
            int rawEnc1 = (data[4] & 0xff) << 8 | (data[3] & 0xff);
            int rawEnc2 = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            int rawEnc3 = (data[8] & 0xff) << 8 | (data[7] & 0xff);
            setUpEncoders(rawEnc0, rawEnc1, rawEnc2, rawEnc3);

            robotInternal.limitL0 = LowLevelFuncad.accessBit(data[9], 1);
            robotInternal.limitH0 = LowLevelFuncad.accessBit(data[9], 2);
            robotInternal.limitL1 = LowLevelFuncad.accessBit(data[9], 3);
            robotInternal.limitH1 = LowLevelFuncad.accessBit(data[9], 4);
            robotInternal.limitL2 = LowLevelFuncad.accessBit(data[9], 5);
            robotInternal.limitH2 = LowLevelFuncad.accessBit(data[9], 6);
            robotInternal.limitL3 = LowLevelFuncad.accessBit(data[10], 1);
            robotInternal.limitH3 = LowLevelFuncad.accessBit(data[10], 2);
        }
    }

    private byte[] setUpTxData() {
        byte[] data = new byte[48];

        data[0] = (byte)0x01;
        byte[] motorSpeeds;

        motorSpeeds = LowLevelFuncad.intTo4Bytes(Math.abs((int)(robotInternal.speedMotor0 / 100.0 * 65535)));
        data[2] = motorSpeeds[2];
        data[3] = motorSpeeds[3];

        motorSpeeds = LowLevelFuncad.intTo4Bytes(Math.abs((int)(robotInternal.speedMotor1 / 100.0 * 65535)));
        data[4] = motorSpeeds[2];
        data[5] = motorSpeeds[3];

        motorSpeeds = LowLevelFuncad.intTo4Bytes(Math.abs((int)(robotInternal.speedMotor2 / 100.0 * 65535)));
        data[6] = motorSpeeds[2];
        data[7] = motorSpeeds[3];

        motorSpeeds = LowLevelFuncad.intTo4Bytes(Math.abs((int)(robotInternal.speedMotor3 / 100.0 * 65535)));
        data[8] = motorSpeeds[2];
        data[9] = motorSpeeds[3];

        data[10] = (byte)Integer.parseInt("1" +
                (robotInternal.speedMotor0 >= 0 ? "1" : "0") +
                (robotInternal.speedMotor1 >= 0 ? "1" : "0") +
                (robotInternal.speedMotor2 >= 0 ? "1" : "0") +
                (robotInternal.speedMotor3 >= 0 ? "1" : "0") +
                "001", 2);

        data[11] = (byte)Integer.parseInt("1" + "0100001", 2);
        data[20] = (byte)0xde;

        return data;
    }

    private void setUpEncoders(int enc0, int enc1, int enc2, int enc3){
        robotInternal.encMotor0 -= getNormalDiff(enc0, robotInternal.rawEncMotor0);
        robotInternal.encMotor1 -= getNormalDiff(enc1, robotInternal.rawEncMotor1);
        robotInternal.encMotor2 -= getNormalDiff(enc2, robotInternal.rawEncMotor2);
        robotInternal.encMotor3 -= getNormalDiff(enc3, robotInternal.rawEncMotor3);

        robotInternal.rawEncMotor0 = enc0;
        robotInternal.rawEncMotor1 = enc1;
        robotInternal.rawEncMotor2 = enc2;
        robotInternal.rawEncMotor3 = enc3;
    }

    private static int getNormalDiff(int curr, int last){
        int diff = curr - last;
        if (diff > 30000){
            diff = -(last + (65535 - curr));
        }
        else if (diff < -30000){
            diff = curr + (65535 - last);
        }
        return diff;
    }
}
