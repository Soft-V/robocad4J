package io.github.softv.internal.common;

public abstract class Updater {
    public final Robot robot;
    public boolean stopRobotInfoThread = false;

    public Updater(Robot robot) {
        this.robot = robot;
    }

    public abstract void update();
}
