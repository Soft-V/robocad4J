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
        System.load("C:\\opencv\\build\\java\\x64\\opencv_java440.dll");
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        RobotAlgaritm robot = new RobotAlgaritm(true);
        Shufflecad shufflecad = new Shufflecad(robot);

        CameraVariable cv = (CameraVariable)shufflecad.addVar(new CameraVariable("test"));

        long millis = LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY);
        while (LocalTime.now(ZoneOffset.UTC).get(ChronoField.MILLI_OF_DAY) - millis < 200000)
        {
            robot.setMotorSpeed0(20);
            robot.setMotorSpeed1(20);
            if (robot.getCameraImage() != null)
                cv.setMat(robot.getCameraImage());
        }
        robot.setMotorSpeed0(0);
        robot.setMotorSpeed1(0);
        Thread.sleep(1000);
        robot.stop();
    }
}
