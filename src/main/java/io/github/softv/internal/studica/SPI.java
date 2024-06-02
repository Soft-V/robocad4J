package io.github.softv.internal.studica;

import io.github.softv.Common;
import io.github.softv.internal.LowLevelFuncad;
import io.github.softv.internal.studica.jni.LibHolder;
import io.github.softv.internal.studica.shared.VmxStatic;

import java.util.Arrays;

public class SPI {
    private static int toggler = 0;

    public static boolean stopThread = false;
    public static Thread th = null;

    public static void startSPI(){
        th = new Thread(SPI::spiLoop);
        th.setDaemon(true);
        th.start();
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

//                System.out.println("SPI");
//                System.out.println(Arrays.toString(txList));
//                System.out.println(Arrays.toString(rxList));

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
            VmxStatic.ultrasound1 = us1U / 100.0f;
            int us2U = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            VmxStatic.ultrasound2 = us2U / 100.0f;

            float power = ((data[8] & 0xff) << 8 | (data[7] & 0xff)) / 100.0f;
            Common.power = power;

            float yaw = (yawU / 100.0f) * (LowLevelFuncad.accessBit(data[9], 1) ? 1 : -1);
            calcYawUnlim(yaw, VmxStatic.yaw);
            VmxStatic.yaw = yaw;

            VmxStatic.flex0 = LowLevelFuncad.accessBit(data[9], 2);
            VmxStatic.flex1 = LowLevelFuncad.accessBit(data[9], 3);
            VmxStatic.flex2 = LowLevelFuncad.accessBit(data[9], 4);
            VmxStatic.flex3 = LowLevelFuncad.accessBit(data[9], 5);
            VmxStatic.flex4 = LowLevelFuncad.accessBit(data[9], 6);
        }
        else if ((data[0] & 0xff) == 2){
            VmxStatic.analog1 = (data[2] & 0xff) << 8 | (data[1] & 0xff);
            VmxStatic.analog2 = (data[4] & 0xff) << 8 | (data[3] & 0xff);
            VmxStatic.analog3 = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            VmxStatic.analog4 = (data[8] & 0xff) << 8 | (data[7] & 0xff);

            VmxStatic.flex5 = LowLevelFuncad.accessBit(data[9], 1);
            VmxStatic.flex6 = LowLevelFuncad.accessBit(data[9], 2);
            VmxStatic.flex7 = LowLevelFuncad.accessBit(data[9], 3);
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
        VmxStatic.yaw_unlim += delta;
    }
}
