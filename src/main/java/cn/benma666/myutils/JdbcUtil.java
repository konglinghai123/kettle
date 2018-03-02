package cn.benma666.myutils;

import cn.benma666.myutils.StringUtil;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.fastjson.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdbcUtil {

   public static final String PARAM_OBJ = "paramObj";
   public static final String PARAMS = "params";
   public static final String DATABASE_NAME = "databaseName";
   public static final String PORT = "port";
   public static final String HOSTNAME = "hostname";
   public static final String DRIVER_CLASS_NAME = "driverClassName";
   public static final String DB_TYPE = "dbType";


   public static JSONObject parseJdbcUrl(String url) throws Exception {
      JSONObject result = new JSONObject();
      if(StringUtil.isBlank(url)) {
         return result;
      } else {
         String dbType = JdbcUtils.getDbType(url, (String)null);
         if(StringUtil.isBlank(dbType)) {
            return result;
         } else {
            result.put("dbType", dbType);
            result.put("driverClassName", JdbcUtils.getDriverClassName(url));
            Pattern pat;
            Matcher m;
            if("mysql".equals(dbType)) {
               pat = Pattern.compile("jdbc:mysql://(.*):(\\d*)/([^?]+)\\??((([^=]+)=([^&]*)&?)*)");
               m = pat.matcher(url);
               if(m.find() && m.groupCount() >= 3) {
                  result.put("hostname", m.group(1));
                  result.put("port", m.group(2));
                  result.put("databaseName", m.group(3));
                  result.put("params", m.group(4));
                  JSONObject paramObj = new JSONObject();
                  if(StringUtil.isNotBlank(m.group(4))) {
                     String[] ps = m.group(4).split("&");
                     String[] arr$ = ps;
                     int len$ = ps.length;

                     for(int i$ = 0; i$ < len$; ++i$) {
                        String p = arr$[i$];
                        String[] pv = p.split("=");
                        paramObj.put(pv[0], pv[1]);
                     }
                  }

                  result.put("paramObj", paramObj);
               }
            } else if("oracle".equals(dbType)) {
               pat = Pattern.compile("jdbc:oracle:thin:@(.*):(\\d*):([^?]+)");
               m = pat.matcher(url);
               if(m.find() && m.groupCount() == 3) {
                  result.put("hostname", m.group(1));
                  result.put("port", m.group(2));
                  result.put("databaseName", m.group(3));
               }
            }

            return result;
         }
      }
   }
}
