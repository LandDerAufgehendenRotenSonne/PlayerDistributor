package karnickeldev.playerdistributor.distribution;

import karnickeldev.playerdistributor.config.ConfigManager;
import karnickeldev.playerdistributor.excel.ExcelHelper;
import karnickeldev.playerdistributor.util.LoggingUtil;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public static PlayerList loadPlayerData(ExcelHelper excelInput, ConfigManager configManager) {
        List<PlayerData> unfactioned = new ArrayList<>();
        List<PlayerData> factioned = new ArrayList<>();

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
            System.out.println("Sheet " + sheetName + " not found");
            System.exit(0);
        }

        List<String> visited = new ArrayList<>();

        int emptyRows = 0;

        for(int y = start; y <= end; y++) {
            String name = excelInput.readCell(sheet, y, name_col);
            if(name == null || name.isEmpty()) {
                emptyRows++;
                continue;
            }

            if(visited.contains(name)) {
                System.out.println("WARNING: double entry \"" + name + "\" found");
                continue;
            }
            visited.add(name);

            String faction = excelInput.readCell(sheet, y, faction_col);
            boolean hasFaction = false;

            // skip players with entry
            if(faction != null && !faction.isEmpty()) {
                if(!configManager.getFactions().contains(faction)) {
                    System.out.println("WARNING: invalid faction found");
                    continue;
                }
                hasFaction = true;
            }

            String role = excelInput.readCell(sheet, y, role_col);
            if(role == null || role.isEmpty()) {
                System.out.println("skipping row without any role");
                continue;
            }
            if(!configManager.getRoles().contains(role)) {
                System.out.println("skipping row with invalid role");
                continue;
            }

            List<String> friends = new ArrayList<>();
            for(int x = friend_col; x <= friend_col + max_friends; x++) {
                String friend = excelInput.readCell(sheet, y, x);
                if(friend != null && !friend.isEmpty() && !configManager.getFriendBlacklist().contains(friend)) {
                    friends.add(friend);
                }
            }

            if(hasFaction) {
                PlayerData pd = new PlayerData(y, name, role, Collections.emptyList());
                pd.faction = faction;
                factioned.add(pd);
            } else {
                unfactioned.add(new PlayerData(y, name, role, friends));
            }

            if(y % 10 == 0) {
                LoggingUtil.printProgressBar("Parsing Input", (byte)20, (y-start) / (float) (end - start));
            }

        }
        System.out.println("skipped " + emptyRows + " empty rows");
        return new PlayerList(factioned, unfactioned);
    }

}
