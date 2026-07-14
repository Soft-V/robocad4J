package io.github.softv.algaritm;

import io.github.softv.RobotAlgaritm;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;

public class TestRobotOnStartup {
    private RobotAlgaritm robot;
    private SoftAssert softAssert;

    @BeforeSuite
    public void setUp() throws IOException, InterruptedException {
        softAssert = new SoftAssert();
        robot = new RobotAlgaritm(false);
        Thread.sleep(100);
    }

    /**
     * analog1() - analog6() должны быть ненулевыми на всех роботах, на выводах analog7() и analog8() допускаются нулевые значения.
     */
    @Test(groups = "auto", priority = 1)
    public void checkAnalogOutputAfterRobotIsTurnedOn() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 2000) {
            System.out.printf("Analog1: %.2f | Analog2: %.2f | Analog3: %.2f | Analog4: %.2f | Analog5: %.2f | Analog6: %.2f | Analog7: %.2f | Analog8: %.2f\n",
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
        while (System.currentTimeMillis() - startTime < 2000) {
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
        while(System.currentTimeMillis() - startTime < 2000) {
            System.out.println("Yaw: " + robot.getYaw());
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
        while(System.currentTimeMillis() - startTime < 2000) {
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
        Assert.assertFalse(robot.getInputs()[0]);
        Assert.assertFalse(robot.getInputs()[1]);
        Assert.assertFalse(robot.getInputs()[2]);
        Assert.assertFalse(robot.getInputs()[3]);
    }

    /**
     * Проверка всех кнопок на виртуальной панели на работоспособность. Необходимо за 30 секунд хотя бы 1 раз прожать все 4 кнопки: EMS, Start, Reset и Stop на виртуальной панели для успешного завершения теста.
     * Параллельно желательно проверять, что нажатая в симуляторе и отображаемая в консоли кнопки правильно соотносятся, но и без этого тест пройдется.
     */
    @Test(groups = "manual", priority = 7)
    public void checkButtonsResponse() {
        boolean emsPressedOnce = false;
        boolean startButtonPressedOnce = false;
        boolean resetButtonPressedOnce = false;
        boolean stopButtonPressedOnce = false;

        long startTime = System.currentTimeMillis();
        while((!emsPressedOnce || !startButtonPressedOnce || !resetButtonPressedOnce || !stopButtonPressedOnce) && System.currentTimeMillis() - startTime < 30_000) {
            if(robot.getInputs()[0])
                emsPressedOnce = true;

            if (robot.getInputs()[1])
                startButtonPressedOnce = true;

            if(robot.getInputs()[2])
                resetButtonPressedOnce = true;

            if(robot.getInputs()[3])
                stopButtonPressedOnce = true;

            System.out.printf("EMS: %b   |   Start: %b   |   Reset: %b   |   Stop: %b\n", emsPressedOnce, startButtonPressedOnce, resetButtonPressedOnce, stopButtonPressedOnce);
        }

        Assert.assertTrue(emsPressedOnce);
        Assert.assertTrue(startButtonPressedOnce);
        Assert.assertTrue(resetButtonPressedOnce);
        Assert.assertTrue(stopButtonPressedOnce);
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
