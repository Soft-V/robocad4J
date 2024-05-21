package io.github.softv.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LowLevelFuncad {
    public static boolean accessBit(byte b, int pos){
        return ((b >> pos) & 1) != 0;
    }

    public static byte[] intTo4Bytes(int val){
        return new byte[] { (byte)((val >> 24) & 0xff), (byte)((val >> 16) & 0xff),
                (byte)((val >> 8) & 0xff), (byte)(val & 0xff)};
    }

    public static byte[] readBytes(DataInputStream in) throws IOException {
        byte[] dataSize = new byte[4];
        in.readFully(dataSize, 0, 4);
        int length = (dataSize[3] & 0xff) << 24 | (dataSize[2] & 0xff) << 16 |
                (dataSize[1] & 0xff) << 8 | (dataSize[0] & 0xff);

        if(length > 0)
        {
            byte[] message = new byte[length];
            in.readFully(message, 0, length);
            return message;
        }
        return new byte[0];
    }

    public static void writeBytes(DataOutputStream out, byte[] data) throws IOException {
        byte[] sizeBytes = intTo4Bytes(data.length);
        out.write(sizeBytes);
        out.write(data);
    }
}
