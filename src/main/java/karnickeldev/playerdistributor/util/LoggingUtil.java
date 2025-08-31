package karnickeldev.playerdistributor.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class LoggingUtil {

    private static int WARNINGS = 0;

    public static int getWarnings() {
        return WARNINGS;
    }

    private static FileWriter fw;
    private static PrintWriter pw;

    private static boolean initialized = false;

    public static void init() throws IOException {
        if(initialized) return;
        initialized = true;

        fw = new FileWriter("log.log", true);
        pw = new PrintWriter(fw);
    }

    public static void close() throws IOException {
        pw.close();
        fw.close();
    }

    private static void log(String msg, boolean writeToFile) {
        System.out.print(msg);
        if(writeToFile) pw.print(msg);
    }

    public static void flush() {
        System.out.flush();
        pw.flush();
    }

    public static void printProgressBar(String prefix, byte length, float progress) {
        log("\r", false);
        log(prefix, false);
        log(" [", false);

        byte done = (byte) (progress * length);
        for(byte i = 0; i < length; i++) {
            log(i <= done ? "#" : "_", false);
        }
        log("]\n", false);
        flush();
    }

    public static void warn(String text) {
        WARNINGS++;
        log("[WARN] " + text + '\n', true);
        flush();
    }

    public static void info(String text) {
        log("[INFO] " + text + '\n', true);
        flush();
    }

    public static void error(String text) {
        log("[ERROR] " + text + '\n', true);
        flush();
    }


}
