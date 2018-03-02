package org.beetl.sql.core.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.beetl.core.Configuration;
import org.beetl.sql.core.BeetlSQLException;
import org.beetl.sql.core.NameConversion;
import org.beetl.sql.core.SQLSource;
import org.beetl.sql.core.annotatoin.AssignID;
import org.beetl.sql.core.annotatoin.AutoID;
import org.beetl.sql.core.annotatoin.DateTemplate;
import org.beetl.sql.core.annotatoin.SeqID;
import org.beetl.sql.core.annotatoin.TableTemplate;
import org.beetl.sql.core.db.ClassDesc;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.core.db.DefaultKeyWordHandler;
import org.beetl.sql.core.db.KeyWordHandler;
import org.beetl.sql.core.db.MetadataManager;
import org.beetl.sql.core.db.TableDesc;
import org.beetl.sql.core.engine.Beetl;
import org.beetl.sql.core.kit.BeanKit;

public abstract class AbstractDBStyle implements DBStyle {

   protected static AbstractDBStyle adbs;
   protected NameConversion nameConversion;
   protected MetadataManager metadataManager;
   public String STATEMENT_START;
   public String STATEMENT_END;
   public String HOLDER_START;
   public String HOLDER_END;
   protected String lineSeparator = System.getProperty("line.separator", "\n");
   protected KeyWordHandler keyWordHandler = new DefaultKeyWordHandler();
   protected boolean offsetStartZero = false;


   public void init(Beetl beetl) {
      Configuration cf = beetl.getGroupTemplate().getConf();
      this.STATEMENT_START = cf.getStatementStart();
      this.STATEMENT_END = cf.getStatementEnd();
      if(this.STATEMENT_END == null || this.STATEMENT_END.length() == 0) {
         this.STATEMENT_END = this.lineSeparator;
      }

      this.HOLDER_START = cf.getPlaceholderStart();
      this.HOLDER_END = cf.getPlaceholderEnd();
      this.offsetStartZero = Boolean.parseBoolean(beetl.getPs().getProperty("OFFSET_START_ZERO").trim());
   }

   public String getSTATEMENTSTART() {
      return this.STATEMENT_START;
   }

   public String getSTATEMENTEND() {
      return this.STATEMENT_END;
   }

   public NameConversion getNameConversion() {
      return this.nameConversion;
   }

   public void setNameConversion(NameConversion nameConversion) {
      this.nameConversion = nameConversion;
   }

   public MetadataManager getMetadataManager() {
      return this.metadataManager;
   }

   public void setMetadataManager(MetadataManager metadataManager) {
      this.metadataManager = metadataManager;
   }

   public SQLSource genSelectById(Class cls) {
      String tableName = this.nameConversion.getTableName(cls);
      TableDesc table = this.metadataManager.getTable(tableName);
      String condition = this.appendIdCondition(cls);
      return new SQLSource("select * from " + this.getTableName(table) + condition);
   }

   public SQLSource genSelectByTemplate(Class cls) {
      String tableName = this.nameConversion.getTableName(cls);
      TableDesc table = this.metadataManager.getTable(tableName);
      String condition = this.getSelectTemplate(cls);
      String appendSql = "";
      TableTemplate t = (TableTemplate)BeanKit.getAnnotation(cls, TableTemplate.class);
      if(t != null) {
         appendSql = t.value();
         if((appendSql == null || appendSql.length() == 0) && table.getIdNames().size() != 0) {
            appendSql = " order by ";
            Set sql = table.getIdNames();
            byte i = 0;

            for(Iterator i$ = sql.iterator(); i$.hasNext(); appendSql = appendSql + " , ") {
               String id = (String)i$.next();
               appendSql = appendSql + id + " desc";
               if(i == sql.size() - 1) {
                  break;
               }
            }
         }
      }

      String sql1 = "select * from " + this.getTableName(table) + condition + appendSql;
      return new SQLSource(sql1);
   }

   public SQLSource genSelectCountByTemplate(Class cls) {
      String tableName = this.nameConversion.getTableName(cls);
      TableDesc table = this.metadataManager.getTable(tableName);
      String condition = this.getSelectTemplate(cls);
      return new SQLSource("select count(1) from " + this.getTableName(table) + condition);
   }

