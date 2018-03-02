package org.beetl.sql.core.db;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.beetl.sql.core.NameConversion;
import org.beetl.sql.core.SQLSource;
import org.beetl.sql.core.db.KeyWordHandler;
import org.beetl.sql.core.db.MetadataManager;
import org.beetl.sql.core.engine.Beetl;

public interface DBStyle {

   int ID_ASSIGN = 1;
   int ID_AUTO = 2;
   int ID_SEQ = 3;
   int ID_UUID = 4;
   String OFFSET = "_pageOffset";
   String PAGE_SIZE = "_pageSize";
   String PAGE_END = "_pageEnd";
   String ORDER_BY = "_orderBy";
   int DB_MYSQL = 1;
   int DB_ORACLE = 2;
   int DB_POSTGRES = 3;
   int DB_SQLSERVER = 4;
   int DB_SQLLITE = 5;
   int DB_DB2 = 6;
   int DB_H2 = 7;


   void init(Beetl var1);

   SQLSource genSelectById(Class var1);

   SQLSource genSelectByTemplate(Class var1);

   SQLSource genSelectCountByTemplate(Class var1);

   SQLSource genDeleteById(Class var1);

   SQLSource genSelectAll(Class var1);

   SQLSource genUpdateAll(Class var1);

   SQLSource genUpdateById(Class var1);

   SQLSource genUpdateTemplate(Class var1);

   SQLSource genInsert(Class var1);

   SQLSource genInsertTemplate(Class var1);

   String genColumnList(String var1);

   String genCondition(String var1);

   String genColAssignProperty(String var1);

   String genColAssignPropertyAbsolute(String var1);

   Set getCols(String var1);

   String getName();

   int getDBType();

   String getPageSQL(String var1);

   void initPagePara(Map var1, long var2, long var4);

   int getIdType(Method var1);

   KeyWordHandler getKeyWordHandler();

   void setKeyWordHandler(KeyWordHandler var1);

   NameConversion getNameConversion();

   MetadataManager getMetadataManager();

   void setNameConversion(NameConversion var1);

   void setMetadataManager(MetadataManager var1);

   SQLSource genSelectVal(String var1);

   String Date14Exp();
}
