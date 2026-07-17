import io.github.softv.RobotAlgaritm;
import io.github.softv.RobotVmxTitan;
import io.github.softv.shufflecad.CameraVariable;
import io.github.softv.shufflecad.Shufflecad;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

public class TestAlga {
    public static void test() throws IOException, InterruptedException {

        RobotAlgaritm robot = new RobotAlgaritm(false);
        Shufflecad shufflecad = new Shufflecad(robot);
        Thread.sleep(1000);
        long millis = LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY);
        while (LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY) - millis < 3000)
        {
            System.out.println(robot.getMotorEnc0() + " | " + robot.getMotorEnc1());
            robot.setMotorSpeed0(-50);
            robot.setMotorSpeed1(50);
        }
        robot.resetMotorEnc0();
        robot.resetMotorEnc1();

        millis = LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY);

        while (LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY) - millis < 3000)
        {
            System.out.println(robot.getMotorEnc0() + " | " + robot.getMotorEnc1());
        }
        robot.setMotorSpeed0(0);
        robot.setMotorSpeed1(0);
        Thread.sleep(1000);
        robot.stop();
    }
}
