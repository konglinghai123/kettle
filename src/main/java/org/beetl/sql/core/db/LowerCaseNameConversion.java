package org.beetl.sql.core.db;

import org.beetl.sql.core.NameConversion;
import org.beetl.sql.core.annotatoin.Table;
import org.beetl.sql.core.kit.StringKit;

public class LowerCaseNameConversion extends NameConversion {

   public String getTableName(Class c) {
      Table table = (Table)c.getAnnotation(Table.class);
      return table != null?table.name():StringKit.enCodeUnderlined(c.getSimpleName());
   }

   public String getClassName(String tableName) {
      String temp = StringKit.deCodeUnderlined(tableName.toLowerCase());
      return StringKit.toUpperCaseFirstOne(temp);
   }

   public String getColName(Class c, String attrName) {
      return attrName.toLowerCase();
   }

   public String getPropertyName(Class c, String colName) {
      return colName.toLowerCase().toLowerCase();
   }
}
