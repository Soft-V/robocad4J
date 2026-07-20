package io.github.softv.common;

import io.github.softv.CommonRobot;
import io.github.softv.RobotVmxTitan;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;

public class TestRobotAtStartup {
    private CommonRobot robot;
    private SoftAssert softAssert;

    @BeforeSuite
    public void setUp() throws IOException, InterruptedException {
        softAssert = new SoftAssert();
        robot = new CommonRobot(false);
        Thread.sleep(100);
    }

    /**
     * analog1() - analog6() должны быть ненулевыми на всех роботах, на выводах analog7() и analog8() допускаются нулевые значения.
     */
    @Test(groups = "auto", priority = 1)
    public void checkAnalogOutputAfterRobotIsTurnedOn() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 500) {
            System.out.printf("Analog1: %d | Analog2: %d | Analog3: %d | Analog4: %d | Analog5: %d | Analog6: %d | Analog7: %d | Analog8: %d\n",
                               robot.getAnalog1(), robot.getAnalog2(), robot.getAnalog3(), robot.getAnalog4(), robot.getAnalog5(), robot.getAnalog6(), robot.getAnalog7(), robot.getAnalog8());
        }

        softAssert.assertNotEquals(robot.getAnalog1(), 0.0f);
        softAssert.assertNotEquals(robot.getAnalog2(), 0.0f);
        softAssert.assertNotEquals(robot.getAnalog3(), 0.0f);
        softAssert.assertNotEquals(robot.getAnalog4(), 0.0f);
        softAssert.assertNotEquals(robot.getAnalog5(), 0.0f);
        softAssert.assertNotEquals(robot.getAnalog6(), 0.0f);
        softAssert.assertNotEquals(robot.getAnalog7(), 0.0f);
        softAssert.assertNotEquals(robot.getAnalog8(), 0.0f);
    }

    /**
     * Ultrasound1() и Ultrasound2() должны быть ненулевыми, на Ultrasound3() и Ultrasound4() допускаются нули.
     */
    @Test(groups = "auto", priority = 2)
    public void checkUltrasonicOutputAfterRobotIsTurnedOn() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 500) {
            System.out.printf("Ultrasound1: %.2f | Ultrasound2: %.2f | Ultrasound3: %.2f | Ultrasound4: %.2f\n",
                               robot.getUltrasound1(), robot.getUltrasound2(), robot.getUltrasound3(), robot.getUltrasound4());
        }
        softAssert.assertNotEquals(robot.getUltrasound1(), 0.0f);
        softAssert.assertNotEquals(robot.getUltrasound2(), 0.0f);
        softAssert.assertNotEquals(robot.getUltrasound3(), 0.0f);
        softAssert.assertNotEquals(robot.getUltrasound4(), 0.0f);
    }

    /**
     * Yaw не должен быть нулевым, в противном случае тест пройден не будет.
     */
    @Test(groups = "auto", priority = 3)
    public void checkYawOutputAfterRobotIsTurnedOn() {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 500) {
            System.out.printf("Yaw: %f", robot.getYaw());
        }
        Assert.assertNotEquals(robot.getYaw(), 0.0f);
    }

    @Test(groups = "auto", priority = 4)
    public void checkIfResetYawWorks() {
        // Я думал ресет уже реализован. После добавления фичи закончу реализацию теста
    }

    /**
     * При запуске робота энкодеры должны быть нулевыми, в противном случае тест пройден не будет.
     */
    @Test(groups = "auto", priority = 5)
    public void checkIfEncodersAreZeroOnStartup() {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 500) {
            System.out.printf("Encoder0: %d | Encoder1: %d | Encoder2: %d | Encoder3: %d | Encoder4: %d | Encoder5: %d | Encoder6: %d | Encoder7: %d\n",
                                robot.getMotorEnc0(), robot.getMotorEnc1(), robot.getMotorEnc2(), robot.getMotorEnc3(), robot.getMotorEnc4(), robot.getMotorEnc5(), robot.getMotorEnc6(), robot.getMotorEnc7());
        }
        Assert.assertEquals(robot.getMotorEnc0(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc1(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc2(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc3(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc4(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc5(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc6(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc7(), 0.0f);
    }

    /**
     * При запуске все кнопки должны быть отжаты, в противном случае тест пройден не будет.
     */
    @Test(groups = "auto", priority = 6)
    public void checkDefaultButtonsValues() {
        Assert.assertFalse(robot.getButtons()[0]);
        Assert.assertFalse(robot.getButtons()[1]);
        Assert.assertFalse(robot.getButtons()[2]);
        Assert.assertFalse(robot.getButtons()[3]);
    }

    @AfterClass
    public void onEnd() {
        try{
            robot.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
