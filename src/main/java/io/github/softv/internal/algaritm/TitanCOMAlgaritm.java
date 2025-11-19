package io.github.softv.internal.algaritm;

import io.github.softv.internal.AlgaritmInternal;
import io.github.softv.internal.LowLevelFuncad;
import io.github.softv.internal.StudicaInternal;
import io.github.softv.internal.common.ConnectionReal;
import io.github.softv.internal.common.Robot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class TitanCOMAlgaritm {
    public boolean stopThread = false;
    public Thread th = null;

    private ConnectionReal connection;
    private Robot robot;
    private AlgaritmInternal robotInternal;
    private DefaultAlgaritmConfiguration conf;

    public void startCom(ConnectionReal connection, Robot robot, AlgaritmInternal robotInternal, DefaultAlgaritmConfiguration conf) {
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
        if ((data[0] & 0xff) == 1 && (data[40] & 0xff) == 222){
            robotInternal.encMotor0 = ((data[4] & 0xff) << 24) | ((data[3] & 0xff) << 16) | ((data[2] & 0xff) << 8) | (data[1] & 0xff);
            robotInternal.encMotor1 = ((data[8] & 0xff) << 24) | ((data[7] & 0xff) << 16) | ((data[6] & 0xff) << 8) | (data[5] & 0xff);
            robotInternal.encMotor2 = ((data[12] & 0xff) << 24) | ((data[11] & 0xff) << 16) | ((data[10] & 0xff) << 8) | (data[9] & 0xff);
            robotInternal.encMotor3 = ((data[16] & 0xff) << 24) | ((data[15] & 0xff) << 16) | ((data[14] & 0xff) << 8) | (data[13] & 0xff);

            robotInternal.limitL3 = LowLevelFuncad.accessBit(data[17], 0);
            robotInternal.limitL0 = LowLevelFuncad.accessBit(data[17], 1);
            robotInternal.limitH0 = LowLevelFuncad.accessBit(data[17], 2);
            robotInternal.limitL1 = LowLevelFuncad.accessBit(data[17], 3);
            robotInternal.limitH1 = LowLevelFuncad.accessBit(data[17], 4);
            robotInternal.limitL2 = LowLevelFuncad.accessBit(data[17], 5);
            robotInternal.limitH2 = LowLevelFuncad.accessBit(data[17], 6);
            robotInternal.limitH3 = LowLevelFuncad.accessBit(data[17], 7);

            robotInternal.isStep1Busy = (data[18] != 0);
            robotInternal.isStep2Busy = (data[19] != 0);
        }
    }

    private byte[] setUpTxData() {
        byte[] data = new byte[48];

        data[0] = (byte)0x01;

        data[1] = (byte)(int)LowLevelFuncad.clamp(robotInternal.speedMotor0, -100, 100);
        data[2] = (byte)(int)LowLevelFuncad.clamp(robotInternal.speedMotor1, -100, 100);
        data[3] = (byte)(int)LowLevelFuncad.clamp(robotInternal.speedMotor2, -100, 100);
        data[4] = (byte)(int)LowLevelFuncad.clamp(robotInternal.speedMotor3, -100, 100);

        data[5] = (byte)Integer.parseInt("11" +
                (robotInternal.stepMotor1Direction ? "1" : "0") +
                (robotInternal.stepMotor2Direction ? "1" : "0") +
                (robotInternal.usePid ? "1" : "0") +
                "001", 2);

        data[6] = (byte)(int)robotInternal.additionalServo1;
        data[7] = (byte)(int)robotInternal.additionalServo2;

        byte[] byteDataToSend;
        byteDataToSend = LowLevelFuncad.intTo4Bytes(Math.abs(robotInternal.stepMotor1Steps));
        data[8] = byteDataToSend[0];
        data[9] = byteDataToSend[1];
        data[10] = byteDataToSend[2];
        data[11] = byteDataToSend[3];
        byteDataToSend = LowLevelFuncad.intTo4Bytes(Math.abs(robotInternal.stepMotor2Steps));
        data[12] = byteDataToSend[0];
        data[13] = byteDataToSend[1];
        data[14] = byteDataToSend[2];
        data[15] = byteDataToSend[3];

        byteDataToSend = LowLevelFuncad.intTo4Bytes(Math.abs(robotInternal.stepMotor1StepsPerS));
        data[16] = byteDataToSend[0];
        data[17] = byteDataToSend[1];
        data[18] = byteDataToSend[2];
        data[19] = byteDataToSend[3];
        byteDataToSend = LowLevelFuncad.intTo4Bytes(Math.abs(robotInternal.stepMotor2StepsPerS));
        data[20] = byteDataToSend[0];
        data[21] = byteDataToSend[1];
        data[22] = byteDataToSend[2];
        data[23] = byteDataToSend[3];

        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(robotInternal.pPid);
        byte[] bbArr = bb.array();
        data[24] = bbArr[0];
        data[25] = bbArr[1];
        data[26] = bbArr[2];
        data[27] = bbArr[3];
        ByteBuffer bb2 = ByteBuffer.allocate(4);
        bb2.order(ByteOrder.LITTLE_ENDIAN);
        bb2.putFloat(robotInternal.iPid);
        byte[] bbArr2 = bb2.array();
        data[28] = bbArr2[0];
        data[29] = bbArr2[1];
        data[30] = bbArr2[2];
        data[31] = bbArr2[3];
        ByteBuffer bb3 = ByteBuffer.allocate(4);
        bb3.order(ByteOrder.LITTLE_ENDIAN);
        bb3.putFloat(robotInternal.dPid);
        byte[] bbArr3 = bb3.array();
        data[32] = bbArr3[0];
        data[33] = bbArr3[1];
        data[34] = bbArr3[2];
        data[35] = bbArr3[3];

        data[40] = (byte)0xde;

        return data;
    }
}
