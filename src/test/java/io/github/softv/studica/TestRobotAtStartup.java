package io.github.softv.studica;

import io.github.softv.RobotVmxTitan;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;

public class TestRobotAtStartup {
    private RobotVmxTitan robot;
    private SoftAssert softAssert;

    @BeforeSuite
    public void setUp() throws IOException, InterruptedException {
        softAssert = new SoftAssert();
        robot = new RobotVmxTitan(false);
        Thread.sleep(100);
    }

    /**
     * analog1() - analog6() должны быть ненулевыми на всех роботах, на выводах analog7() и analog8() допускаются нулевые значения.
     */
    @Test(groups = "auto", priority = 1)
    public void checkAnalogOutputAfterRobotIsTurnedOn() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 500) {
            System.out.printf("Analog1: %.2f | Analog2: %.2f | Analog3: %.2f | Analog4: %.2f\n",
                               robot.getAnalog1(), robot.getAnalog2(), robot.getAnalog3(), robot.getAnalog4());
        }

        softAssert.assertNotEquals(robot.getAnalog1(), 0.0f);
        softAssert.assertNotEquals(robot.getAnalog2(), 0.0f);
        softAssert.assertNotEquals(robot.getAnalog3(), 0.0f);
        softAssert.assertNotEquals(robot.getAnalog4(), 0.0f);
    }

    /**
     * Ultrasound1() и Ultrasound2() должны быть ненулевыми, на Ultrasound3() и Ultrasound4() допускаются нули.
     */
    @Test(groups = "auto", priority = 2)
    public void checkUltrasonicOutputAfterRobotIsTurnedOn() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 500) {
            System.out.printf("Ultrasound1: %.2f | Ultrasound2: %.2f\n",
                               robot.getUltrasound1(), robot.getUltrasound2());
        }
        softAssert.assertNotEquals(robot.getUltrasound1(), 0.0f);
        softAssert.assertNotEquals(robot.getUltrasound2(), 0.0f);

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
            System.out.printf("Encoder0: %f | Encoder1: %f | Encoder2: %f | Encoder3: %f\n", robot.getMotorEnc0(), robot.getMotorEnc1(), robot.getMotorEnc2(), robot.getMotorEnc3());
        }
        Assert.assertEquals(robot.getMotorEnc0(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc1(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc2(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc3(), 0.0f);

    }

    /**
     * При запуске все кнопки должны быть отжаты, в противном случае тест пройден не будет.
     */
    @Test(groups = "auto", priority = 6)
    public void checkDefaultButtonsValues() {
        Assert.assertFalse(robot.getVmxFlex()[0]);
        Assert.assertFalse(robot.getVmxFlex()[1]);
        Assert.assertFalse(robot.getVmxFlex()[2]);
        Assert.assertFalse(robot.getVmxFlex()[3]);
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
