package io.github.crackanddie.shufflecad;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ReadWriteSocketHelper {
    public static void write(DataOutputStream stream, byte[] data) throws IOException {
        byte[] lengthTrans = { (byte)((data.length) & 0xff), (byte)((data.length >> 8) & 0xff),
                (byte)((data.length >> 16) & 0xff), (byte)((data.length >> 24) & 0xff) };
        stream.write(lengthTrans);
        stream.flush();
        stream.write(data);
        stream.flush();
    }

    public static byte[] read(DataInputStream stream) throws IOException {
        byte[] messageLen = new byte[4];
        stream.readFully(messageLen, 0, messageLen.length);
        int length = (messageLen[3] & 0xff) << 24 | (messageLen[2] & 0xff) << 16 |
                (messageLen[1] & 0xff) << 8 | (messageLen[0] & 0xff);
        if (length > 0){
            byte[] message = new byte[length];
            stream.readFully(message, 0, length);
            return message;
        }
        return new byte[0];
    }
}
