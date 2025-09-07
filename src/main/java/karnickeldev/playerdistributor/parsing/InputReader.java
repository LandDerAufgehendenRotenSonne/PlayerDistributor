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

        List<Integer> rowsToDelete = new ArrayList<>();

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
                rowsToDelete.add(y);
                LoggingUtil.warn("Deleted row " + y + " with empty username");
                continue;
            }

            if(hasWhitespace(discordId)) {
                LoggingUtil.info("Suspicious DiscordId " + discordId + " in row " + (y+1));
            }

            if(discordVisited.containsKey(discordId.toLowerCase())) {
                LoggingUtil.warn("skipping double entry \"" + discordId + "\" found in rows " + (y+1) + ", " + (discordVisited.get(discordId.toLowerCase())+1));
                if(PlayerDistributor.REMOVE_UNCHECKED_ENTRIES) {
                    rowsToDelete.add(y);
                }
                continue;
            }
            discordVisited.put(discordId.toLowerCase(), y);

            // check mc name
            String name = excelInput.readCell(sheet, y, name_col);
            if(name == null) {
                name = InputValidator.MISSING_MC_NAME;
            } else {
                if(!name.equals(InputValidator.MISSING_MC_NAME) && mcVisited.containsKey(name.toLowerCase())) {
                    LoggingUtil.warn("skipping double mc-name \"" + name + "\" found in rows " + (y+1) + ", " +
                            (mcVisited.get(name.toLowerCase())+1) + " of user @" + discordId);
                    if(PlayerDistributor.REMOVE_UNCHECKED_ENTRIES) {
                        rowsToDelete.add(y);
                    }
                    continue;
                }
                mcVisited.put(name.toLowerCase(), y);
            }

            if(hasWhitespace(name) && !name.equals(InputValidator.MISSING_MC_NAME)) {
                LoggingUtil.info("Suspicious MinecraftName " + name + " in row " + (y+1));
            }

            // check twitch
            String twitchCheck = excelInput.readCell(sheet, y, twitch_checked_col);
            if(twitchCheck == null || twitchCheck.isEmpty()) {
                LoggingUtil.warn("skipping @" + discordId + " in row " + (y+1) + " because twitch isn't checked");
            }
            boolean twitch = parseBool(twitchCheck);

            // check discord
            String discordCheck = excelInput.readCell(sheet, y, discord_checked_col);
            if(discordCheck == null || discordCheck.isEmpty()) {
                LoggingUtil.warn("skipping @" + discordId + " in row " + (y+1) + " because discord isn't checked");
            }
            boolean discord = parseBool(discordCheck);

            // maybe remove row
            // skip if players background is not properly checked
            if(!(discord && twitch)) {
                if(PlayerDistributor.REMOVE_UNCHECKED_ENTRIES) {
                    LoggingUtil.info("Deleted row " + (y+1) + " with player @" + discordId);
                    rowsToDelete.add(y);
                }
                continue;
            }


            // check faction
            String faction = excelInput.readCell(sheet, y, faction_col);
            if(faction == null) faction = "";

            // handle faction
            if(!faction.isEmpty() && !configManager.getFactions().contains(faction)) {
                LoggingUtil.warn("Invalid faction found for player @" + discordId + " in row " + (y+1));
                continue;
            }

            // check role
            String role = excelInput.readCell(sheet, y, role_col);
            if(role == null || !configManager.getRoles().contains(role.toLowerCase())) {
                role = PlayerDistributor.UNASSIGNED_ROLE;
            } else {
                role = role.toLowerCase();
            }

            // check friends
            List<String> friends = new ArrayList<>();
            for(int x = friend_col; x <= friend_col + max_friends; x++) {
                String friend = excelInput.readCell(sheet, y, x);
                if(friend != null && !friend.isEmpty()) {
                    if(friend.contains(",")) {
                        String[] friendList = friend.split(",");
                        for(String fl: friendList) {
                            String fn = fl.trim();
                            friends.add(fn);
                        }
                    } else {
                        friends.add(friend);
                    }
                }
            }


            // check include
            String slot = excelInput.readCell(sheet, y, configManager.getIncludeCol());
            boolean guaranteed_slot = parseBool(slot);
            if(guaranteed_slot) LoggingUtil.info("User @" + name + " has a slot guarantee");

            playerList.add(new PlayerData(y, discordId, name, true, true, role, faction, guaranteed_slot, friends));

            if(y % 10 == 0) {
                LoggingUtil.printProgressBar("Parsing Input", (byte)20, (y-start) / (float) (end - start));
            }

        }

        if(PlayerDistributor.REMOVE_UNCHECKED_ENTRIES) {
            int length = rowsToDelete.size();
            for(int i = 0; i < length; i++) {
                excelInput.removeRow(sheet, rowsToDelete.get(i) - i);
            }
        }

        LoggingUtil.info("skipped " + emptyRows + " empty rows");
        return playerList;
    }

    private static boolean hasWhitespace(String s) {
        if(s == null || s.isEmpty()) return false;
        for(char c: s.trim().toCharArray()) {
            if(Character.isWhitespace(c)) return true;
        }
        return false;
    }

    public static boolean parseBool(String bool) {
        if(bool == null || bool.isEmpty()) return false;
        return bool.equalsIgnoreCase("true")
                || bool.equalsIgnoreCase("checked")
                || bool.equals("1") || bool.equalsIgnoreCase("include");
    }

}
