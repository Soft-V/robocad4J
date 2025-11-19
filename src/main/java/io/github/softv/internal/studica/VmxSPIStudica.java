package io.github.softv.internal.studica;

import io.github.softv.internal.LowLevelFuncad;
import io.github.softv.internal.StudicaInternal;
import io.github.softv.internal.common.ConnectionReal;
import io.github.softv.internal.common.DefaultStudicaConfiguration;
import io.github.softv.internal.common.Robot;

import java.util.Arrays;

public class VmxSPIStudica {
    private static int toggler = 0;

    public boolean stopThread = false;
    public Thread th = null;

    private ConnectionReal connection;
    private Robot robot;
    private StudicaInternal robotInternal;
    private DefaultStudicaConfiguration conf;

    public void startSpi(ConnectionReal connection, Robot robot, StudicaInternal robotInternal, DefaultStudicaConfiguration conf) {
        this.connection = connection;
        this.robot = robot;
        this.robotInternal = robotInternal;
        this.conf = conf;

        th = new Thread(this::spiLoop);
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

    private void spiLoop(){
        try{
            int spiResult = connection.spiIni(conf.vmxPort, conf.vmxChannel, conf.vmxSpeed, conf.vmxMode);
            if (spiResult != 0) {
                this.robot.writeLog("Failed to open SPI");
                return;
            }

            long stTime = System.currentTimeMillis();
            long sendCountTime = System.currentTimeMillis();
            int commCounter = 0;
            while (!stopThread){
                long txTime = System.currentTimeMillis();
                byte[] txList = setUpTxData();
                this.robot.robotInfo.txSpiTimeDev = (System.currentTimeMillis() - txTime) * 10;

                byte[] rxList = connection.spiRw(txList);

                long rxTime = System.currentTimeMillis();
                setUpRxData(rxList);
                this.robot.robotInfo.rxSpiTimeDev = (System.currentTimeMillis() - rxTime) * 10;

                commCounter++;
                if (System.currentTimeMillis() - sendCountTime > 1000){
                    sendCountTime = System.currentTimeMillis();
                    this.robot.robotInfo.spiCountDev = commCounter;
                    commCounter = 0;
                }

                Thread.sleep(2);
                this.robot.robotInfo.spiTimeDev = (System.currentTimeMillis() - stTime) * 10;
                stTime = System.currentTimeMillis();
            }
        }
        catch (Exception e){
            connection.spiStop();
            robot.writeLog(e.getMessage());
            robot.writeLog(Arrays.toString(e.getStackTrace()));
        }
    }

    private void setUpRxData(byte[] data){
        if ((data[0] & 0xff) == 1){
            int yawU = (data[2] & 0xff) << 8 | (data[1] & 0xff);
            int us1U = (data[4] & 0xff) << 8 | (data[3] & 0xff);
            this.robotInternal.ultrasound1 = us1U / 100.0f;
            int us2U = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            this.robotInternal.ultrasound2 = us2U / 100.0f;

            float power = ((data[8] & 0xff) << 8 | (data[7] & 0xff)) / 100.0f;
            this.robot.power = power;

            float yaw = (yawU / 100.0f) * (LowLevelFuncad.accessBit(data[9], 1) ? 1 : -1);
            calcYawUnlim(yaw, this.robotInternal.yaw);
            this.robotInternal.yaw = yaw;

            this.robotInternal.flex0 = LowLevelFuncad.accessBit(data[9], 2);
            this.robotInternal.flex1 = LowLevelFuncad.accessBit(data[9], 3);
            this.robotInternal.flex2 = LowLevelFuncad.accessBit(data[9], 4);
            this.robotInternal.flex3 = LowLevelFuncad.accessBit(data[9], 5);
            this.robotInternal.flex4 = LowLevelFuncad.accessBit(data[9], 6);
        }
        else if ((data[0] & 0xff) == 2){
            this.robotInternal.analog1 = (data[2] & 0xff) << 8 | (data[1] & 0xff);
            this.robotInternal.analog2 = (data[4] & 0xff) << 8 | (data[3] & 0xff);
            this.robotInternal.analog3 = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            this.robotInternal.analog4 = (data[8] & 0xff) << 8 | (data[7] & 0xff);

            this.robotInternal.flex5 = LowLevelFuncad.accessBit(data[9], 1);
            this.robotInternal.flex6 = LowLevelFuncad.accessBit(data[9], 2);
            this.robotInternal.flex7 = LowLevelFuncad.accessBit(data[9], 3);
        }
    }

    private byte[] setUpTxData(){
        byte[] data = new byte[10];
        if (toggler == 0){
            data[0] = (byte)0x01;
            data[9] = (byte)0xde;
        }
        return data;
    }

    private void calcYawUnlim(float newYaw, float oldYaw){
        float delta = newYaw - oldYaw;
        if (delta < -180){
            delta = 180 - oldYaw;
            delta += 180 + newYaw;
        }
        else if (delta > 180){
            delta = (180 + oldYaw) * -1;
            delta += (180 - newYaw) * -1;
        }
        this.robotInternal.yaw_unlim += delta;
    }
}
