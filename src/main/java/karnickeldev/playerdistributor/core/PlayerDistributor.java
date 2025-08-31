package karnickeldev.playerdistributor.core;

import karnickeldev.playerdistributor.config.ConfigManager;
import karnickeldev.playerdistributor.distribution.*;
import karnickeldev.playerdistributor.excel.ExcelFileUtil;
import karnickeldev.playerdistributor.excel.ExcelHelper;
import karnickeldev.playerdistributor.parsing.InputReader;
import karnickeldev.playerdistributor.parsing.InputValidator;
import karnickeldev.playerdistributor.util.LoggingUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author : KarnickelDev
 * @since : 29.08.2025
 **/
public class PlayerDistributor {

    public static final String OUTPUT_DIR = "output";
    public static final String OUTPUT_NAME = "output";

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
        }

        // find ExcelFile
        File excelFile = null;
        for (String arg: args) {
            String[] parts = arg.split("=", 2);
            if(parts.length != 2) continue;
            String key = parts[0].trim();
            String value = parts[1].trim();

            if(key.equalsIgnoreCase("inputFile")) {
                excelFile = ExcelFileUtil.findFromPath(value);
                break;
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

        // preassign factions
        // includes friends of preassigned players
        List<PlayerData> playersWithoutFaction = FactionPreAssigner.filterAndPreAssignFactions(validPlayerList);

        // group players
        List<PlayerGroup> groups = GroupBuilder.buildGroups(playersWithoutFaction);
        if(groups.isEmpty()) LoggingUtil.info("No groups found");
        LoggingUtil.info("Found " + groups.size() + " groups (largest: " + (groups.isEmpty() ? 0 : groups.get(0).size()) + ")");

        // distribute players
        Distributor.DistributionResult distributionResult = Distributor.distribute(configManager, groups, validPlayerList);
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
        }
        try {
            outputExcel.save();
            outputExcel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try {
            LoggingUtil.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
