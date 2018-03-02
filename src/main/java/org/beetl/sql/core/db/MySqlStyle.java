package org.beetl.sql.core.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import org.beetl.sql.core.annotatoin.AssignID;
import org.beetl.sql.core.annotatoin.AutoID;
import org.beetl.sql.core.annotatoin.SeqID;
import org.beetl.sql.core.db.AbstractDBStyle;
import org.beetl.sql.core.db.KeyWordHandler;

public class MySqlStyle extends AbstractDBStyle {

   public String getPageSQL(String sql) {
      return sql + this.getOrderBy() + " \nlimit " + this.HOLDER_START + "_pageOffset" + this.HOLDER_END + " , " + this.HOLDER_START + "_pageSize" + this.HOLDER_END;
   }

   public void initPagePara(Map param, long start, long size) {
      param.put("_pageOffset", Long.valueOf(start - (long)(this.offsetStartZero?0:1)));
      param.put("_pageSize", Long.valueOf(size));
   }

   public MySqlStyle() {
      this.keyWordHandler = new KeyWordHandler() {
         public String getTable(String tableName) {
            return "`" + tableName + "`";
         }
         public String getCol(String colName) {
            return "`" + colName + "`";
         }
      };
   }

   public int getIdType(Method idMethod) {
      Annotation[] ans = idMethod.getAnnotations();
      byte idType = 2;
      Annotation[] arr$ = ans;
      int len$ = ans.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Annotation an = arr$[i$];
         if(an instanceof AutoID) {
            idType = 2;
            break;
         }

         if(!(an instanceof SeqID) && an instanceof AssignID) {
            idType = 1;
         }
      }

      return idType;
   }

   public String getName() {
      return "mysql";
   }

   public int getDBType() {
      return 1;
   }

   public String Date14Exp() {
      return "DATE_FORMAT(NOW(),\'%Y%m%d%H%i%S\')";
   }
}