   protected String getSelectTemplate(Class cls) {
      String condition = " where 1=1 " + this.lineSeparator;
      String tableName = this.nameConversion.getTableName(cls);
      TableDesc table = this.metadataManager.getTable(tableName);
      ClassDesc classDesc = table.getClassDesc(cls, this.nameConversion);
      Iterator cols = classDesc.getInCols().iterator();
      Iterator attrs = classDesc.getAttrs().iterator();

      while(cols.hasNext() && attrs.hasNext()) {
         String col = (String)cols.next();
         String attr = (String)attrs.next();
         if(classDesc.isDateType(col)) {
            String getter = "get" + col.substring(0, 1).toUpperCase() + col.substring(1);

            try {
               Method e = cls.getMethod(getter, new Class[0]);
               DateTemplate dateTemplate = (DateTemplate)e.getAnnotation(DateTemplate.class);
               if(dateTemplate != null) {
                  String sql = this.genDateAnnotatonSql(dateTemplate, cls, col);
                  condition = condition + sql;
               }
            } catch (Exception var14) {
               throw new RuntimeException("获取metod出错" + var14.getMessage());
            }
         } else {
            condition = condition + this.appendWhere(cls, table, col, attr);
         }
      }

      return condition;
   }

   public SQLSource genDeleteById(Class cls) {
      String tableName = this.nameConversion.getTableName(cls);
      TableDesc table = this.metadataManager.getTable(tableName);
      String condition = this.appendIdCondition(cls);
      return new SQLSource("delete from " + this.getTableName(table) + condition);
   }

   public SQLSource genSelectAll(Class cls) {
      String tableName = this.nameConversion.getTableName(cls);
      TableDesc table = this.metadataManager.getTable(tableName);
      tableName = table.getName();
      return new SQLSource("select * from " + this.getTableName(table));
   }

   public SQLSource genUpdateById(Class cls) {
      String tableName = this.nameConversion.getTableName(cls);
      TableDesc table = this.metadataManager.getTable(tableName);
      ClassDesc classDesc = table.getClassDesc(cls, this.nameConversion);
      StringBuilder sql = (new StringBuilder("update ")).append(this.getTableName(table)).append(" set ").append(this.lineSeparator);
      Iterator cols = classDesc.getInCols().iterator();
      Iterator properties = classDesc.getAttrs().iterator();
      List idCols = classDesc.getIdCols();

      String condition;
      while(cols.hasNext() && properties.hasNext()) {
         condition = (String)cols.next();
         String prop = (String)properties.next();
         if(!classDesc.isUpdateIgnore(prop) && !idCols.contains(condition)) {
            sql.append(this.appendSetColumnAbsolute(cls, table, condition, prop));
         }
      }

      condition = this.appendIdCondition(cls);
      sql = this.removeComma(sql, condition);
      return new SQLSource(sql.toString());
   }

   public SQLSource genUpdateTemplate(Class cls) {
      String tableName = this.nameConversion.getTableName(cls);
      TableDesc table = this.metadataManager.getTable(tableName);
      ClassDesc classDesc = table.getClassDesc(cls, this.nameConversion);
      StringBuilder sql = (new StringBuilder("update ")).append(this.getTableName(table)).append(" set ").append(this.lineSeparator);
      String condition = this.appendIdCondition(cls);
      Iterator cols = classDesc.getInCols().iterator();
      Iterator properties = classDesc.getAttrs().iterator();
      List idCols = classDesc.getIdCols();

      while(cols.hasNext() && properties.hasNext()) {
         String trimSql = (String)cols.next();
         String prop = (String)properties.next();
         if(!classDesc.isUpdateIgnore(prop) && !idCols.contains(trimSql)) {
            sql.append(this.appendSetColumn(cls, table, trimSql, prop));
         }
      }

      StringBuilder trimSql1 = new StringBuilder();
      trimSql1.append(this.getSTATEMENTSTART()).append("trim(){\n").append(this.getSTATEMENTEND()).append("\n").append(sql);
      trimSql1.append(this.getSTATEMENTSTART()).append("}\n").append(this.getSTATEMENTEND());
      sql = this.removeComma(trimSql1, condition);
      if(condition == null) {
         throw new BeetlSQLException(4, "无法生成sql语句，缺少主键");
      } else {
         return new SQLSource(sql.toString());
      }
   }

   public SQLSource genUpdateAll(Class cls) {
      String tableName = this.nameConversion.getTableName(cls);
      TableDesc table = this.metadataManager.getTable(tableName);
      ClassDesc classDesc = table.getClassDesc(cls, this.nameConversion);
      StringBuilder sql = (new StringBuilder("update ")).append(this.getTableName(table)).append(" set ").append(this.lineSeparator);
      Iterator cols = classDesc.getInCols().iterator();
      Iterator properties = classDesc.getAttrs().iterator();
      List idCols = classDesc.getIdCols();

      while(cols.hasNext() && properties.hasNext()) {
         String col = (String)cols.next();
         String prop = (String)properties.next();
         if(!classDesc.isUpdateIgnore(prop) && !idCols.contains(col)) {
            sql.append(this.appendSetColumn(cls, table, col, prop));
         }
      }

      sql = this.removeComma(sql, (String)null);
      return new SQLSource(sql.toString());
   }

