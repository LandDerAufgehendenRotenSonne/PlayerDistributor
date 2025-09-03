package karnickeldev.playerdistributor.parsing;

import karnickeldev.playerdistributor.config.ConfigManager;
import karnickeldev.playerdistributor.core.PlayerDistributor;
import karnickeldev.playerdistributor.distribution.PlayerData;
import karnickeldev.playerdistributor.util.LoggingUtil;
import karnickeldev.playerdistributor.util.MinecraftUsernameValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : KarnickelDev
 * @since : 31.08.2025
 **/
public class InputValidator {

    public static final String MISSING_MC_NAME = "NAME MISSING";

    public static List<PlayerData> validateInput(ConfigManager configManager, List<PlayerData> playerData) {
        List<PlayerData> validatedPlayerlist = new ArrayList<>();

        Map<String, String> discordByName = new HashMap<>(playerData.size());
        Map<String, PlayerData> playerDataByDiscord = new HashMap<>();
        Map<String, Boolean> validByDiscord = new HashMap<>(playerData.size());

        Map<String, Integer> blacklistHits = new HashMap<>();

        // build maps
        for(PlayerData pd: playerData) {
            discordByName.put(pd.name.toLowerCase(), pd.discordId.toLowerCase());
            playerDataByDiscord.put(pd.discordId.toLowerCase(), pd);
            validByDiscord.put(pd.discordId.toLowerCase(), true);
        }

        for(PlayerData pd: playerData) {
            String discord = pd.discordId.toLowerCase();

            // validate username
            if(pd.name != null && !pd.name.equals(MISSING_MC_NAME) && !checkUsername(pd.name)) {
                LoggingUtil.warn("Player @" + pd.discordId + " in row " + (pd.row+1) + " has invalid minecraft username " + pd.name);
                validByDiscord.put(discord, false);
                continue;
            }

            // validate role
            if(pd.role.isEmpty() || !configManager.getRoles().contains(pd.role)) {
                LoggingUtil.warn("Player @" + pd.discordId + " in row " + (pd.row+1) + " has invalid role " + pd.role);
                validByDiscord.put(discord, false);
                continue;
            }

            // validate friends
            List<String> toRemove = new ArrayList<>();
            for(String friend: pd.friends) {
                String key = friend.toLowerCase();
                if(!playerDataByDiscord.containsKey(key)) {
                    key = discordByName.get(key);
                }

                // check if player exists
                if(key == null || !playerDataByDiscord.containsKey(key)) {
                    LoggingUtil.warn("Player @" + pd.discordId + "'s friend " + friend + " not found");
                    toRemove.add(friend);
                    continue;
                }

                // check friend valid
                if(!validByDiscord.get(key)) {
                    LoggingUtil.warn("Removed Player @" + pd.discordId + "'s friend " + friend + " because he was invalid");
                    toRemove.add(friend);
                    continue;
                }

                // check friend not on blacklist
                if(configManager.getFriendBlacklist().contains(key)) {
                    LoggingUtil.info("Ignoring @" + pd.discordId + "'s Friend " + friend + " because of blacklist");
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
        if(!PlayerDistributor.CHECK_MINECRAFT_NAMES) return true;
        if(name.equals(MISSING_MC_NAME)) return true;
        return MinecraftUsernameValidator.checkUsername(name);
    }

}
