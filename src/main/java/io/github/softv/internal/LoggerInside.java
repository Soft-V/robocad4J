package io.github.softv.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LoggerInside {
    private final String path;

    public LoggerInside(String path) {
        this.path = path;
        try {
            File yourFile = new File(path);
            boolean ignored = yourFile.createNewFile(); // if file already exists will do nothing
        } catch (IOException ignored) { }
    }

    public synchronized void log(String s) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.path));
            writer.append(s);
            writer.append("\n");
        } catch (IOException ignored) { }
    }
}
