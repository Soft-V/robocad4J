package io.github.softv.internal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LoggerInside {
    private BufferedWriter writer = null;

    public LoggerInside(String path) {
        try {
            writer = new BufferedWriter(new FileWriter(path));
        } catch (IOException ignored) { }
    }

    public synchronized void log(String s) {
        if (writer == null)
            return;
        try {
            writer.append(s);
        } catch (IOException ignored) { }
    }

    public synchronized void close() {
        if (writer == null)
            return;
        try {
            writer.close();
        } catch (IOException ignored) { }
    }
}
