package karnickeldev.playerdistributor.core;

import karnickeldev.playerdistributor.config.ConfigManager;
import karnickeldev.playerdistributor.distribution.*;
import karnickeldev.playerdistributor.excel.ExcelFileUtil;
import karnickeldev.playerdistributor.excel.ExcelHelper;
import karnickeldev.playerdistributor.parsing.InputReader;
import karnickeldev.playerdistributor.parsing.InputValidator;
import karnickeldev.playerdistributor.parsing.Limiter;
import karnickeldev.playerdistributor.util.LoggingUtil;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author : KarnickelDev
 * @since : 29.08.2025
 **/
public class PlayerDistributor {

    public static final String OUTPUT_DIR = "output";
    public static final String OUTPUT_NAME = "output";

    public static final String UNASSIGNED_ROLE = "OTHER";

    public static final String ACCEPTED = "ACCEPTED";

    public static boolean CHECK_MINECRAFT_NAMES = false;
    public static boolean REMOVE_UNCHECKED_ENTRIES = false;
    public static int GROUP_LIMIT = 32;
    public static GroupBuilder.LinkMode LINK_MODE = GroupBuilder.LinkMode.ANY_LINK;

    public static void main(String[] args) {

        // init logger
        try {
            LoggingUtil.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ConfigManager configManager = new ConfigManager(
                ConfigManager.getJarDirectory().resolve("config.properties")
        );

        if(!Files.exists(configManager.getConfigFile())) {
            LoggingUtil.info("No config found, generating default...");

            try {
                configManager.createDefault();
                LoggingUtil.info("Generated default config. Edit it and run again");
                return;
            } catch (Exception e) {
                LoggingUtil.error("Error generating default config");
                throw new RuntimeException(e);
            }
        }

        // load config
        if(!configManager.load()) {
            LoggingUtil.error("Error loading config");
            return;
        }

        if(!configManager.isValid()) {
            LoggingUtil.error("Config invalid");
            return;
        }

        // create output
        Path outputPath = ConfigManager.getJarDirectory().resolve(OUTPUT_DIR);
        if(!Files.exists(outputPath)) {
            try {
                Files.createDirectory(outputPath);
            } catch (IOException e) {
                LoggingUtil.error("Could not create output directory");
                return;
            }
        } else {
            if(!outputPath.toFile().isDirectory()) {
                LoggingUtil.error("Output directory can't be a File");
                System.exit(1);
            }
        }

        // find ExcelFile
        File excelFile = null;
        System.out.println(Arrays.toString(args));
        for (String arg: args) {
            if(arg.trim().replaceAll("-", "").equalsIgnoreCase("checkMCNames")) {
                CHECK_MINECRAFT_NAMES = true;
                continue;
            }

            if(arg.trim().replaceAll("-", "").equalsIgnoreCase("delUnchecked")) {
                CHECK_MINECRAFT_NAMES = false;
                REMOVE_UNCHECKED_ENTRIES = true;
                continue;
            }

            if(arg.trim().replaceAll("-", "").equalsIgnoreCase("requireMutual")) {
                LINK_MODE = GroupBuilder.LinkMode.MUTUAL;
                continue;
            }

            String[] parts = arg.split("=", 2);
            if(parts.length != 2) continue;
            String key = parts[0].trim().replaceAll("-", "");
            String value = parts[1].trim();

            if(key.equalsIgnoreCase("inputFile")) {
                excelFile = ExcelFileUtil.findFromPath(value);
            }

            if(key.equalsIgnoreCase("groupLimit")) {
                try {
                    GROUP_LIMIT = Integer.parseInt(value);
                } catch (Exception ignored) {}
            }

        }

        // fallback to first Excel file found
        if(excelFile == null) excelFile = ExcelFileUtil.findFirst();

        // no file, exit
        if(excelFile == null) {
            LoggingUtil.error("No valid Excel-File found");
            return;
        }

        // create ExcelHelpers
        ExcelHelper inputExcel = ExcelHelper.loadFile(excelFile, true);
        if(inputExcel == null) {
            LoggingUtil.error("something went wrong with loading the files");
            return;
        }

        Path outputFile = ConfigManager.getJarDirectory().resolve(OUTPUT_DIR + "/" + OUTPUT_NAME + ".xlsx");

        if(Files.exists(outputFile)) {
            LoggingUtil.error("Delete " + OUTPUT_NAME + ".xlsx in output directory and run again");
            System.exit(1);
        }

        try {
            inputExcel.save(outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ExcelHelper outputExcel = ExcelHelper.loadFile(outputFile.toFile(), false);

        // process files
        List<PlayerData> rawPlayerList = InputReader.loadPlayerData(outputExcel, configManager);
        LoggingUtil.info("Parsed " + rawPlayerList.size() + " players");

        // validate input
        List<PlayerData> validPlayerList = InputValidator.validateInput(configManager, rawPlayerList);
        LoggingUtil.info("Validated " + validPlayerList.size() + " players");

        if(REMOVE_UNCHECKED_ENTRIES) {

            // delete rejected players
            Sheet sheet = outputExcel.getSheet(configManager.getSheetName());
            int offset = 0;
            for(int y = configManager.getStartRow(); y <= configManager.getEndRow(); y++) {
                String include = outputExcel.readCell(sheet, y - offset, configManager.getIncludeCol());
                if(include == null || (!include.equalsIgnoreCase(ACCEPTED) && !InputReader.parseBool(include))) {
                    outputExcel.removeRow(sheet, y - offset);
                    offset++;
                }
            }

            try {
                outputExcel.save();
                outputExcel.close();

                LoggingUtil.info("Deleted rows with players that didn't pass the background check, " +
                        "were double entries, or simply lost the slot lottery");
                LoggingUtil.info("Run again without the --delUnchecked Flag to distribute players");
                LoggingUtil.close();
            } catch (IOException e) {
                LoggingUtil.error("Error deleting unchecked players");
                throw new RuntimeException(e);
            }
            System.exit(0);
            return;
        }

        // slot lottery, randomly select players until config#playerlimit
        List<PlayerData> acceptedPlayerList = Limiter.limitPlayerCount(configManager.getPlayerLimit(), validPlayerList);

        // preassign factions
        // includes friends of preassigned players
        List<PlayerData> playersWithoutFaction = FactionPreAssigner.filterAndPreAssignFactions(acceptedPlayerList);

        // group players
        List<PlayerGroup> groups = GroupBuilder.buildGroups(playersWithoutFaction);
        if(groups.isEmpty()) LoggingUtil.info("No groups found");
        LoggingUtil.info("Found " + groups.size() + " groups (largest: " + (groups.isEmpty() ? 0 : groups.get(0).size()) + ")");

        System.out.println(groups);

        // distribute players
        Distributor.DistributionResult distributionResult = Distributor.distribute(configManager, groups, acceptedPlayerList);
        LoggingUtil.info(distributionResult.factions.toString());

        LoggingUtil.info("TOTAL WARNINGS: " + LoggingUtil.getWarnings());
        if(LoggingUtil.getWarnings() > 0) {
            LoggingUtil.info("");
            LoggingUtil.info("FIX WARNINGS AND RUN AGAIN");
            LoggingUtil.info("");
        }

        // write distribution
        String sheet = configManager.getSheetName();
        int faction_col = configManager.getFactionCol();
        for(PlayerData player: distributionResult.distributedPlayers) {
            outputExcel.writeCell(outputExcel.getSheet(sheet), player.row, faction_col, player.faction);
            if(player.name.equals(InputValidator.MISSING_MC_NAME)) {
                outputExcel.writeCell(outputExcel.getSheet(sheet), player.row, configManager.getMCNameCol(), player.name);
            }
            if(player.role.equalsIgnoreCase(UNASSIGNED_ROLE)) {
                outputExcel.writeCell(outputExcel.getSheet(sheet), player.row, configManager.getRoleCol(), player.role);
            }

            if(player.accepted) {
                outputExcel.writeCell(outputExcel.getSheet(sheet), player.row, configManager.getIncludeCol(), ACCEPTED);
            }
        }
        try {
            outputExcel.save();
            outputExcel.close();

            LoggingUtil.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
