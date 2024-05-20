package io.github.softv.internal.studica;

import io.github.softv.Common;
import io.github.softv.internal.LowLevelFuncad;
import io.github.softv.internal.studica.jni.LibHolder;
import io.github.softv.internal.studica.shared.VMXStatic;

import java.util.Arrays;

public class SPI {
    private static int toggler = 0;

    public static boolean stopThread = false;
    public static Thread thread = null;

    public static void startSPI(){
        thread = new Thread(SPI::spiLoop);
        thread.setDaemon(true);
        thread.start();
    }

    private static void spiLoop(){
        try{
            LibHolder.getInstance().startSPI();
            long stTime = System.currentTimeMillis();
            long sendCountTime = System.currentTimeMillis();
            int commCounter = 0;
            while (!stopThread){
                long txTime = System.currentTimeMillis();
                byte[] txList = setUpTxData();
                Common.txSpiTimeDev = System.currentTimeMillis() - txTime;

                byte[] rxList = LibHolder.getInstance().readWriteSPI(txList, txList.length);

                long rxTime = System.currentTimeMillis();
                setUpRxData(rxList);
                Common.rxSpiTimeDev = System.currentTimeMillis() - rxTime;

                commCounter++;
                if (System.currentTimeMillis() - sendCountTime > 1000){
                    sendCountTime = System.currentTimeMillis();
                    Common.spiCountDev = commCounter;
                    commCounter = 0;
                }

                Thread.sleep(2);
                Common.spiTimeDev = (System.currentTimeMillis() - stTime) / 1000.0f;
                stTime = System.currentTimeMillis();
            }
        }
        catch (Exception e){
            LibHolder.getInstance().stopSPI();
            Common.logger.writeMainLog(e.getMessage());
            Common.logger.writeMainLog(Arrays.toString(e.getStackTrace()));
        }
    }

    private static void setUpRxData(byte[] data){
        if ((data[0] & 0xff) == 1){
            int yawU = (data[2] & 0xff) << 8 | (data[1] & 0xff);
            int us1U = (data[4] & 0xff) << 8 | (data[3] & 0xff);
            VMXStatic.ultrasound1 = us1U / 100.0f;
            int us2U = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            VMXStatic.ultrasound2 = us2U / 100.0f;

            float power = ((data[6] & 0xff) << 8 | (data[5] & 0xff)) / 100.0f;
            Common.power = power;

            float yaw = (yawU / 100.0f) * (LowLevelFuncad.accessBit(data[9], 1) ? 1 : -1);
            calcYawUnlim(yaw, VMXStatic.yaw);
            VMXStatic.yaw = yaw;

            VMXStatic.flex0 = LowLevelFuncad.accessBit(data[9], 2);
            VMXStatic.flex1 = LowLevelFuncad.accessBit(data[9], 3);
            VMXStatic.flex2 = LowLevelFuncad.accessBit(data[9], 4);
            VMXStatic.flex3 = LowLevelFuncad.accessBit(data[9], 5);
            VMXStatic.flex4 = LowLevelFuncad.accessBit(data[9], 6);
        }
        else if ((data[0] & 0xff) == 2){
            VMXStatic.analog1 = (data[2] & 0xff) << 8 | (data[1] & 0xff);
            VMXStatic.analog2 = (data[4] & 0xff) << 8 | (data[3] & 0xff);
            VMXStatic.analog3 = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            VMXStatic.analog4 = (data[8] & 0xff) << 8 | (data[7] & 0xff);

            VMXStatic.flex5 = LowLevelFuncad.accessBit(data[9], 1);
            VMXStatic.flex6 = LowLevelFuncad.accessBit(data[9], 2);
            VMXStatic.flex7 = LowLevelFuncad.accessBit(data[9], 3);
        }
    }

    private static byte[] setUpTxData(){
        byte[] data = new byte[10];
        if (toggler == 0){
            data[0] = (byte)0x01;
            data[9] = (byte)0xde;
        }
        return data;
    }

    private static void calcYawUnlim(float newYaw, float oldYaw){
        float delta = newYaw - oldYaw;
        if (delta < -180){
            delta = 180 - oldYaw;
            delta += 180 + newYaw;
        }
        else if (delta > 180){
            delta = (180 + oldYaw) * -1;
            delta += (180 - newYaw) * -1;
        }
        VMXStatic.yaw_unlim += delta;
    }
}
