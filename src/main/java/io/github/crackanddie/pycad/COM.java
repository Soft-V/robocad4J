package io.github.crackanddie.pycad;

import io.github.crackanddie.common.Funcad;
import io.github.crackanddie.jni.LibHolder;
import io.github.crackanddie.shufflecad.InfoHolder;

import java.util.Arrays;

public class COM {
    public static boolean stopThread = false;
    public static Thread th = null;

    public static void startCOM() {
        th = new Thread(COM::comLoop);
        th.setDaemon(true);
        th.start();
    }

    private static void comLoop() {
        try {
            LibHolder.getInstance().startUSB();
            long stTime = System.currentTimeMillis();
            long sendCountTime = System.currentTimeMillis();
            int commCounter = 0;
            while (!stopThread) {
                long txTime = System.currentTimeMillis();
                byte[] txList = setUpTxData();
                InfoHolder.txComTimeDev = String.valueOf(System.currentTimeMillis() - txTime);

                byte[] rxList = LibHolder.getInstance().readWriteUSB(txList, txList.length);

                long rxTime = System.currentTimeMillis();
                setUpRxData(rxList);
                InfoHolder.rxComTimeDev = String.valueOf(System.currentTimeMillis() - rxTime);

                commCounter++;
                if (System.currentTimeMillis() - sendCountTime > 1000) {
                    sendCountTime = System.currentTimeMillis();
                    InfoHolder.comCountDev = String.valueOf(commCounter);
                    commCounter = 0;
                }

                Thread.sleep(2);
                InfoHolder.comTimeDev = String.format("%.2f", (System.currentTimeMillis() - stTime) / 1000.0f);
                stTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            LibHolder.getInstance().stopUSB();
            InfoHolder.logger.writeMainLog(e.getMessage());
            InfoHolder.logger.writeMainLog(Arrays.toString(e.getStackTrace()));
        }
    }

    private static void setUpRxData(byte[] data){
        if ((data[0] & 0xff) == 1 && (data[24] & 0xff) == 111){
            int rawEnc0 = (data[2] & 0xff) << 8 | (data[1] & 0xff);
            int rawEnc1 = (data[4] & 0xff) << 8 | (data[3] & 0xff);
            int rawEnc2 = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            int rawEnc3 = (data[8] & 0xff) << 8 | (data[7] & 0xff);
            setUpEncoders(rawEnc0, rawEnc1, rawEnc2, rawEnc3);

            TitanStatic.limitL0 = Funcad.accessBit(data[9], 1);
            TitanStatic.limitH0 = Funcad.accessBit(data[9], 2);
            TitanStatic.limitL1 = Funcad.accessBit(data[9], 3);
            TitanStatic.limitH1 = Funcad.accessBit(data[9], 4);
            TitanStatic.limitL2 = Funcad.accessBit(data[9], 5);
            TitanStatic.limitH2 = Funcad.accessBit(data[9], 6);
            TitanStatic.limitL3 = Funcad.accessBit(data[10], 1);
            TitanStatic.limitH3 = Funcad.accessBit(data[10], 2);
        }
    }

    private static byte[] setUpTxData(){
        byte[] data = new byte[48];

        data[0] = (byte)0x01;
        byte[] motorSpeeds;

        motorSpeeds = Funcad.intTo4Bytes(Math.abs((int)(TitanStatic.speedMotor0 / 100.0 * 65535)));
        data[2] = motorSpeeds[2];
        data[3] = motorSpeeds[3];

        motorSpeeds = Funcad.intTo4Bytes(Math.abs((int)(TitanStatic.speedMotor1 / 100.0 * 65535)));
        data[4] = motorSpeeds[2];
        data[5] = motorSpeeds[3];

        motorSpeeds = Funcad.intTo4Bytes(Math.abs((int)(TitanStatic.speedMotor2 / 100.0 * 65535)));
        data[6] = motorSpeeds[2];
        data[7] = motorSpeeds[3];

        motorSpeeds = Funcad.intTo4Bytes(Math.abs((int)(TitanStatic.speedMotor3 / 100.0 * 65535)));
        data[8] = motorSpeeds[2];
        data[9] = motorSpeeds[3];

        data[10] = Byte.parseByte("1" +
                (TitanStatic.speedMotor0 >= 0 ? "1" : "0") +
                (TitanStatic.speedMotor1 >= 0 ? "1" : "0") +
                (TitanStatic.speedMotor2 >= 0 ? "1" : "0") +
                (TitanStatic.speedMotor3 >= 0 ? "1" : "0") +
                "001", 2);

        data[11] = Byte.parseByte("1" + "0100001", 2);
        data[20] = (byte)0xde;

        return data;
    }

    private static void setUpEncoders(int enc0, int enc1, int enc2, int enc3){
        TitanStatic.encMotor0 -= getNormalDiff(enc0, TitanStatic.rawEncMotor0);
        TitanStatic.encMotor1 -= getNormalDiff(enc1, TitanStatic.rawEncMotor1);
        TitanStatic.encMotor2 -= getNormalDiff(enc2, TitanStatic.rawEncMotor2);
        TitanStatic.encMotor3 -= getNormalDiff(enc3, TitanStatic.rawEncMotor3);

        TitanStatic.rawEncMotor0 = enc0;
        TitanStatic.rawEncMotor1 = enc1;
        TitanStatic.rawEncMotor2 = enc2;
        TitanStatic.rawEncMotor3 = enc3;
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

