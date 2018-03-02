package org.beetl.sql.core.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import org.beetl.sql.core.SQLSource;
import org.beetl.sql.core.annotatoin.AssignID;
import org.beetl.sql.core.annotatoin.SeqID;
import org.beetl.sql.core.db.AbstractDBStyle;

public class OracleStyle extends AbstractDBStyle {

   public String getPageSQL(String sql) {
      String pageSql = "SELECT * FROM  (  SELECT beeltT.*, ROWNUM beetl_rn  FROM ( \n" + sql + this.getOrderBy() + "\n )  beeltT " + " WHERE ROWNUM <" + this.HOLDER_START + "_pageEnd" + this.HOLDER_END + ") " + "WHERE beetl_rn >= " + this.HOLDER_START + "_pageOffset" + this.HOLDER_END;
      return pageSql;
   }

   public void initPagePara(Map paras, long start, long size) {
      long s = start + (long)(this.offsetStartZero?1:0);
      paras.put("_pageOffset", Long.valueOf(s));
      paras.put("_pageEnd", Long.valueOf(s + size));
   }

   public int getIdType(Method idMethod) {
      Annotation[] ans = idMethod.getAnnotations();
      byte idType = 1;
      Annotation[] arr$ = ans;
      int len$ = ans.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Annotation an = arr$[i$];
         if(an instanceof SeqID) {
            idType = 3;
            break;
         }

         if(an instanceof AssignID) {
            idType = 1;
         }
      }

      return idType;
   }

   public String getName() {
      return "oracle";
   }

   public int getDBType() {
      return 2;
   }

   public SQLSource genSelectVal(String val) {
      return new SQLSource("select " + val + " as val from dual");
   }

   public String Date14Exp() {
      return "to_char(sysdate,\'yyyymmddhh24miss\')";
   }
}
