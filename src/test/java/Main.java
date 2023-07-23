import io.github.crackanddie.RobocadVMXTitan;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        RobocadVMXTitan robot = new RobocadVMXTitan(false);

        int seconds = LocalDateTime.now().getSecond();
        while (LocalDateTime.now().getSecond() - seconds < 10)
        {
            robot.setMotorSpeed0(-20);
            robot.setMotorSpeed1(20);
        }
        robot.setMotorSpeed0(0);
        robot.setMotorSpeed1(0);
        Thread.sleep(1000);
        robot.stop();
    }
}
