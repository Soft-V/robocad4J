package io.github.softv.internal.studica;

import org.opencv.core.Mat;

public abstract class ConnectionBase {
    public abstract void start();
    public abstract void stop();
    public abstract Mat getCamera();
}
