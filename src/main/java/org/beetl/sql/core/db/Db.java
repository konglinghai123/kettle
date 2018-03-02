package org.beetl.sql.core.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.fastjson.JSONObject;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.beetl.sql.core.ClasspathLoader;
import org.beetl.sql.core.ConnectionSource;
import org.beetl.sql.core.ConnectionSourceHelper;
import org.beetl.sql.core.Interceptor;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLReady;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.core.db.LowerCaseNameConversion;
import org.beetl.sql.core.db.MySqlStyle;
import org.beetl.sql.core.db.OracleStyle;
import org.beetl.sql.core.db.PostgresStyle;
import org.beetl.sql.ext.DebugInterceptor;

public class Db {

   private static Map dbMap = new HashMap();
   public static Db db = null;
   private SQLManager sqlManager;
   private String dbType;


   public Db(String name, DruidDataSource dataSource) {
      this(name, ConnectionSourceHelper.getSingle(dataSource), dataSource.getDbType());
   }

   public Db(String name, DataSource dataSource, String dbType) {
      this(name, ConnectionSourceHelper.getSingle(dataSource), dbType);
   }

   public Db(String name, String url, String user, String pwd) throws SQLException {
      this(name, ConnectionSourceHelper.getSimple(JdbcUtils.getDriverClassName(url), url, user, pwd), JdbcUtils.getDbType(url, JdbcUtils.getDriverClassName(url)));
   }

   public Db(String name, ConnectionSource source, String dbType) {
      this.sqlManager = null;
      this.dbType = null;
      this.dbType = dbType;
      Object dbStyle = null;
      if("oracle".equals(dbType)) {
         dbStyle = new OracleStyle();
      } else if("mysql".equals(dbType)) {
         dbStyle = new MySqlStyle();
      } else if("postgresql".equals(dbType)) {
         dbStyle = new PostgresStyle();
      }

      ClasspathLoader loader = new ClasspathLoader("sql");
      LowerCaseNameConversion nc = new LowerCaseNameConversion();
      this.sqlManager = new SQLManager((DBStyle)dbStyle, loader, source, nc, new Interceptor[]{new DebugInterceptor()});
      if(db == null) {
         db = this;
      }

      dbMap.put(name, this);
   }

   public static Db use(String name) {
      return (Db)dbMap.get(name);
   }

   public static Db use() {
      return db;
   }

   public int update(String sql, Object ... params) {
      return this.sqlManager.executeUpdate(new SQLReady(sql, params));
   }

   public int update(String sql, Map params) {
      return this.sqlManager.update(sql, params);
   }

   public List find(String sql, Object ... params) {
      return this.sqlManager.execute(new SQLReady(sql, params), JSONObject.class);
   }

   public List find(String sql, Map params) {
      return this.sqlManager.select(sql, JSONObject.class, params);
   }

   public JSONObject findFirst(String sql, Object ... params) {
      List list = this.find(sql, params);
      return list.size() > 0?(JSONObject)list.get(0):null;
   }

   public JSONObject findFirst(String sql, Map params) {
      List list = this.find(sql, params);
      return list.size() > 0?(JSONObject)list.get(0):null;
   }

   public Map findMap(String keyName, String sql, Object ... params) {
      List list = this.find(sql, params);
      HashMap result = new HashMap();
      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         JSONObject jo = (JSONObject)var7.next();
         result.put(jo.getString(keyName), jo);
      }

      return result;
   }

   public Map findMap(String keyName, String sql, Map params) {
      List list = this.find(sql, params);
      HashMap result = new HashMap();
      Iterator var7 = list.iterator();

      while(var7.hasNext()) {
         JSONObject jo = (JSONObject)var7.next();
         result.put(jo.getString(keyName), jo);
      }

      return result;
   }

   public String queryStr(String sql, Object ... params) {
      JSONObject obj = this.findFirst(sql, params);
      return obj.size() > 0?obj.values().toArray()[0].toString():null;
   }

   public String queryStr(String sql, Map params) {
      JSONObject obj = this.findFirst(sql, params);
      return obj.size() > 0?obj.values().toArray()[0].toString():null;
   }

   public String getCurrentDateStr14() {
      String result = this.queryStr(this.sqlManager.getDbStyle().genSelectVal(this.sqlManager.getDbStyle().Date14Exp()).getTemplate(), new Object[0]);
      return result;
   }

   public static Map buildMap(Object ... params) {
      HashMap ps = new HashMap();

      for(int i = 0; i < params.length; ++i) {
         ps.put("p_" + (i + 1), params[i]);
      }

      return ps;
   }

   public static String getDbtypeByDatasource(DataSource dataSource) {
      String dbType = null;
      if(dataSource instanceof DruidDataSource) {
         dbType = ((DruidDataSource)dataSource).getDbType();
      }

      return dbType;
   }

   public SQLManager getSqlManager() {
      return this.sqlManager;
   }

   public String getDbType() {
      return this.dbType;
   }
}
