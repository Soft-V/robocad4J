package io.github.softv.algaritm;

import io.github.softv.RobotAlgaritm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.IReporter;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestRobotEnvironmentInteraction {
    private static final Logger log = LoggerFactory.getLogger(TestRobotEnvironmentInteraction.class);
    private RobotAlgaritm robot;
    private IReporter ireporter;
    @BeforeClass
    public void setUp() throws IOException, InterruptedException {
        robot = new RobotAlgaritm(false);

        Thread.sleep(100);
    }

    /**
     * Робот должен проехать прямо, вправо и повернуться по часовой. Тест пройдется автоматически,
     * надо будет просто смотреть, что всё выполняется правильно.
     * @throws InterruptedException
     */
    @Test(groups = "auto", priority = 1)
    public void testAxisSpeedDistribution() throws InterruptedException {
        setAxisSpeed(30,0,0);
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 2000) {  }

        setAxisSpeed(0,30,0);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 2000) {  }

        setAxisSpeed(0,0,30);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 3000) {  }

        setAxisSpeed(0,0,0);
    }

    /**
     * При запуске энкодеры базы должны быть ненулевыми, в противном случае тест пройден не будет.
     */
    @Test(groups = "auto", priority = 2)
    public void checkBaseEncodersValuesAfterRobotMoved() {
        Reporter.log("checkBaseEncodersValuesAfterRobotMoved started", 2);
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 2000) {
            System.out.printf("Encoder0: %f | Encoder1: %f | Encoder2: %f\n", robot.getMotorEnc0(), robot.getMotorEnc1(), robot.getMotorEnc2());
        }
        Assert.assertNotEquals(robot.getMotorEnc0(), 0.0f);
        Assert.assertNotEquals(robot.getMotorEnc1(), 0.0f);
        Assert.assertNotEquals(robot.getMotorEnc2(), 0.0f);
    }

    @Test(groups = "auto", priority = 3)
    public void checkIfEncodersResetWorks() {
        robot.resetMotorEnc0();
        robot.resetMotorEnc1();
        robot.resetMotorEnc2();
        robot.resetMotorEnc3();
        Assert.assertEquals(robot.getMotorEnc0(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc1(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc2(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc3(), 0.0f);
    }

    @Test(groups = "auto", priority = 4)
    public void checkElevatorServoInteraction() {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 500) { }

        robot.setAngleServo(180, 1);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 2500) { }

        robot.setAngleServo(90, 1);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1500) { }

        robot.setAngleServo(0, 1);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1500) { }
    }

    @Test(groups = "auto", priority = 5)
    public void checkGripperServoInteraction() {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 500) { }

        robot.setAngleServo(180, 2);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 2500) { }

        robot.setAngleServo(90, 2);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1500) { }

        robot.setAngleServo(0, 2);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1500) { }
    }

    @Test(groups = "auto", priority = 6)
    public void checkIfLEDsAreWorkingAndAttachedCorrectly() {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 500) { }

        startTime = System.currentTimeMillis();
        robot.getOutputs()[0] = true; // Зеленый
        robot.getOutputs()[1] = false;
        while(System.currentTimeMillis() - startTime < 500) { }


        startTime = System.currentTimeMillis();
        robot.getOutputs()[1] = true; // Красный
        robot.getOutputs()[0] = false;
        while(System.currentTimeMillis() - startTime < 1000) { }
    }

    /**
     * Проверка всех кнопок на виртуальной панели на работоспособность. Необходимо за 30 секунд хотя бы 1 раз прожать все 4 кнопки: EMS, Start, Reset и Stop на виртуальной панели для успешного завершения теста.
     * Параллельно желательно проверять, что нажатая в симуляторе и отображаемая в консоли кнопки правильно соотносятся, но и без этого тест пройдется.
     */
    @Test(groups = "manual", priority = 7, timeOut = 30_000)
    public void checkButtonsResponse() {
        boolean emsPressedOnce = false;
        boolean startButtonPressedOnce = false;
        boolean resetButtonPressedOnce = false;
        boolean stopButtonPressedOnce = false;

        long startTime = System.currentTimeMillis();
        while(!emsPressedOnce || !startButtonPressedOnce || !resetButtonPressedOnce || !stopButtonPressedOnce) {
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



    /**
     *
     * @param x скорость вперед и назад
     * @param y скорость вправо и влево
     * @param z скорость по часовой и против часовой
     * @throws InterruptedException
     */
    private void setAxisSpeed(float x, float y, float z) throws InterruptedException {
        float right = -x + y / 2 + z;
        float left = x + y / 2 + z;
        float back = -y + z;

        robot.setMotorSpeed0(left);
        robot.setMotorSpeed1(right);
        robot.setMotorSpeed2(back);
        Thread.sleep(20);
    }
}
