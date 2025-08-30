package karnickeldev.playerdistributor.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author : KarnickelDev
 * @since : 30.08.2025
 **/
public class ExcelHelper {

    public static ExcelHelper loadFile(File file, boolean readOnly) {
        if(file == null) return null;
        if(!file.exists() || !ExcelFileUtil.isExcelFile(file)) return null;

        Workbook workbook;
        try (FileInputStream fis = new FileInputStream(file)) {
            workbook = WorkbookFactory.create(fis);
        } catch (Exception e) {
            System.out.println("Error reading file " + file.getName());
            throw new RuntimeException(e);
        }
        if(workbook == null) {
            System.err.println("Unexpected error creating workbook");
            System.exit(0);
        }
        return new ExcelHelper(file.toPath(), workbook, readOnly);
    }

    public static ExcelHelper createNew(Path path, String fileName) {
        if(new File(path.toFile(), fileName).exists()) {
            System.out.println("output file " + fileName + " already exists");
            System.exit(0);
        }

        Path excelFile = path.resolve(fileName);

        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }

            if (Files.exists(excelFile)) {
                Files.delete(excelFile);
            }

            Files.createFile(excelFile);
        } catch (Exception e) {
            System.out.println("Error creating output, try again");
            throw new RuntimeException(e);
        }

        return new ExcelHelper(excelFile, new XSSFWorkbook(), false);
    }

    private final Path path;
    private final Workbook workbook;
    private final boolean readOnly;
    private ExcelHelper(Path path, Workbook workbook, boolean readOnly) {
        this.path = path;
        this.workbook = workbook;
        this.readOnly = readOnly;
    }

    public Path getPath() {
        return path;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Get a sheet by name
     */
    public Sheet getSheet(String sheetName) {
        return workbook.getSheet(sheetName);
    }

    /**
     * Read a cell value as String (supports numbers, booleans, strings).
     */
    public String readCell(Sheet sheet, int rowIndex, int colIndex) {
        if(sheet == null) {
            System.out.println("Tried reading from empty sheet");
            return null;
        }

        Row row = sheet.getRow(rowIndex);
        if (row == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING, FORMULA -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    /**
     * Write a value to a cell.
     */
    public void writeCell(Sheet sheet, int rowIndex, int colIndex, String value) {
        if(sheet == null) {
            System.out.println("Tried writing to empty sheet");
            return;
        }
        if(readOnly) {
            System.out.println("Tried writing to readOnly workbook");
            return;
        }

        Row row = sheet.getRow(rowIndex);
        if (row == null) row = sheet.createRow(rowIndex);

        Cell cell = row.getCell(colIndex);
        if (cell == null) cell = row.createCell(colIndex);

        cell.setCellValue(value);
    }

    /**
     * Save the Excel file to disk.
     */
    public void save(Path path) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            workbook.write(fos);
        }
    }

    public void save() throws IOException {
        save(path);
    }

    /**
     * Close the workbook to free resources.
     */
    public void close() throws IOException {
        workbook.close();
    }

}
