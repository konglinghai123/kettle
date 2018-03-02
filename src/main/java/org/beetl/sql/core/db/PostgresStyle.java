package org.beetl.sql.core.db;

import java.util.Map;
import org.beetl.sql.core.db.AbstractDBStyle;

public class PostgresStyle extends AbstractDBStyle {

   public String getPageSQL(String sql) {
      String pageSql = "select _a.* from ( \n" + sql + this.getOrderBy() + " \n) _a " + " limit " + this.HOLDER_START + "_pageSize" + this.HOLDER_END + " offset " + this.HOLDER_START + "_pageOffset" + this.HOLDER_END;
      return pageSql;
   }

   public void initPagePara(Map paras, long start, long size) {
      paras.put("_pageOffset", Long.valueOf(start - (long)(this.offsetStartZero?0:1)));
      paras.put("_pageSize", Long.valueOf(size));
   }

   public String getName() {
      return "postgres";
   }

   public int getDBType() {
      return 3;
   }

   public String Date14Exp() {
      return "to_char(now(),\'YYYYMMDDHH24MISS\')";
   }
}
