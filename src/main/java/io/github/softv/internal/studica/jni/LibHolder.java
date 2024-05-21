package io.github.softv.internal.studica.jni;

public class LibHolder {
    private static JavaWrapper instance;

    public static JavaWrapper getInstance(){
        if (instance == null){
            instance = new JavaWrapper();
        }
        return instance;
    }
}
