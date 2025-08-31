package karnickeldev.playerdistributor.parsing;

import karnickeldev.playerdistributor.config.ConfigManager;
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
        int name_col = configManager.getMCNameCol();
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

        Map<String, Integer> visited = new HashMap<>();

        int emptyRows = 0;

        for(int y = start; y <= end; y++) {
            String name = excelInput.readCell(sheet, y, name_col);
            if(name == null || name.isEmpty()) {
                emptyRows++;
                continue;
            }

            if(visited.containsKey(name.toLowerCase())) {
                LoggingUtil.warn("skipping double entry \"" + name + "\" found in rows " + y + ", " + visited.get(name.toLowerCase()));
                continue;
            }
            visited.put(name.toLowerCase(), y);

            String faction = excelInput.readCell(sheet, y, faction_col);

            if(faction == null) {
                faction = "";
            }

            // handle faction
            if(!faction.isEmpty() && !configManager.getFactions().contains(faction)) {
                LoggingUtil.warn("Invalid faction found for player " + name + " in row " + y);
                continue;
            }

            String role = excelInput.readCell(sheet, y, role_col);
            if(role == null) {
                role = "";
            }

            List<String> friends = new ArrayList<>();
            for(int x = friend_col; x <= friend_col + max_friends; x++) {
                String friend = excelInput.readCell(sheet, y, x);
                if(friend != null && !friend.isEmpty()) {
                    friends.add(friend);
                }
            }

            PlayerData pd = new PlayerData(y, name, role, faction, friends);

            playerList.add(pd);

            if(y % 10 == 0) {
                LoggingUtil.printProgressBar("Parsing Input", (byte)20, (y-start) / (float) (end - start));
            }

        }
        LoggingUtil.info("skipped " + emptyRows + " empty rows");
        return playerList;
    }

}
