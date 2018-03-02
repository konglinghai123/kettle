package cn.benma666.myutils;

import cn.benma666.myutils.DateUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XlsxReader extends DefaultHandler {

   Log log = LogFactory.getLog(this.getClass());
   private SharedStringsTable sst;
   private String lastContents;
   private boolean nextIsString;
   private int sheetIndex = -1;
   private List rowlist = new ArrayList();
   protected int curRow = 0;
   private int curCol = 0;
   protected OPCPackage pkg;
   protected InputStream sheet;
   private String cellType;
   private int cellS;
   private StylesTable stylesTable;


   public void readOneSheet(String path) throws Exception {
      this.pkg = OPCPackage.open(path, PackageAccess.READ);
      XSSFReader r = new XSSFReader(this.pkg);
      SharedStringsTable sst = r.getSharedStringsTable();
      this.stylesTable = r.getStylesTable();
      XMLReader parser = this.fetchSheetParser(sst);
      this.sheet = r.getSheet("rId1");
      InputSource sheetSource = new InputSource(this.sheet);

      try {
         parser.parse(sheetSource);
      } finally {
         if(this.sheet != null) {
            try {
               this.sheet.close();
            } catch (Exception var15) {
               this.log.info("关闭excel失败，" + var15.getMessage());
            }
         }

         if(this.pkg != null) {
            try {
               this.pkg.close();
            } catch (Exception var14) {
               this.log.info("关闭excel失败，" + var14.getMessage());
            }
         }

      }

   }

   public void process(String path) throws Exception {
      OPCPackage pkg = OPCPackage.open(path);
      XSSFReader r = new XSSFReader(pkg);
      SharedStringsTable sst = r.getSharedStringsTable();
      XMLReader parser = this.fetchSheetParser(sst);
      Iterator sheets = r.getSheetsData();

      while(sheets.hasNext()) {
         this.curRow = 0;
         ++this.sheetIndex;
         InputStream sheet = (InputStream)sheets.next();
         InputSource sheetSource = new InputSource(sheet);
         parser.parse(sheetSource);
         sheet.close();
      }

   }

   public void optRow(int sheetIndex, int curRow, List rowList) throws RuntimeException {
      String temp = "";

      String str;
      for(Iterator i$ = rowList.iterator(); i$.hasNext(); temp = temp + str + "_") {
         str = (String)i$.next();
      }

      System.out.println(curRow + "||" + temp);
   }

   public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
      XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
      this.sst = sst;
      parser.setContentHandler(this);
      return parser;
   }

   public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
      if(name.equals("c")) {
         String cellLocation = attributes.getValue("r");
         this.curCol = extractColumnNumber(cellLocation) - 1;
         this.cellType = attributes.getValue("t");
         if(this.cellType != null && this.cellType.equals("s")) {
            this.nextIsString = true;
         } else {
            this.nextIsString = false;
         }

         this.cellS = attributes.getValue("s") == null?-1:Integer.parseInt(attributes.getValue("s"));
      }

      this.lastContents = "";
   }

   public static int extractColumnNumber(String position) {
      int startIndex;
      for(startIndex = 0; !Character.isDigit(position.charAt(startIndex)) && startIndex < position.length(); ++startIndex) {
         ;
      }

      String colPart = position.substring(0, startIndex);
      return parseColumnNumber(colPart);
   }

   public static final int parseColumnNumber(String columnIndicator) {
      int col = 0;

      for(int i = columnIndicator.length() - 1; i >= 0; --i) {
         char c = columnIndicator.charAt(i);
         int offset = 1 + Character.getNumericValue(c) - Character.getNumericValue('A');
         col = (int)((double)col + Math.pow(26.0D, (double)(columnIndicator.length() - i - 1)) * (double)offset);
      }

      return col;
   }

   public void endElement(String uri, String localName, String name) throws SAXException {
      if(this.nextIsString && name.equals("v")) {
         try {
            int value = Integer.parseInt(this.lastContents);
            this.lastContents = (new XSSFRichTextString(this.sst.getEntryAt(value))).toString();
         } catch (Exception var9) {
            this.log.error("获取单元格字符串值失败", var9);
         }
      }

      if(name.equals("v")) {
         try {
            if(this.cellType == null && this.cellS > 0) {
               long var10 = this.stylesTable.getCellXfAt(this.cellS).getNumFmtId();
               if(this.cellIsDate(var10)) {
                  double d = Double.parseDouble(this.lastContents);
                  if(org.apache.poi.ss.usermodel.DateUtil.isValidExcelDate(d)) {
                     this.lastContents = DateUtil.doFormatDate(org.apache.poi.ss.usermodel.DateUtil.getJavaDate(d, false), "yyyy-MM-dd HH:mm:ss");
                  }
               }
            }
         } catch (Exception var8) {
            this.log.debug("解析为时间错误", var8);
         }

         String var11 = this.lastContents.trim();
         var11 = var11 == null?"":var11;

         while(this.rowlist.size() < this.curCol) {
            this.rowlist.add("");
         }

         this.rowlist.add(this.curCol, var11.trim());
         ++this.curCol;
      } else if(name.equals("row")) {
         this.optRow(this.sheetIndex, this.curRow, this.rowlist);
         this.rowlist.clear();
         ++this.curRow;
         this.curCol = 0;
      }

   }

   public boolean cellIsDate(long numFmtId) {
      String numFmt = this.stylesTable.getNumberFormatAt((int)numFmtId);
      if(numFmt == null) {
         return numFmtId == 22L || numFmtId == 14L;
      } else {
         numFmt = numFmt.toLowerCase();
         return numFmt.indexOf("yy") > -1 && numFmt.indexOf("m") > -1 || numFmt.indexOf("h") > -1 && numFmt.indexOf("m") > -1 || numFmt.indexOf("d") > -1 && numFmt.indexOf("m") > -1;
      }
   }

   public void characters(char[] ch, int start, int length) throws SAXException {
      this.lastContents = this.lastContents + new String(ch, start, length);
   }
}
