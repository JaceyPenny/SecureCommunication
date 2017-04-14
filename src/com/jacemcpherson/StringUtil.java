package com.jacemcpherson;

import java.util.Arrays;

/**
 * Provides quick access to frequently used String operations.
 */
public class StringUtil {

    /**
     * Converts an integer to a String, then pads that string to "totalLength" using spaces.
     * @param value
     * @param totalLength
     * @return a String of length "totalLength" starting with the String value of "value"
     */
    public static String padded(int value, int totalLength) {
        return padded(Integer.toString(value), totalLength);
    }

    /**
     * Pads a String to "totalLength" using spaces.
     * @param value
     * @param totalLength
     * @return a String of length "totalLength" starting with "value"
     */
    public static String padded(String value, int totalLength) {
        if (totalLength <= value.length()) {
            return value.substring(0, totalLength);
        }

        return value + blankLine(totalLength - value.length());
    }

    /**
     * Produces a String of length "length" consisting only of spaces.
     * @param length
     * @return a String of length "length" consisting only of spaces.
     */
    public static String blankLine(int length) {
        return repeatedCharacter(' ', length);
    }

    /**
     * Produces a String of length "length" consisting only of the character "repeat"
     * @param repeat
     * @param length
     * @return a String of length "length" consisting only of the character "repeat"
     */
    public static String repeatedCharacter(char repeat, int length) {
        char[] spaces = new char[length];
        Arrays.fill(spaces, repeat);
        return new String(spaces);
    }

    /**
     * Formats a byte[] as a hex string. The resulting String should have in.length * 2 characters.
     * @param in
     * @return "in" formatted as a hex String.
     */
    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
