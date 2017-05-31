package com.github.rerorero.oleoleflake.util;

public class ByteUtil {
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b: bytes){
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] reverse(byte[] org) {
        int len = org.length;
        byte[] bytes = new byte[len];
        for(int i = 0; i < len; i++) {
            bytes[len - i - 1] = org[i];
        }
        return bytes;
    }

    public static byte[] paddingAhead(byte[] src, int wholeBitLen) {
        int byteLen = (int) Math.ceil(wholeBitLen / 8.0);
        if (byteLen <= src.length) {
            return src;
        }
        byte[] fixedArray = new byte[byteLen];
        byte[] padding = new byte[byteLen -  src.length];
        System.arraycopy(padding,0,fixedArray, 0, padding.length);
        System.arraycopy(src,0,fixedArray, padding.length, src.length);
        return fixedArray;
    }
}
