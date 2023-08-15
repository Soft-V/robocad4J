package io.github.crackanddie.jni;

public class JavaWrapper {
    static {
        System.load("/home/pi/CommonRPiLibrary/CommonRPiLibrary/build/libCommonRPiLibrary.so");
    }

    private static native void Java_StartSPI();
    private static native void Java_StartUSB();
    private static native void Java_StopSPI();
    private static native void Java_StopUSB();
    private static native byte[] Java_ReadWriteSPI(byte[] data, int len);
    private static native byte[] Java_ReadWriteUSB(byte[] data, int len);

    public JavaWrapper(){}

    public void startSPI(){
        Java_StartSPI();
    }

    public void startUSB(){
        Java_StartUSB();
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