   public SQLSource genInsert(Class cls) {
      return this.generalInsert(cls, false);
   }

   public SQLSource genInsertTemplate(Class cls) {
      return this.generalInsert(cls, true);
   }

   protected SQLSource generalInsert(Class cls, boolean template) {
      String tableName = this.nameConversion.getTableName(cls);
      TableDesc table = this.metadataManager.getTable(tableName);
      ClassDesc classDesc = table.getClassDesc(cls, this.nameConversion);
      StringBuilder sql = new StringBuilder("insert into " + this.getTableName(table) + this.lineSeparator);
      StringBuilder colSql = new StringBuilder("(");
      StringBuilder valSql = new StringBuilder(" VALUES (");
      if(template) {
         colSql.append(this.lineSeparator).append(this.STATEMENT_START);
         colSql.append("trim({suffixOverrides:\',\'}){").append(this.lineSeparator);
         valSql.append(this.lineSeparator).append(this.STATEMENT_START);
         valSql.append("trim({suffixOverrides:\',\'}){").append(this.lineSeparator);
      }

      int idType = 1;
      SQLSource source = new SQLSource();
      Iterator cols = classDesc.getInCols().iterator();
      Iterator attrs = classDesc.getAttrs().iterator();
      List idCols = classDesc.getIdCols();

      while(cols.hasNext() && attrs.hasNext()) {
         String map = (String)cols.next();
         String i$ = (String)attrs.next();
         if(!classDesc.isInsertIgnore(i$)) {
            if(idCols.size() == 1 && idCols.contains(map)) {
               idType = this.getIdType((Method)classDesc.getIdMethods().get(i$));
               if(idType == 2) {
                  continue;
               }

               if(idType == 3) {
                  colSql.append(this.appendInsertColumn(cls, table, map));
                  SeqID idAttr = (SeqID)((Method)classDesc.getIdMethods().get(i$)).getAnnotation(SeqID.class);
                  valSql.append(idAttr.name() + ".nextval,");
                  continue;
               }

               if(idType == 1) {
                  ;
               }
            }

            if(template) {
               colSql.append(this.appendInsertTemplateColumn(cls, table, i$, map));
               valSql.append(this.appendInsertTemplateValue(cls, table, i$));
            } else {
               colSql.append(this.appendInsertColumn(cls, table, map));
               valSql.append(this.appendInsertValue(cls, table, i$));
            }
         }
      }

      if(template) {
         colSql.append(this.lineSeparator).append(this.STATEMENT_START);
         colSql.append("}").append(this.lineSeparator).append(this.STATEMENT_END);
         colSql.append(")");
         valSql.append(this.lineSeparator).append(this.STATEMENT_START);
         valSql.append("}").append(this.lineSeparator).append(this.STATEMENT_END);
         valSql.append(")");
         sql.append(colSql).append(valSql);
      } else {
         sql.append(this.removeComma(colSql, (String)null).append(")").append(this.removeComma(valSql, (String)null)).append(")").toString());
      }

      source.setTemplate(sql.toString());
      source.setIdType(idType);
      source.setTableDesc(table);
      if(idType == 1) {
         HashMap map1 = new HashMap();
         Iterator i$1 = classDesc.getIdAttrs().iterator();

         while(i$1.hasNext()) {
            String idAttr1 = (String)i$1.next();
            AssignID assignId = (AssignID)((Method)classDesc.getIdMethods().get(idAttr1)).getAnnotation(AssignID.class);
            if(assignId != null && assignId.value().length() != 0) {
               map1.put(idAttr1, assignId);
            }
         }

         if(map1.size() != 0) {
            source.setAssignIds(map1);
         }
      }

      return source;
   }

   public String genColumnList(String table) {
      Set colSet = this.getCols(table);
      if(null != colSet && !colSet.isEmpty()) {
         StringBuilder cols = new StringBuilder();
         Iterator i$ = colSet.iterator();

         while(i$.hasNext()) {
            String col = (String)i$.next();
            cols.append(col).append(",");
         }

         return cols.deleteCharAt(cols.length() - 1).toString();
      } else {
         return "";
      }
   }

