package karnickeldev.playerdistributor.util;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class LoggingUtil {

    public static void printProgressBar(String prefix, byte length, float progress) {
        System.out.print(prefix);
        System.out.print(" [");

        byte done = (byte) (progress * length);
        for(byte i = 0; i < length; i++) {
            System.out.print(i <= done ? '#' : '_');
        }
        System.out.println("]\n"); // should flush
    }


}
