package karnickeldev.playerdistributor.excel;

import karnickeldev.playerdistributor.config.ConfigManager;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class ExcelFileUtil {

    /**
     * Creates a Wrapper for the Excel File to process
     * Uses first Excel File found in the Directory
     * @return A FileManager for the Excel File or null on error
     */
    public static File findFirst() {
        Path jarDir = ConfigManager.getJarDirectory();
        if(jarDir.toFile().listFiles() == null) return null;

        File[] files = jarDir.toFile().listFiles();

        assert files != null;
        for(File file: files) {
            if(file.exists() && isExcelFile(file)) {
                return file;
            }
        }

        return null;
    }

    /**
     * Creates a Wrapper for the Excel File to process
     * @param inputFilePath Path of the Excel File
     * @return A FileManager for the Excel File or null on error
     */
    public static File findFromPath(String inputFilePath) {
        File file = new File(inputFilePath);
        if(file.exists() && isExcelFile(file)) {
            return file;
        } else {
            return null;
        }
    }

    public static boolean isExcelFile(File file) {
        // Quick extension check
        String name = file.getName().toLowerCase();
        if (!(name.endsWith(".xls") || name.endsWith(".xlsx"))) {
            return false;
        }
        // Try opening with POI for reliability
        try (FileInputStream fis = new FileInputStream(file)) {

            // Try old Excel format (.xls)
            try {
                POIFSFileSystem poifs = new POIFSFileSystem(fis);
                return true;
            } catch (Exception ignored) {}

            // Reset stream for new Excel format (.xlsx)
            try (FileInputStream fis2 = new FileInputStream(file)) {
                OPCPackage opc = OPCPackage.open(fis2);
                opc.close();
                return true;
            } catch (Exception ignored) {}

        } catch (Exception ignored) {}

        return false;
    }



}
