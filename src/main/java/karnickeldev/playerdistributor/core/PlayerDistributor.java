package karnickeldev.playerdistributor.core;

import karnickeldev.playerdistributor.config.ConfigManager;
import karnickeldev.playerdistributor.distribution.*;
import karnickeldev.playerdistributor.excel.ExcelFileUtil;
import karnickeldev.playerdistributor.excel.ExcelHelper;

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

    public static final String PREFIX = "[PlayerDistributor]";
    public static final String OUTPUT_DIR = "output";
    public static final String OUTPUT_NAME = "output";

    public static void main(String[] args) {

        ConfigManager configManager = new ConfigManager(
                ConfigManager.getJarDirectory().resolve("config.properties")
        );

        // load config
        try {
            configManager.init();
        } catch (IOException e) {
            System.err.println("Error loading config");
            throw new RuntimeException(e);
        }

        if(!configManager.isValid()) {
            System.out.println("Config invalid");
            return;
        }

        // create output
        Path outputPath = ConfigManager.getJarDirectory().resolve(OUTPUT_DIR);
        if(!Files.exists(outputPath)) {
            try {
                Files.createDirectory(outputPath);
            } catch (IOException e) {
                System.out.println("Could not create output directory");
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
            System.out.println("No valid Excel-File found");
            return;
        }

        // create ExcelHelpers
        ExcelHelper inputExcel = ExcelHelper.loadFile(excelFile, true);
        if(inputExcel == null) {
            System.out.println("something went wrong with loading the files");
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
        PlayerList playerList = InputReader.loadPlayerData(outputExcel, configManager);
        System.out.println("Parsed " + playerList.getUnfactionedPlayers().size() + " players without a faction");

        List<PlayerGroup> groups = GroupBuilder.buildGroups(playerList.getUnfactionedPlayers());
        if(groups.isEmpty()) System.out.println("No groups found");
        System.out.println("Found " + groups.size() + " groups (largest: " + (groups.isEmpty() ? 0 : groups.get(0).size()) + ")");

        Distributor.DistributionResult distributionResult = Distributor.distribute(configManager, groups, playerList);
        System.out.println(distributionResult.factions);

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
    }

}
