package io.github.softv.internal.common;

import org.opencv.core.Mat;

import java.util.ArrayList;

public abstract class ConnectionBase {
    public abstract void stop();
    public abstract Mat getCamera();
    public abstract ArrayList<Integer> getLidar();
}
