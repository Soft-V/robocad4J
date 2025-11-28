package io.github.softv.internal.common;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.File;
import java.util.Scanner;

public class UpdaterRepka extends Updater {
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final CentralProcessor processor;
    private final GlobalMemory memory;

    public UpdaterRepka(Robot robot) {
        super(robot);

        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.processor = hardware.getProcessor();
        this.memory = hardware.getMemory();
    }

    public void update() {
        while (!stopRobotInfoThread) {
            File tempFile = new File("/sys/class/thermal/thermal_zone0/temp");
            try (Scanner myReader = new Scanner(tempFile)) {
                if (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    robot.robotInfo.temperature = Float.parseFloat(data.replace(',', '.')) / 1000f;
                }
            } catch (Exception e) {
                robot.writeLog(e.getMessage());
            }

            robot.robotInfo.cpuLoad = (float)processor.getSystemCpuLoad(500) * 100;
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            robot.robotInfo.memoryLoad = ((float)usedMemory) / totalMemory * 100;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) { }
        }
    }
}
