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
    public void testMotorsSpeedDistribution() throws InterruptedException {
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
        // Добавлю после реализации ресетов, пока автоматически проходится
        Reporter.log("F");
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

//    @AfterSuite
//    private void generateReport() {
//        IReporter.
//    }
}
