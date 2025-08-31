package karnickeldev.playerdistributor.parsing;

import karnickeldev.playerdistributor.config.ConfigManager;
import karnickeldev.playerdistributor.core.PlayerDistributor;
import karnickeldev.playerdistributor.distribution.PlayerData;
import karnickeldev.playerdistributor.excel.ExcelHelper;
import karnickeldev.playerdistributor.util.LoggingUtil;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.*;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class InputReader {

    /**
     * Loads the PlayerData from the Excel File
     * Ignores empty rows or Players that are already assigned a Faction
     * @return A List of Players with no Faction
     */
    public static List<PlayerData> loadPlayerData(ExcelHelper excelInput, ConfigManager configManager) {
        List<PlayerData> playerList = new ArrayList<>(512);

        String sheetName = configManager.getSheetName();
        int discordId_col = configManager.getDiscordIdCol();
        int name_col = configManager.getMCNameCol();
        int twitch_checked_col = configManager.getTwitchCheckedCol();
        int discord_checked_col = configManager.getDiscordCheckedCol();
        int role_col = configManager.getRoleCol();
        int faction_col = configManager.getFactionCol();
        int friend_col = configManager.getFriendsCol();
        int max_friends = configManager.getMaxFriends();

        int start = configManager.getStartRow();
        int end = configManager.getEndRow();

        Sheet sheet = excelInput.getSheet(sheetName);
        if(sheet == null) {
            LoggingUtil.warn("Sheet " + sheetName + " not found");
            System.exit(0);
        }

        Map<String, Integer> mcVisited = new HashMap<>();
        Map<String, Integer> discordVisited = new HashMap<>();

        int emptyRows = 0;

        for(int y = start; y <= end; y++) {

            // check discordId
            String discordId = excelInput.readCell(sheet, y, discordId_col);
            if(discordId == null || discordId.isEmpty()) {
                emptyRows++;
                continue;
            }

            if(discordVisited.containsKey(discordId)) {
                LoggingUtil.warn("skipping double entry \"" + discordId + "\" found in rows " + y + ", " + discordVisited.get(discordId));
                continue;
            }
            discordVisited.put(discordId, y);

            // check mc name
            String name = excelInput.readCell(sheet, y, name_col);
            if(name == null) {
                name = "missing minecraft name";
            } else {
                if(mcVisited.containsKey(name.toLowerCase())) {
                    LoggingUtil.warn("skipping double mc-name \"" + name + "\" found in rows " + y + ", " +
                            mcVisited.get(name.toLowerCase()) + " of user @" + discordId);
                    continue;
                }
                mcVisited.put(name.toLowerCase(), y);
            }

            // check twitch
            String twitchCheck = excelInput.readCell(sheet, y, twitch_checked_col);
            if(twitchCheck == null || twitchCheck.isEmpty()) {
                LoggingUtil.warn("skipping @" + discordId + " in row " + y + " because twitch isn't checked");
            }
            boolean twitch = parseBool(twitchCheck);

            // check discord
            String discordCheck = excelInput.readCell(sheet, y, discord_checked_col);
            if(discordCheck == null || discordCheck.isEmpty()) {
                LoggingUtil.warn("skipping @" + discordId + " in row " + y + " because discord isn't checked");
            }
            boolean discord = parseBool(twitchCheck);

            // maybe remove row
            // skip if players background is not properly checked
            if(!(discord && twitch)) {
                if(PlayerDistributor.REMOVE_UNCHECKED_ENTRIES) {
                    LoggingUtil.info("Deleted row " + y + " with player @" + discordId);
                    excelInput.removeRow(sheet, y);
                    y--;
                }
                continue;
            }


            // check faction
            String faction = excelInput.readCell(sheet, y, faction_col);
            if(faction == null) faction = "";

            // handle faction
            if(!faction.isEmpty() && !configManager.getFactions().contains(faction)) {
                LoggingUtil.warn("Invalid faction found for player @" + discordId + " in row " + y);
                continue;
            }

            // check role
            String role = excelInput.readCell(sheet, y, role_col);
            if(role == null) {
                role = "";
            }

            // check friends
            List<String> friends = new ArrayList<>();
            for(int x = friend_col; x <= friend_col + max_friends; x++) {
                String friend = excelInput.readCell(sheet, y, x);
                if(friend != null && !friend.isEmpty()) {
                    friends.add(friend);
                }
            }

            playerList.add(new PlayerData(y, discordId, name, twitch, discord, role, faction, friends));

            if(y % 10 == 0) {
                LoggingUtil.printProgressBar("Parsing Input", (byte)20, (y-start) / (float) (end - start));
            }

        }
        LoggingUtil.info("skipped " + emptyRows + " empty rows");
        return playerList;
    }

    private static boolean parseBool(String bool) {
        try {
            return Boolean.parseBoolean(bool);
        } catch (Exception e) {
            return false;
        }
    }

}