   public Set getCols(String tableName) {
      TableDesc table = this.metadataManager.getTable(tableName);
      return table.getCols();
   }

   public String genCondition(String tableName) {
      TableDesc table = this.metadataManager.getTable(tableName);
      ClassDesc classDesc = table.getClassDesc(this.nameConversion);
      Set attrSet = classDesc.getAttrs();
      if(null != attrSet && !attrSet.isEmpty()) {
         Iterator attrIt = attrSet.iterator();
         Iterator colIt = table.getCols().iterator();
         StringBuilder condition = new StringBuilder();
         Set colsIds = table.getIdNames();

         while(colIt.hasNext() && attrIt.hasNext()) {
            String col = (String)colIt.next();
            String attr = (String)attrIt.next();
            if(!colsIds.contains(col)) {
               condition.append(this.appendWhere((Class)null, table, col, attr));
            }
         }

         return "1 = 1  \n" + condition.toString();
      } else {
         return "";
      }
   }

   public String genColAssignProperty(String tableName) {
      TableDesc table = this.metadataManager.getTable(tableName);
      ClassDesc classDesc = table.getClassDesc(this.nameConversion);
      Iterator cols = classDesc.getInCols().iterator();
      Iterator properties = classDesc.getAttrs().iterator();
      StringBuilder sql = new StringBuilder();

      while(cols.hasNext() && properties.hasNext()) {
         String col = (String)cols.next();
         String prop = (String)properties.next();
         sql.append(this.appendSetColumn((Class)null, table, col, prop));
      }

      return sql.deleteCharAt(sql.length() - 1).toString();
   }

   public String genColAssignPropertyAbsolute(String tableName) {
      TableDesc table = this.metadataManager.getTable(tableName);
      ClassDesc classDesc = table.getClassDesc(this.nameConversion);
      Set colSet = classDesc.getInCols();
      Set properties = classDesc.getAttrs();
      if(null != colSet && !colSet.isEmpty()) {
         StringBuilder sql = new StringBuilder();
         Iterator colIt = colSet.iterator();
         Iterator propertiesIt = properties.iterator();

         while(colIt.hasNext() && propertiesIt.hasNext()) {
            String col = (String)colIt.next();
            String prop = (String)propertiesIt.next();
            sql.append(this.appendSetColumnAbsolute((Class)null, table, col, prop));
         }

         return sql.deleteCharAt(sql.length() - 1).toString();
      } else {
         return "";
      }
   }

   protected StringBuilder removeComma(StringBuilder sql, String condition) {
      int index = sql.lastIndexOf(",");
      return index == -1?sql:sql.deleteCharAt(index).append(condition == null?"":condition);
   }

   protected String appendSetColumnAbsolute(Class c, TableDesc table, String colName, String fieldName) {
      return this.getKeyWordHandler().getCol(colName) + "=" + this.HOLDER_START + fieldName + this.HOLDER_END + ",";
   }

   protected String appendSetColumn(Class c, TableDesc table, String colName, String fieldName) {
      String prefix = "";
      return this.STATEMENT_START + "if(!isEmpty(" + prefix + fieldName + ")){" + this.STATEMENT_END + "\t" + this.getKeyWordHandler().getCol(colName) + "=" + this.HOLDER_START + prefix + fieldName + this.HOLDER_END + "," + this.lineSeparator + this.STATEMENT_START + "}" + this.STATEMENT_END;
   }

   protected String appendWhere(Class c, TableDesc table, String colName, String fieldName) {
      String prefix = "";
      String connector = " and ";
      return this.STATEMENT_START + "if(!isEmpty(" + prefix + fieldName + ")){" + this.STATEMENT_END + connector + this.getKeyWordHandler().getCol(colName) + "=" + this.HOLDER_START + prefix + fieldName + this.HOLDER_END + this.lineSeparator + this.STATEMENT_START + "}" + this.STATEMENT_END;
   }

   protected String appendInsertColumn(Class c, TableDesc table, String colName) {
      return this.getKeyWordHandler().getCol(colName) + ",";
   }

   protected String appendInsertValue(Class c, TableDesc table, String fieldName) {
      return this.HOLDER_START + fieldName + this.HOLDER_END + ",";
   }

   protected String appendInsertTemplateColumn(Class c, TableDesc table, String fieldName, String colName) {
      String col = this.getKeyWordHandler().getCol(colName);
      return col.startsWith("\'")?this.HOLDER_START + "db.testColNull(" + fieldName + ",\"" + col + "\")" + this.HOLDER_END:this.HOLDER_START + "db.testColNull(" + fieldName + ",\'" + col + "\')" + this.HOLDER_END;
   }

