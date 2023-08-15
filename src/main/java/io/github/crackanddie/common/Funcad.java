package io.github.crackanddie.common;

public class Funcad {
    public static boolean accessBit(byte b, int pos){
        return ((b >> pos) & 1) != 0;
    }
}
