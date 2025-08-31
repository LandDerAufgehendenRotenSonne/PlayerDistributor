package karnickeldev.playerdistributor.parsing;

import karnickeldev.playerdistributor.config.ConfigManager;
import karnickeldev.playerdistributor.distribution.PlayerData;
import karnickeldev.playerdistributor.util.LoggingUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : KarnickelDev
 * @since : 31.08.2025
 **/
public class InputValidator {


    public static List<PlayerData> validateInput(ConfigManager configManager, List<PlayerData> playerData) {
        List<PlayerData> validatedPlayerlist = new ArrayList<>();

        Map<String, PlayerData> playerDataByName = new HashMap<>(playerData.size());
        Map<String, Boolean> validByName = new HashMap<>(playerData.size());

        Map<String, Integer> blacklistHits = new HashMap<>();

        // build maps
        for(PlayerData pd: playerData) {
            playerDataByName.put(pd.name.toLowerCase(), pd);
            validByName.put(pd.name.toLowerCase(), true);
        }

        for(PlayerData pd: playerData) {
            String name = pd.name.toLowerCase();

            // validate username
            if(!checkUsername(name)) {
                LoggingUtil.warn("Player " + pd.name + " in row " + pd.row + " has invalid minecraft username");
                validByName.put(name, false);
                continue;
            }

            // validate role
            if(pd.role.isEmpty() || !configManager.getRoles().contains(pd.role)) {
                LoggingUtil.warn("Player " + pd.name + " in row " + pd.row + " has invalid role " + pd.role);
                validByName.put(name, false);
                continue;
            }

            // validate friends
            List<String> toRemove = new ArrayList<>();
            for(String friend: pd.friends) {
                String key = friend.toLowerCase();
                if(!playerDataByName.containsKey(key)) {
                    LoggingUtil.warn("Player " + pd.name + "'s friend " + friend + " not found");
                    toRemove.add(friend);
                    continue;
                }

                // check friend valid
                if(!validByName.get(key)) {
                    LoggingUtil.warn("Removed Player " + pd.name + "'s friend " + friend + " because he was invalid");
                    toRemove.add(friend);
                    continue;
                }

                // check friend not on blacklist
                if(configManager.getFriendBlacklist().contains(key)) {
                    LoggingUtil.info("Ignoring " + pd.name + "'s Friend " + friend + " because of blacklist");
                    blacklistHits.put(friend.toLowerCase(), blacklistHits.getOrDefault(friend.toLowerCase(), 0) + 1);
                    toRemove.add(friend);
                }

            }
            pd.friends.removeAll(toRemove);

            validatedPlayerlist.add(pd.copy());
        }

        for(String blacklisted: blacklistHits.keySet()) {
            LoggingUtil.info("Attempts to befriend " + blacklisted + ": " + blacklistHits.get(blacklisted));
        }
        LoggingUtil.info("");
        return validatedPlayerlist;
    }

    @SuppressWarnings("unused")
    private static boolean checkUsername(String name) {
        return true;
    }

}
