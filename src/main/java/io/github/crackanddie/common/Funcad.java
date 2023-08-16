package io.github.crackanddie.common;

public class Funcad {
    public static boolean accessBit(byte b, int pos){
        return ((b >> pos) & 1) != 0;
    }

    public static byte[] intTo4Bytes(int val){
        return new byte[] { (byte)((val >> 24) & 0xff), (byte)((val >> 16) & 0xff),
                (byte)((val >> 8) & 0xff), (byte)(val & 0xff)};
    }
}
