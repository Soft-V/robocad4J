package io.github.softv.internal.common;

public abstract class Updater {
    public final Robot robot;
    public boolean stopRobotInfoThread = false;

    public Updater(Robot robot) {
        this.robot = robot;
    }

    public abstract void update();

    protected float usagePercent(float used, float total) {
        if (total == 0)
            return 0;
        return used / total;
    }
}
