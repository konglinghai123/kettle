package cn.benma666.myutils;

import cn.benma666.myutils.ExcelUtil;
import cn.benma666.myutils.FileUtil;
import cn.benma666.myutils.XlsxReader;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelReader extends XlsxReader {

   Log log = LogFactory.getLog(this.getClass());
   private String excelPath = "";
   private int colCount = 0;


   public boolean disposeExcel() throws Exception {
      return this.disposeExcel(this.excelPath);
   }

   public boolean disposeExcel(String excelPath) throws Exception {
      this.excelPath = excelPath;
      if(excelPath.endsWith("xlsx")) {
         super.readOneSheet(excelPath);
      } else {
         Workbook wb = null;
         FileInputStream excelInput = null;
         String[][] previewData = (String[][])null;

         try {
            excelInput = new FileInputStream(excelPath);
            wb = ExcelUtil.getWorkBook(excelPath, excelInput);
            previewData = ExcelUtil.excelPreview(wb, true, Integer.MAX_VALUE);
            String[][] arr$ = previewData;
            int len$ = previewData.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               String[] rows = arr$[i$];
               if(this.curRow == 0) {
                  this.doHeard(Arrays.asList(rows));
               } else {
                  this.doRow(rows);
               }

               ++this.curRow;
            }
         } finally {
            FileUtil.closeInputStream(excelInput);
         }
      }

      return true;
   }

   public void optRow(int sheetIndex, int curRow, List rowList) throws RuntimeException {
      if(curRow == 0) {
         this.doHeard(rowList);
      } else {
         for(int i = rowList.size(); i < this.colCount; ++i) {
            rowList.add("");
         }

         this.doRow(rowList);
      }

   }

   protected void doHeard(List colNameList) throws RuntimeException {
      this.log.info(this.excelPath + "文件头信息：" + colNameList);
      if(this.colCount == 0) {
         this.colCount = colNameList.size();
      }

   }

   protected void doRow(List rowList) throws RuntimeException {
      this.log.info(this.curRow + "：" + rowList);
   }

   private void doRow(String[] rowList) {
      this.doRow(Arrays.asList(rowList));
   }

   public String getExcelPath() {
      return this.excelPath;
   }

   public void setExcelPath(String excelPath) {
      this.excelPath = excelPath;
   }

   public int getColCount() {
      return this.colCount;
   }

   public void setColCount(int colCount) {
      this.colCount = colCount;
   }
}
