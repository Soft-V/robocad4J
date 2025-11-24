package io.github.softv.internal.common;

import io.github.softv.internal.LoggerInside;

public abstract class Robot {
    public boolean onRealRobot;
    public LoggerInside logger = null;
    public float power = 0.0f;
    public RobotInfo robotInfo = null;

    public Robot() {
    }

    public void init(boolean onRealRobot, RobotConfiguration conf) {
        this.onRealRobot = onRealRobot;
        String logPath = onRealRobot ? conf.realLogPath : conf.simLogPath;
        this.logger = new LoggerInside(logPath);
        this.robotInfo = new RobotInfo();
    }

    public synchronized void writeLog(String s) {
        this.logger.log(s); // TODO: time
    }
}
