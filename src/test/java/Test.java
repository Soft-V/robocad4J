import io.github.softv.RobotAlgaritm;
import io.github.softv.RobotVmxTitan;
import io.github.softv.shufflecad.CameraVariable;
import io.github.softv.shufflecad.JoystickData;
import io.github.softv.shufflecad.Shufflecad;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {
        RobotAlgaritm robot = new RobotAlgaritm(false);
        Shufflecad shufflecad = new Shufflecad(robot);

        Thread.sleep(500);
//        robot.setAngleServo(180, 1);

        while(true) {
            if(shufflecad.joystickData.BtnA) {
                System.out.println("1111");
                robot.setAngleServo(100, 1);
            }
            Thread.sleep(20);
        }

    }
}
