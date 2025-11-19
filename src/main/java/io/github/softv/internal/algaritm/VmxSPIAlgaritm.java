package io.github.softv.internal.algaritm;

import io.github.softv.internal.AlgaritmInternal;
import io.github.softv.internal.LowLevelFuncad;
import io.github.softv.internal.StudicaInternal;
import io.github.softv.internal.common.ConnectionReal;
import io.github.softv.internal.common.Robot;
import io.github.softv.internal.studica.DefaultStudicaConfiguration;

import java.util.Arrays;

public class VmxSPIAlgaritm {
    private static int toggler = 0;

    public boolean stopThread = false;
    public Thread th = null;

    private ConnectionReal connection;
    private Robot robot;
    private AlgaritmInternal robotInternal;
    private DefaultAlgaritmConfiguration conf;

    public void startSpi(ConnectionReal connection, Robot robot, AlgaritmInternal robotInternal, DefaultAlgaritmConfiguration conf) {
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
            this.robotInternal.analog1 = (data[2] & 0xff) << 8 | (data[1] & 0xff);
            this.robotInternal.analog2 = (data[4] & 0xff) << 8 | (data[3] & 0xff);
            this.robotInternal.analog3 = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            this.robotInternal.analog4 = (data[8] & 0xff) << 8 | (data[7] & 0xff);
            this.robotInternal.analog5 = (data[10] & 0xff) << 8 | (data[9] & 0xff);
            this.robotInternal.analog6 = (data[12] & 0xff) << 8 | (data[11] & 0xff);
            this.robotInternal.analog7 = (data[14] & 0xff) << 8 | (data[13] & 0xff);
        }
        else if ((data[0] & 0xff) == 2){
            this.robotInternal.analog8 = (data[2] & 0xff) << 8 | (data[1] & 0xff);

            int us1U = (data[4] & 0xff) << 8 | (data[3] & 0xff);
            this.robotInternal.ultrasound1 = us1U / 100.0f;
            int us2U = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            this.robotInternal.ultrasound2 = us2U / 100.0f;
            int us3U = (data[8] & 0xff) << 8 | (data[7] & 0xff);
            this.robotInternal.ultrasound2 = us3U / 100.0f;
            int us4U = (data[10] & 0xff) << 8 | (data[9] & 0xff);
            this.robotInternal.ultrasound2 = us4U / 100.0f;
        }
        else if ((data[0] & 0xff) == 3){
            int yawU = (data[2] & 0xff) << 8 | (data[1] & 0xff);
            float yaw = (yawU / 100.0f) * (LowLevelFuncad.accessBit(data[7], 1) ? 1 : -1);
            this.robotInternal.yaw_unlim += calcAngleUnlim(yaw, this.robotInternal.yaw);
            this.robotInternal.yaw = yaw;

            int pitchU = (data[4] & 0xff) << 8 | (data[3] & 0xff);
            float pitch = (pitchU / 100.0f) * (LowLevelFuncad.accessBit(data[7], 2) ? 1 : -1);
            this.robotInternal.pitch_unlim += calcAngleUnlim(pitch, this.robotInternal.pitch);
            this.robotInternal.pitch = pitch;

            int rollU = (data[6] & 0xff) << 8 | (data[5] & 0xff);
            float roll = (rollU / 100.0f) * (LowLevelFuncad.accessBit(data[7], 3) ? 1 : -1);
            this.robotInternal.roll_unlim += calcAngleUnlim(roll, this.robotInternal.roll);
            this.robotInternal.roll = roll;

            float power = ((data[8] & 0xff) << 8 | (data[7] & 0xff)) / 100.0f;
            this.robot.power = power;
        }
    }

    private byte[] setUpTxData(){
        byte[] data = new byte[16];
        if (toggler == 0){
            data[0] = (byte)0x01;

            data[1] = (byte)(int)(float)robotInternal.servoAngles[0];
            data[2] = (byte)(int)(float)robotInternal.servoAngles[1];
            data[3] = (byte)(int)(float)robotInternal.servoAngles[2];
            data[4] = (byte)(int)(float)robotInternal.servoAngles[3];
            data[5] = (byte)(int)(float)robotInternal.servoAngles[4];
            data[6] = (byte)(int)(float)robotInternal.servoAngles[5];
            data[7] = (byte)(int)(float)robotInternal.servoAngles[6];
            data[8] = (byte)(int)(float)robotInternal.servoAngles[7];
        }
        return data;
    }

    private static float calcAngleUnlim(float newYaw, float oldYaw){
        float delta = newYaw - oldYaw;
        if (delta < -180){
            delta = 180 - oldYaw;
            delta += 180 + newYaw;
        }
        else if (delta > 180){
            delta = (180 + oldYaw) * -1;
            delta += (180 - newYaw) * -1;
        }
        return delta;
    }
}
