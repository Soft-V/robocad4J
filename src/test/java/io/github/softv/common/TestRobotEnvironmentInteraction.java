package io.github.softv.common;

import io.github.softv.CommonRobot;
import io.github.softv.RobotVmxTitan;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestRobotEnvironmentInteraction {
    private CommonRobot robot;

    @BeforeClass
    public void setUp() throws IOException, InterruptedException {
        robot = new CommonRobot(false);

        Thread.sleep(100);
    }

    /**
     * Робот должен проехать прямо, вправо и повернуться по часовой. Тест пройдется автоматически,
     * надо будет просто смотреть, что всё выполняется правильно.
     * @throws InterruptedException
     */
    @Test(groups = "auto", priority = 1)
    public void testMotorsSpeedDistribution() throws InterruptedException {
        setAxisSpeed(30,0);
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 2000) {  }

        setAxisSpeed(0,30);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 3000) {  }

        setAxisSpeed(0,0);
    }

    /**
     * При запуске энкодеры базы должны быть ненулевыми, в противном случае тест пройден не будет.
     */
    @Test(groups = "auto", priority = 2)
    public void checkBaseEncodersValuesAfterRobotMoved() {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 500) {
            System.out.printf("Encoder0: %d | Encoder1: %d | Encoder2: %d | Encoder3: %d | Encoder4: %d | Encoder5: %d\n",
                    robot.getMotorEnc0(), robot.getMotorEnc1(), robot.getMotorEnc2(), robot.getMotorEnc3(), robot.getMotorEnc4(), robot.getMotorEnc5());
        }
        Assert.assertNotEquals(robot.getMotorEnc0(), 0.0f);
        Assert.assertNotEquals(robot.getMotorEnc1(), 0.0f);
        Assert.assertNotEquals(robot.getMotorEnc2(), 0.0f);
        Assert.assertNotEquals(robot.getMotorEnc3(), 0.0f);
        Assert.assertNotEquals(robot.getMotorEnc4(), 0.0f);
        Assert.assertNotEquals(robot.getMotorEnc5(), 0.0f);
    }

    @Test(groups = "auto", priority = 3)
    public void checkIfBaseEncodersResetWorks() {
        robot.resetMotorEnc0();
        robot.resetMotorEnc1();
        robot.resetMotorEnc2();
        robot.resetMotorEnc3();
        robot.resetMotorEnc4();
        robot.resetMotorEnc5();
        Assert.assertEquals(robot.getMotorEnc0(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc1(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc2(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc3(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc4(), 0.0f);
        Assert.assertEquals(robot.getMotorEnc5(), 0.0f);
    }

    @Test(groups = "auto", priority = 4)
    public void checkElevatorArmServoInteraction() {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 500) { }

        robot.setAngleServo(300, 1);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 2500) { }

        robot.setAngleServo(150, 1);
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

        robot.setAngleServo(300, 2);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 2500) { }

        robot.setAngleServo(150, 2);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1500) { }

        robot.setAngleServo(0, 2);
        startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1500) { }
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
        
        while(!emsPressedOnce || !startButtonPressedOnce || !resetButtonPressedOnce || !stopButtonPressedOnce) {
            if(robot.getButtons()[0])
                emsPressedOnce = true;

            if (robot.getButtons()[1])
                startButtonPressedOnce = true;

            if(robot.getButtons()[2])
                resetButtonPressedOnce = true;

            if(robot.getButtons()[3])
                stopButtonPressedOnce = true;

            System.out.printf("EMS: %b   |   Start: %b   |   Reset: %b   |   Stop: %b\n", emsPressedOnce, startButtonPressedOnce, resetButtonPressedOnce, stopButtonPressedOnce);
        }

        Assert.assertTrue(emsPressedOnce);
        Assert.assertTrue(startButtonPressedOnce);
        Assert.assertTrue(resetButtonPressedOnce);
        Assert.assertTrue(stopButtonPressedOnce);
    }

    /**
     * @param x скорость вперед и назад
     * @param z скорость по часовой и против часовой
     * @throws InterruptedException
     */
    private void setAxisSpeed(float x, float z) throws InterruptedException {
        float right1 = -x + z;
        float right2 = -x + z;
        float right3 = -x + z;

        float left1 = x + z;
        float left2 = x + z;
        float left3 = x + z;

        robot.setMotorSpeed0(left1);
        robot.setMotorSpeed1(left2);
        robot.setMotorSpeed2(left3);

        robot.setMotorSpeed3(right1);
        robot.setMotorSpeed4(right2);
        robot.setMotorSpeed5(right3);
        Thread.sleep(20);
    }

}
