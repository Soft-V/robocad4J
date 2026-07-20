package io.github.softv.internal.common;

import java.util.ArrayList;

public interface LidarBase {
    void start();

    // 360 distances, one per degree.
    ArrayList<Integer> getData();

    void stop();
}
