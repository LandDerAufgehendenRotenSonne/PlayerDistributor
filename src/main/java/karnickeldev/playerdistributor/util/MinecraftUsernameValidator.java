package karnickeldev.playerdistributor.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author : KarnickelDev
 * @since : 31.08.2025
 **/
public class MinecraftUsernameValidator {

    private static final String MOJANG_API =
            "https://api.mojang.com/users/profiles/minecraft/";

    private static final int TIMEOUT_MS = 50;
    private static long lastCheck = 0;

    /**
     * Returns true if the given username exists (is a valid registered Minecraft username).
     */
    public static boolean checkUsername(String username) {
        try {
            if(System.nanoTime() - lastCheck < TIMEOUT_MS * 1_000_000L) {
                Thread.sleep(TIMEOUT_MS);
            }

            URL url = new URL(MOJANG_API + username);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);

            int code = con.getResponseCode();

            lastCheck = System.nanoTime();

            // 200 = valid user, 204 (or 404) = not valid
            return (code == 200);
        } catch (Exception e) {
            // Network error or API failure
            LoggingUtil.error(e.getMessage());
            return false;
        }
    }
}

