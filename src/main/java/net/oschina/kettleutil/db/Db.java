package net.oschina.kettleutil.db;

import cn.benma666.myutils.StringUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import net.oschina.kettleutil.common.KuConstInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osjava.sj.loader.SJDataSource;
import org.pentaho.di.core.database.util.DatabaseUtil;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.trans.step.BaseStep;

public class Db extends org.beetl.sql.core.db.Db {
    private static String FIND_GENERAL_CONFIG_SQL = "select expand from metl_unify_dict d where d.ocode=? and d.dict_category=?";
    private static Log log = LogFactory.getLog(Db.class);

    public static Db use(String dbCode) {
        try {
            return new Db(new DatabaseUtil().getNamedDataSource(dbCode), dbCode);
        } catch (KettleException e) {
            log.error("获取数据库失败:" + dbCode, e);
            return null;
        }
    }

    public static Db use(BaseStep ku, String dbCode) {
        try {
            return new Db(new DatabaseUtil().getNamedDataSource(dbCode), dbCode);
        } catch (KettleException e) {
            if (ku != null) {
                ku.logError("获取数据库失败:" + dbCode, e);
            } else {
                log.error("获取数据库失败:" + dbCode, e);
            }
            return null;
        }
    }

    public static Db use(JobEntryBase jee, String dbCode) {
        try {
            return new Db(new DatabaseUtil().getNamedDataSource(dbCode), dbCode);
        } catch (KettleException e) {
            if (jee != null) {
                jee.logError("获取数据库失败:" + dbCode, e);
            } else {
                log.error("获取数据库失败:" + dbCode, e);
            }
            return null;
        }
    }

    public Db(DataSource dataSource, String dbCode) {
        super(dbCode, dataSource, getDbtypeByDatasource(dataSource));
    }

    public static String getDbtypeByDatasource(DataSource dataSource) {
        if (dataSource instanceof DruidDataSource) {
            return ((DruidDataSource) dataSource).getDbType();
        }
        if (dataSource instanceof SJDataSource) {
            return JdbcUtils.getDbType(((SJDataSource) dataSource).toString().split("::::")[1], null);
        }
        return null;
    }

    public JSONObject findGeneralConfig(String configCode) {
        String expand = findFirst(FIND_GENERAL_CONFIG_SQL, new Object[]{configCode, KuConstInterface.DICT_CATEGORY_GENERAL_CONFIG}).getString(KuConstInterface.FIELD_EXPAND);
        if (StringUtil.isNotBlank(expand)) {
            return JSON.parseObject(expand);
        }
        return null;
    }

    public static void closeConn(JobEntryBase jee, Connection conn, PreparedStatement... preps) {
        for (PreparedStatement p : preps) {
            if (p != null) {
                try {
                    p.close();
                } catch (SQLException e) {
                    jee.logError("关闭预处理游标失败", e);
                }
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e2) {
                if (jee != null) {
                    jee.logError("关闭数据库连接失败", e2);
                } else {
                    log.error("关闭数据库连接失败", e2);
                }
            }
        }
    }
}
