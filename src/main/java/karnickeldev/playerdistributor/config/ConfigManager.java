package karnickeldev.playerdistributor.config;

import karnickeldev.playerdistributor.core.PlayerDistributor;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author : KarnickelDev
 * @since : 29.08.2025
 **/
public class ConfigManager {

    private static final String CONFIG_COMMENTS = """
            Configuration for PlayerDistributor\
            
            used for a Community Event of Dekarldent (https://www.youtube.com/@dekarldent)\
            
            developed by KarnickelDev (https://github.com/KarnickelDev)""";

    private final Path configFile;
    private final Properties properties = new Properties();

    private final String SHEET_NAME_PROP = "sheet-name";
    private final String DISCORD_ID_COL_PROP = "discordId-col";
    private final String TWITCH_CHECK_COL_PROP = "twitch-check-col";
    private final String DISCORD_CHECK_COL_PROP = "discord-check-col";
    private final String MC_NAME_COL_PROP = "mc-name-col";
    private final String ROLE_COL_PROP = "role-col";
    private final String ROLES_PROP = "roles";
    private final String FACTION_COL_PROP = "faction-col";
    private final String FACTIONS_PROP = "factions";
    private final String FRIENDS_COL_PROP = "friends-col";
    private final String MAX_FRIENDS_PROP = "max-friends";
    private final String FRIENDS_BLACKLIST_PROP = "friends-blacklist";
    private final String START_ROW = "start-row";
    private final String END_ROW = "end-row";
    private final String INCLUDE_COL = "include-col";
    private final String PLAYER_LIMIT = "player-limit";
    private final String SEED = "seed";



    public ConfigManager(Path configFile) {
        this.configFile = configFile;
    }

    public Path getConfigFile() {
        return configFile;
    }

    public static Path getJarDirectory() {
        try {
            Path jarPath = Paths.get(ConfigManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path jarDir = jarPath.getParent();
            if (jarDir != null) return jarDir.toAbsolutePath();
        } catch (URISyntaxException | NullPointerException ignored) {}

        // Fallback: use current working directory
        System.out.println("Warning: Could not determine JAR folder, using current working directory.");
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    }

    public void save() throws IOException {
        try (OutputStream out = Files.newOutputStream(configFile)) {
            properties.store(out, CONFIG_COMMENTS);
        }
    }

    public void createDefault() throws IOException {
        properties.setProperty(SHEET_NAME_PROP, "PlayerData");
        properties.setProperty(DISCORD_ID_COL_PROP, "0");
        properties.setProperty(MC_NAME_COL_PROP, "1");
        properties.setProperty(TWITCH_CHECK_COL_PROP, "2");
        properties.setProperty(DISCORD_CHECK_COL_PROP, "3");
        properties.setProperty(ROLE_COL_PROP, "4");
        properties.setProperty(FACTION_COL_PROP, "5");
        properties.setProperty(INCLUDE_COL, "6");
        properties.setProperty(FRIENDS_COL_PROP, "7");
        properties.setProperty(MAX_FRIENDS_PROP, "5");
        properties.setProperty(FACTIONS_PROP, "Jungle, Desert, Plains");
        properties.setProperty(ROLES_PROP, "PVP, Builder, Roleplay");
        properties.setProperty(FRIENDS_BLACKLIST_PROP, "");
        properties.setProperty(START_ROW, "1");
        properties.setProperty(END_ROW, "100");
        properties.setProperty(PLAYER_LIMIT, "200");
        properties.setProperty(SEED, "421337161");

        save();
    }

    public boolean load() {
        if(Files.exists(configFile)) {
            try(InputStream is = Files.newInputStream(configFile)) {
                properties.load(is);
            } catch (IOException e) {
                System.err.println("Error loading config");
                throw new RuntimeException(e);
            }
            System.out.println("Configuration loaded");
            return true;
        }
        return false;
    }

    public boolean isValid() {
        for(Object k : properties.keySet()) {
            if(properties.get(k) == null) return false;
        }
        return true;
    }

    public String getSheetName() {
        return properties.getProperty(SHEET_NAME_PROP);
    }

    public int getDiscordIdCol() {
        try {
            return Integer.parseInt(properties.getProperty(DISCORD_ID_COL_PROP));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getMCNameCol() {
        try {
            return Integer.parseInt(properties.getProperty(MC_NAME_COL_PROP));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getTwitchCheckedCol() {
        try {
            return Integer.parseInt(properties.getProperty(TWITCH_CHECK_COL_PROP));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getDiscordCheckedCol() {
        try {
            return Integer.parseInt(properties.getProperty(DISCORD_CHECK_COL_PROP));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getRoleCol() {
        try {
            return Integer.parseInt(properties.getProperty(ROLE_COL_PROP));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getFactionCol() {
        try {
            return Integer.parseInt(properties.getProperty(FACTION_COL_PROP));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getFriendsCol() {
        try {
            return Integer.parseInt(properties.getProperty(FRIENDS_COL_PROP));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getMaxFriends() {
        try {
            return Integer.parseInt(properties.getProperty(MAX_FRIENDS_PROP));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getStartRow() {
        try {
            return Integer.parseInt(properties.getProperty(START_ROW));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getEndRow() {
        try {
            return Integer.parseInt(properties.getProperty(END_ROW));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getIncludeCol() {
        try {
            return Integer.parseInt(properties.getProperty(INCLUDE_COL));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getPlayerLimit() {
        try {
            return Integer.parseInt(properties.getProperty(PLAYER_LIMIT));
        } catch (Exception e) {
            return 0;
        }
    }

    public long getSeed() {
        try {
            return Long.parseLong(properties.getProperty(SEED));
        } catch (Exception e) {
            return 0;
        }
    }

    public List<String> getFactions() {
        String factionString = properties.getProperty(FACTIONS_PROP);
        if(factionString == null || factionString.isEmpty()) return Collections.emptyList();
        return Arrays.stream(factionString.split(",")).map(String::trim).toList();
    }

    public List<String> getRoles() {
        String factionString = properties.getProperty(ROLES_PROP);
        if(factionString == null || factionString.isEmpty()) return Collections.emptyList();
        List<String> ret = new ArrayList<>(Arrays.stream(factionString.split(",")).map(String::trim).map(String::toLowerCase).toList());
        ret.add(PlayerDistributor.UNASSIGNED_ROLE);
        return ret;
    }

    public List<String> getFriendBlacklist() {
        String factionString = properties.getProperty(FRIENDS_BLACKLIST_PROP);
        if(factionString == null || factionString.isEmpty()) return Collections.emptyList();
        return Arrays.stream(factionString.split(",")).map(String::trim).map(String::toLowerCase).toList();
    }

}