   protected String appendInsertTemplateValue(Class c, TableDesc table, String fieldName) {
      return this.HOLDER_START + "db.testNull(" + fieldName + "!,\"" + fieldName + "\")" + this.HOLDER_END;
   }

   protected String appendIdCondition(Class cls) {
      String tableName = this.nameConversion.getTableName(cls);
      StringBuilder condition = new StringBuilder(" where ");
      TableDesc table = this.metadataManager.getTable(tableName);
      ClassDesc classDesc = table.getClassDesc(cls, this.nameConversion);
      List colIds = classDesc.getIdCols();
      List propertieIds = classDesc.getIdAttrs();
      this.checkId(colIds, propertieIds, cls.getName());
      Iterator colIt = colIds.iterator();
      Iterator propertieIt = propertieIds.iterator();
      if(colIt.hasNext() && propertieIt.hasNext()) {
         String colId = (String)colIt.next();
         String properId = (String)propertieIt.next();
         condition.append(this.getKeyWordHandler().getCol(colId)).append(" = ").append(this.HOLDER_START).append(properId).append(this.HOLDER_END);

         while(colIt.hasNext() && propertieIt.hasNext()) {
            colId = (String)colIt.next();
            properId = (String)propertieIt.next();
            condition.append(" and ").append(this.getKeyWordHandler().getCol(colId)).append(" = ").append(this.HOLDER_START).append(properId).append(this.HOLDER_END);
         }
      }

      return condition.toString();
   }

   protected boolean isLegalSelectMethod(Method method) {
      return method.getDeclaringClass() != Object.class && (method.getName().startsWith("get") || method.getName().startsWith("is")) && !Date.class.isAssignableFrom(method.getReturnType()) && !Calendar.class.isAssignableFrom(method.getReturnType());
   }

   protected boolean isLegalOtherMethod(Method method) {
      return method.getDeclaringClass() != Object.class && (method.getName().startsWith("get") || method.getName().startsWith("is")) && method.getParameterTypes().length == 0;
   }

   protected String genDateAnnotatonSql(DateTemplate t, Class c, String col) {
      String accept = t.accept();
      String[] vars = null;
      String comp;
      if(accept != null && accept.length() != 0) {
         vars = t.accept().split(",");
      } else {
         comp = col.substring(0, 1).toUpperCase() + col.substring(1);
         vars = new String[]{"min" + comp, "max" + comp};
      }

      comp = null;
      String compare = t.compare();
      String[] comp1;
      if(compare != null && compare.length() != 0) {
         comp1 = t.accept().split(",");
      } else {
         comp1 = new String[]{">=", "<"};
      }

      t.compare().split(",");
      String prefix = "";
      String connector = " and ";
      String sql = this.STATEMENT_START + "if(!isEmpty(" + prefix + vars[0] + ")){" + this.STATEMENT_END + connector + col + comp1[0] + this.HOLDER_START + vars[0] + this.HOLDER_END + this.lineSeparator + this.STATEMENT_START + "}" + this.STATEMENT_END;
      sql = sql + this.STATEMENT_START + "if(!isEmpty(" + prefix + vars[1] + ")){" + this.STATEMENT_END + connector + col + comp1[1] + this.HOLDER_START + vars[1] + this.HOLDER_END + this.lineSeparator + this.STATEMENT_START + "}" + this.STATEMENT_END;
      return sql;
   }

   protected String getTableName(TableDesc desc) {
      return desc.getSchema() != null?this.getKeyWordHandler().getTable(desc.getSchema()) + "." + this.getKeyWordHandler().getTable(desc.getName()):this.getKeyWordHandler().getTable(desc.getName());
   }

   protected void checkId(Collection colsId, Collection attrsId, String clsName) {
      if(colsId.size() == 0 || attrsId.size() == 0) {
         throw new BeetlSQLException(7, "主键未发现," + clsName + ",检查数据库表定义或者NameConversion");
      }
   }

   protected String getOrderBy() {
      return this.lineSeparator + this.HOLDER_START + "text(has(_orderBy)?\' order by \'+_orderBy)" + this.HOLDER_END + " ";
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

   public KeyWordHandler getKeyWordHandler() {
      return this.keyWordHandler;
   }

   public void setKeyWordHandler(KeyWordHandler keyWordHandler) {
      this.keyWordHandler = keyWordHandler;
   }

   public SQLSource genSelectVal(String val) {
      return new SQLSource("select " + val + " as val");
   }

   public String Date14Exp() {
      throw new RuntimeException("未实现");
   }
}
