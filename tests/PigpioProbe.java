import io.github.softv.internal.common.PigpioPWM;
import java.util.Scanner;

public final class PigpioProbe {
    public static void main(String[] args) {
        PigpioPWM pwm = new PigpioPWM(args[0], args[1], 100, 0.15);
        int[] gpios = {4, 18, 17, 27, 23, 22, 24, 25, 7, 5};
        Scanner input = new Scanner(System.in);
        while (input.hasNextLine()) {
            String[] fields = input.nextLine().split(" ");
            try {
                if (fields[0].equals("set"))
                    pwm.setDutycycle(Integer.parseInt(fields[1]), Double.parseDouble(fields[2]));
                else if (fields[0].equals("stop")) pwm.stop(gpios);
                else if (fields[0].equals("stop-used")) pwm.stop();
                else throw new IllegalArgumentException("Unknown probe command");
                System.out.println("ok");
            } catch (RuntimeException error) {
                System.out.println("error: " + error.toString().replace('\n', ' ').replace('\r', ' '));
            }
        }
    }
}
