package com.jacemcpherson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Console {

    private static SimpleDateFormat sFormatter = new SimpleDateFormat("dd MMM, hh:mm:ss.SSS");
    private static Scanner sScanner = new Scanner(System.in);

    public enum LogType {
        ERROR, DEBUG, INFO, WARNING
    }

    public static void init() {
        System.out.print("CryptoSystem v0.1a\n> ");
    }

    public static void out(LogType type, String message, Object... args) {
        System.out.printf(
                "%s | %s: %s\n> ",
                sFormatter.format(new Date()),
                type.toString(),
                String.format(message, args)
        );
    }

    public static String getLine() {
        String result = sScanner.nextLine();
        System.out.print("> ");
        return result;
    }

    public static String get() {
        String result = sScanner.next();
        System.out.print("> ");
        return result;
    }

    public static int getInt() {
        int result = sScanner.nextInt();
        System.out.print("> ");
        return result;
    }

    public static double getDouble() {
        double result = sScanner.nextDouble();
        System.out.print("> ");
        return result;
    }

    public static long getLong() {
        long result = sScanner.nextLong();
        System.out.print("> ");
        return result;
    }

    public static boolean getBoolean() {
        boolean result = sScanner.nextBoolean();
        System.out.print("> ");
        return result;
    }

    public static float getFloat() {
        float result = sScanner.nextFloat();
        System.out.print("> ");
        return result;
    }

    public static boolean hasNext() {
        return sScanner.hasNext();
    }

    public static void e(String message, Object... args) {
        out(LogType.ERROR, message, args);
    }

    public static void d(String message, Object... args) {
        out(LogType.DEBUG, message, args);
    }

    public static void i(String message, Object... args) {
        out(LogType.INFO, message, args);
    }

    public static void w(String message, Object... args) {
        out(LogType.WARNING, message, args);
    }

    public static void exception(Exception e) {

        e(e.getClass().getCanonicalName() + ": " + e.getMessage());

        for (StackTraceElement el : e.getStackTrace()) {
            e("\t" + el.toString());
        }
    }
}
