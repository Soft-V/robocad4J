package io.github.softv.internal.common;

public class JavaWrapper {
    static {
        // TODO: set first path from configuration
        System.load("/home/pi/CommonRPiLibrary/CommonRPiLibrary/build/libCommonRPiLibrary.so");
    }

    private static native int Java_StartSPI(String path, int channel, int speed, int mode);
    private static native int Java_StartUSB(String path, int baud);
    private static native void Java_StopSPI();
    private static native void Java_StopUSB();
    private static native byte[] Java_ReadWriteSPI(byte[] data, int len);
    private static native byte[] Java_ReadWriteUSB(byte[] data, int len);

    public JavaWrapper(){}

    public int startSPI(String path, int channel, int speed, int mode){
        return Java_StartSPI(path, channel, speed, mode);
    }

    public int startUSB(String path, int baud){
        return Java_StartUSB(path, baud);
    }

    public byte[] readWriteSPI(byte[] data, int len){
        return Java_ReadWriteSPI(data, len);
    }

    public byte[] readWriteUSB(byte[] data, int len){
        return Java_ReadWriteUSB(data, len);
    }

    public void stopSPI(){
        Java_StopSPI();
    }

    public void stopUSB(){
        Java_StopUSB();
    }
}
