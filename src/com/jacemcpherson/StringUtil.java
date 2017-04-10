package com.jacemcpherson;

import java.util.Arrays;

public class StringUtil {

    public static String padded(int entry, int totalLength) {
        return padded(Integer.toString(entry), totalLength);
    }

    public static String padded(String entry, int totalLength) {
        if (totalLength <= entry.length()) {
            return entry.substring(0, totalLength);
        }

        return entry + blankLine(totalLength - entry.length());
    }

    public static String blankLine(int length) {
        return repeatedCharacter(' ', length);
    }

    public static String repeatedCharacter(char repeat, int length) {
        char[] spaces = new char[length];
        Arrays.fill(spaces, repeat);
        return new String(spaces);
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
