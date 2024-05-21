package io.github.softv.internal.studica.shared;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class VmxStatic {
    private static final int[] HCDIO_CONST_ARRAY = { 4, 18, 17, 27, 23, 22, 24, 25, 7, 5 };

    public static float yaw = 0;
    public static float yaw_unlim = 0;
    public static boolean calib_imu = false;

    public static float ultrasound1 = 0;
    public static float ultrasound2 = 0;

    public static float analog1 = 0;
    public static float analog2 = 0;
    public static float analog3 = 0;
    public static float analog4 = 0;

    public static boolean flex0 = false;
    public static boolean flex1 = false;
    public static boolean flex2 = false;
    public static boolean flex3 = false;
    public static boolean flex4 = false;
    public static boolean flex5 = false;
    public static boolean flex6 = false;
    public static boolean flex7 = false;

    public static Float[] hcdioValues = new Float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

    public static void setServoAngle(float angle, int pin){
        double dut = 0.000666 * angle + 0.05;
        hcdioValues[pin] = (float)dut;
        echoToFile(HCDIO_CONST_ARRAY[pin] + "=" + dut);
    }

    public static void setLedState(boolean state, int pin){
        double dut = state ? 0.2 : 0.0;
        hcdioValues[pin] = (float)dut;
        echoToFile(HCDIO_CONST_ARRAY[pin] + "=" + dut);
    }

    public static void setServoPWM(float dut, int pin){
        hcdioValues[pin] = (float)dut;
        echoToFile(HCDIO_CONST_ARRAY[pin] + "=" + dut);
    }

    public static void disableServo(int pin){
        hcdioValues[pin] = 0.0f;
        echoToFile(HCDIO_CONST_ARRAY[pin] + "=" + "0.0");
    }

    private static void echoToFile(String val){
        final String file = "/dev/pi-blaster";
        try (PrintWriter out = new PrintWriter(new FileOutputStream(file), true)) {
            out.println(val);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
