package com.DataSoft.DataShift.utils;

import com.DataSoft.DataShift.models.AutomationRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class XLUtility {
    String path;
    public XLUtility() {
    }
    public XLUtility(String path) {
        this.path = path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String[][] getData(String inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();
            int colCount = sheet.getRow(0).getLastCellNum();
            String[][] data = new String[rowCount][colCount];

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            DataFormatter formatter = new DataFormatter();

            for (int r = 1; r <= rowCount; r++) {
                Row row = sheet.getRow(r);
                for (int c = 0; c < colCount; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                data[r - 1][c] = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    Date dateValue = cell.getDateCellValue();
                                    //System.out.print(dateFormatter.format(dateValue) + "\t");
                                    data[r - 1][c] = dateFormatter.format(dateValue);
                                } else {
                                    double numericValue = cell.getNumericCellValue();
                                    if (numericValue == (long) numericValue) {
                                        data[r - 1][c] = String.format("%.0f", numericValue);
                                    } else {
                                        data[r - 1][c] = String.valueOf(numericValue);
                                    }
                                }
                                break;
                        }
                    } else {
                        System.out.print("(Null)" + " || ");
                    }
                }
                System.out.println();
            }
            return data;
        }

    }

    public void setCellData(String sheetName, int rowNum, int colNum, String data) throws IOException {
        FileInputStream fi;
        FileOutputStream fo;
        XSSFWorkbook workbook;
        XSSFSheet sheet;
        XSSFRow row;
        XSSFCell cell;
        File xlfile = new File(path);
        if (!xlfile.exists())    // If file not exists then create new file
        {
            workbook = new XSSFWorkbook();
            fo = new FileOutputStream(path);
            workbook.write(fo);
        }

        fi = new FileInputStream(path);
        workbook = new XSSFWorkbook(fi);

        if (workbook.getSheetIndex(sheetName) == -1) // If sheet not exists then create new Sheet
            workbook.createSheet(sheetName);

        sheet = workbook.getSheet(sheetName);

        if (sheet.getRow(rowNum) == null)   // If row not exists then create new Row
            sheet.createRow(rowNum);
        row = sheet.getRow(rowNum);

        cell = row.createCell(colNum);
        cell.setCellValue(data);
        fo = new FileOutputStream(path);
        workbook.write(fo);
        workbook.close();
        fi.close();
        fo.close();
    }

//    public int getLastRowNum(String countPath) throws IOException {
//        FileInputStream fi = new FileInputStream(countPath);
//        XSSFWorkbook workbook = new XSSFWorkbook(fi);
//        XSSFSheet sheet = workbook.getSheetAt(0);
//        int lastRowNum = sheet.getLastRowNum();
//        workbook.close();
//        fi.close();
//        return lastRowNum;
//    }


    public int getLastRowNum(String countPath) throws IOException {
        String fileExtension = getFileExtension(countPath);

        switch (fileExtension.toLowerCase()) {
            case "csv":
                return countCSVRows(countPath);
            case "xlsx":
            case "xls":
                return countExcelRows(countPath);
            default:
                throw new IllegalArgumentException("Unsupported file format");
        }
    }

    private String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filePath.substring(lastDotIndex + 1);
    }

    private int countCSVRows(String filePath) throws IOException {
        int rowCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip the header row
            reader.readLine();
            while (reader.readLine() != null) {
                rowCount++;
            }
        }
        return rowCount;
    }

    private int countExcelRows(String filePath) throws IOException {
        FileInputStream fi = new FileInputStream(filePath);
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(fi);
            Sheet sheet = workbook.getSheetAt(0);
            // Skip the header row, so start counting from index 1
            return sheet.getLastRowNum(); // getLastRowNum() returns 0-based index
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            fi.close();
        }
    }

    public List<Map<String, String>> readExcelData(String filePath) {
        List<Map<String, String>> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            int colCount = headerRow.getPhysicalNumberOfCells();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                Map<String, String> rowData = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order
                for (int j = 0; j < colCount; j++) {
                    Cell cell = row.getCell(j);
                    String header = headerRow.getCell(j).getStringCellValue();
                    String value = cell != null ? cell.toString() : "";
                    rowData.put(header, value);
                }
                data.add(rowData);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

}
