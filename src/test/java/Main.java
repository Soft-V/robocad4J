import io.github.softv.Common;
import io.github.softv.shufflecad.CameraVariable;
import io.github.softv.shufflecad.Shufflecad;
import io.github.softv.studica.RobotVmxTitan;

import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoField;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

        System.load("D:\\Programs\\opencv\\build\\java\\x64\\opencv_java440.dll");
        Common.LOG_LEVEL = Common.LOG_ALL;
        RobotVmxTitan robot = new RobotVmxTitan(false);

        CameraVariable cv = (CameraVariable)Shufflecad.addVar(new CameraVariable("test"));

        long millis = LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY);
        while (LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY) - millis < 20000)
        {
            robot.setMotorSpeed0(20);
            robot.setMotorSpeed1(-20);
            if (robot.getCameraImage() != null)
                cv.setMat(robot.getCameraImage());
            System.out.println(LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY) - millis);
            // System.out.println(robot.getAnalog1());
            System.out.println(robot.getVmxFlex()[0]);
            System.out.println(robot.getVmxFlex()[1]);
            System.out.println(robot.getVmxFlex()[2]);
            System.out.println(robot.getVmxFlex()[3]);
            Thread.sleep(1000);
            Common.power = 12 + (LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY) - millis) / 10000.f;
        }
        robot.setMotorSpeed0(0);
        robot.setMotorSpeed1(0);
        Thread.sleep(1000);
        robot.stop();
    }
}
