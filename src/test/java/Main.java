import io.github.softv.RobocadVMXTitan;
import io.github.softv.robocadSim.Holder;
import io.github.softv.shufflecad.CameraVariable;
import io.github.softv.shufflecad.InfoHolder;
import io.github.softv.shufflecad.Shufflecad;

import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoField;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

        System.load("D:\\Programs\\opencv\\build\\java\\x64\\opencv_java440.dll");
        Holder.LOG_LEVEL = Holder.LOG_ALL;
        RobocadVMXTitan robot = new RobocadVMXTitan(false);

        CameraVariable cv = (CameraVariable)Shufflecad.addVar(new CameraVariable("test"));

        long millis = LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY);
        while (LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY) - millis < 20000)
        {
            robot.setMotorSpeed0(-20);
            robot.setMotorSpeed1(20);
            if (robot.getCameraImage() != null)
                cv.setMat(robot.getCameraImage());
            System.out.println(LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY) - millis);
            Thread.sleep(1000);
            InfoHolder.power = String.valueOf(12 + (LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY) - millis) / 10000.f);
        }
        robot.setMotorSpeed0(0);
        robot.setMotorSpeed1(0);
        Thread.sleep(1000);
        robot.stop();
    }
}
