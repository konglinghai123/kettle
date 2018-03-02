package cn.benma666.myutils;

import cn.benma666.myutils.DateUtil;
import cn.benma666.myutils.StringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

   private static Log log = LogFactory.getLog(ExcelUtil.class);


   public static Workbook getWorkBook(String filePath, InputStream inputStream) throws IOException {
      if(filePath.toLowerCase().endsWith("xls")) {
         return new HSSFWorkbook(inputStream);
      } else if(filePath.toLowerCase().endsWith("xlsx")) {
         return new XSSFWorkbook(inputStream);
      } else {
         log.info("不是Excel文件：" + filePath);
         inputStream.close();
         throw new RuntimeException("不是Excel文件：" + filePath);
      }
   }

   public static boolean excelVaild(Workbook wb, boolean hasTitle) {
      boolean result = true;
      if(null == wb) {
         result = false;
         log.info("文件不符合要求!");
      } else if(wb.getNumberOfSheets() < 0) {
         result = false;
         log.info("文件内容为空!");
      } else {
         Sheet sheet = wb.getSheetAt(0);
         if(sheet.getPhysicalNumberOfRows() == 0) {
            result = false;
            log.info("除去首行表头后,文件内容为空!");
         } else if(hasTitle && sheet.getPhysicalNumberOfRows() <= 1) {
            result = false;
            log.info("除去首行表头后,文件内容为空!");
         }
      }

      return result;
   }

   public static String[][] excelPreview(Workbook wb, boolean hasTitle, int previewNum) {
      if(!excelVaild(wb, hasTitle)) {
         return (String[][])null;
      } else {
         Sheet sheet = wb.getSheetAt(0);
         int rows = sheet.getLastRowNum() + 1;
         short cells = sheet.getRow(0).getLastCellNum();
         int total = Math.min(rows, previewNum);
         String[][] result = new String[total][cells];

         for(int i = 0; i < total; ++i) {
            for(int j = 0; j < cells; ++j) {
               if(null != sheet.getRow(i) && null != sheet.getRow(i).getCell(j)) {
                  Cell cell = sheet.getRow(i).getCell(j);
                  if(cell.getCellType() == 0) {
                     if(HSSFDateUtil.isCellDateFormatted(cell)) {
                        Date var13 = cell.getDateCellValue();
                        String reTime = DateUtil.doFormatDate(var13, "yyyy-MM-dd HH:mm:ss");
                        result[i][j] = reTime;
                     } else {
                        result[i][j] = StringUtil.converDoubleToString(Double.valueOf(cell.getNumericCellValue()));
                     }
                  } else {
                     sheet.getRow(i).getCell(j).setCellType(1);
                     String value = sheet.getRow(i).getCell(j).getStringCellValue();
                     result[i][j] = null != value && !value.equals("")?value.trim():"";
                  }
               } else {
                  result[i][j] = "";
               }
            }
         }

         return result;
      }
   }

   public static int getTotalCount(Workbook wb, boolean hasTitle) {
      if(null == wb) {
         return 0;
      } else {
         Sheet sheet = wb.getSheetAt(0);
         return null != sheet && sheet.getPhysicalNumberOfRows() != 0?sheet.getLastRowNum() + 1:0;
      }
   }

   public static HSSFWorkbook createExcel03(List excelTops) {
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet sheet = workbook.createSheet();
      HSSFRow row = sheet.createRow(0);

      for(int i = 0; i < excelTops.size(); ++i) {
         sheet.setColumnWidth(i, 5000);
         String value = (String)excelTops.get(i);
         setCell(row, i, value);
      }

      return workbook;
   }

   public static XSSFWorkbook createExcel07(List excelTops) {
      XSSFWorkbook workbook = new XSSFWorkbook();
      XSSFSheet sheet = workbook.createSheet();
      XSSFRow row = sheet.createRow(0);

      for(int i = 0; i < excelTops.size(); ++i) {
         sheet.setColumnWidth(i, 5000);
         String value = (String)excelTops.get(i);
         setCell(row, i, value, workbook, true);
      }

      return workbook;
   }

   public static void setCell(Row row, int index, String value, XSSFWorkbook wb, boolean isTh) {
      XSSFCellStyle style = wb.createCellStyle();
      style.setAlignment(HorizontalAlignment.CENTER);
      style.setVerticalAlignment(VerticalAlignment.CENTER);
      if(isTh) {
         style.setFillForegroundColor((short)22);
         style.setFillPattern((short)1);
      }

      Cell cell = row.createCell(index);
      cell.setCellType(1);
      cell.setCellValue(value);
      cell.setCellStyle(style);
   }

   public static void setCell(Row row, int index, String value) {
      Cell cell = row.createCell(index);
      cell.setCellType(1);
      cell.setCellValue(value);
   }

   public static void downloadExcel(String fileName, Workbook workBook, HttpServletResponse resp) {
      resp.setContentType("application/octet-stream;charset=GBK");
      if(workBook != null) {
         try {
            String op = ".xls";
            if(workBook instanceof XSSFWorkbook) {
               op = ".xlsx";
            }

            resp.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("GBK"), "iso-8859-1") + op);
         } catch (UnsupportedEncodingException var15) {
            log.error("转换编码错误：" + fileName, var15);
         }

         ServletOutputStream op1 = null;

         try {
            op1 = resp.getOutputStream();
            workBook.write(op1);
         } catch (Exception var14) {
            log.error("向页面写入表格数据错误：" + fileName, var14);
         } finally {
            if(op1 != null) {
               try {
                  op1.flush();
                  op1.close();
               } catch (IOException var13) {
                  log.debug("向页面写入表格数据错误：" + fileName, var13);
               }
            }

         }
      }

   }

   public static List readExcel(InputStream inputStream, boolean isExcel2003, int maxCount, boolean shbhlmc) throws RuntimeException {
      if(inputStream == null) {
         return null;
      } else {
         try {
            return readWorkBook((Workbook)(isExcel2003?new HSSFWorkbook(inputStream):new XSSFWorkbook(inputStream)), maxCount, shbhlmc);
         } catch (IOException var5) {
            log.error("文件不存在：" + inputStream.toString(), var5);
            throw new RuntimeException("读取文件异常", var5);
         }
      }
   }

   public static int readExcelRowCount(InputStream inputStream, boolean isExcel2003, boolean shbhlmc) throws IOException {
      if(inputStream == null) {
         return 0;
      } else {
         Object wb = isExcel2003?new HSSFWorkbook(inputStream):new XSSFWorkbook(inputStream);
         Sheet sheet = ((Workbook)wb).getSheetAt(0);
         return shbhlmc?sheet.getLastRowNum():sheet.getLastRowNum() + 1;
      }
   }

   private static List readWorkBook(Workbook wb, int maxCount, boolean shbhlmc) throws RuntimeException {
      ArrayList dataList = new ArrayList();
      Sheet sheet = wb.getSheetAt(0);
      int totalCells = sheet.getRow(0).getLastCellNum() + 1;
      maxCount = shbhlmc?maxCount + 1:maxCount;

      for(int rowIndex = shbhlmc?1:0; rowIndex < maxCount; ++rowIndex) {
         Row row = sheet.getRow(rowIndex);
         if(row != null) {
            ArrayList cellArray = new ArrayList();
            int emptyCellNum = 0;

            for(int cellIndex = 0; cellIndex < totalCells; ++cellIndex) {
               Cell cell = row.getCell(cellIndex);
               String cellValue = "";
               if(cell != null && 3 != cell.getCellType()) {
                  Object var13;
                  if(0 == cell.getCellType()) {
                     if(HSSFDateUtil.isCellDateFormatted(cell)) {
                        var13 = DateUtil.doFormatDate(cell.getDateCellValue(), "yyyy-MM-dd HH:mm:ss");
                     } else {
                        var13 = StringUtil.converDoubleToString(Double.valueOf(cell.getNumericCellValue()));
                     }
                  } else if(1 == cell.getCellType()) {
                     var13 = cell.getStringCellValue().replace(" ", "").replace(" ", "");
                  } else if(4 == cell.getCellType()) {
                     var13 = Boolean.valueOf(cell.getBooleanCellValue());
                  } else {
                     var13 = cell.toString();
                  }

                  cellArray.add(var13);
               } else {
                  cellArray.add(cellValue);
                  ++emptyCellNum;
               }
            }

            if(emptyCellNum != totalCells) {
               dataList.add(cellArray);
            }
         }
      }

      return dataList;
   }

   public static void resultSetToExcel(ResultSet rs, String sheetName, HSSFWorkbook workbook) throws Exception {
      HSSFSheet sheet = workbook.createSheet(sheetName);
      HSSFRow row = sheet.createRow(0);
      ResultSetMetaData md = rs.getMetaData();
      int nColumn = md.getColumnCount();

      HSSFCell cell;
      int iRow;
      for(iRow = 1; iRow <= nColumn; ++iRow) {
         cell = row.createCell(iRow - 1);
         cell.setCellType(1);
         cell.setCellValue(md.getColumnLabel(iRow));
      }

      for(iRow = 1; rs.next(); ++iRow) {
         row = sheet.createRow(iRow);

         for(int j = 1; j <= nColumn; ++j) {
            cell = row.createCell(j - 1);
            cell.setCellValue(rs.getObject(j).toString());
         }
      }

   }

   public static Map ExcelTotable(File file) throws Exception {
      HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));
      HashMap tables = new HashMap();
      int i = 0;

      while(i < workbook.getNumberOfSheets()) {
         ArrayList row = new ArrayList();
         HSSFSheet sheet = workbook.getSheetAt(i);
         tables.put(sheet.getSheetName(), row);
         int r = 0;

         label63:
         while(true) {
            if(i < sheet.getLastRowNum()) {
               HSSFRow srow = sheet.getRow(r);
               if(srow != null) {
                  ArrayList column = new ArrayList();
                  row.add(column);
                  int c = 0;

                  while(true) {
                     if(c < srow.getLastCellNum() + 1) {
                        String value = "";
                        HSSFCell cell = srow.getCell(c);
                        if(cell != null) {
                           switch(cell.getCellType()) {
                           case 0:
                              if(HSSFDateUtil.isCellDateFormatted(cell)) {
                                 Date date = cell.getDateCellValue();
                                 if(date != null) {
                                    value = (new SimpleDateFormat("yyyy-MM-dd")).format(date);
                                 } else {
                                    value = "";
                                 }
                              } else {
                                 value = (new DecimalFormat("0")).format(cell.getNumericCellValue());
                              }
                              break;
                           case 1:
                              value = cell.getStringCellValue();
                              break;
                           case 2:
                              if(!cell.getStringCellValue().equals("")) {
                                 value = cell.getStringCellValue();
                              } else {
                                 value = cell.getNumericCellValue() + "";
                              }
                           case 3:
                              break;
                           case 4:
                              value = cell.getBooleanCellValue()?"Y":"N";
                              break;
                           case 5:
                              value = "";
                              break;
                           default:
                              value = "";
                           }
                        }

                        if(c != 0 || !value.trim().equals("")) {
                           column.add(rightTrim(value));
                           ++c;
                           continue;
                        }
                     }

                     ++r;
                     continue label63;
                  }
               }
            }

            ++i;
            break;
         }
      }

      return tables;
   }

   public static String rightTrim(String str) {
      if(str == null) {
         return "";
      } else {
         int length = str.length();

         for(int i = length - 1; i >= 0 && str.charAt(i) == 32; --i) {
            --length;
         }

         return str.substring(0, length);
      }
   }

}
