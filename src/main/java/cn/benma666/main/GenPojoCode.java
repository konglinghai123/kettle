package cn.benma666.main;

import cn.benma666.myutils.DateUtil;
import cn.benma666.myutils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.sql.core.db.Db;

public class GenPojoCode {

   public static void main(String[] args) {
      if(args.length != 1) {
         System.out.println("请输入参数文件路径");
      } else {
         try {
            Scanner e = new Scanner(new File(args[0]));
            StringBuffer jsonStr = new StringBuffer();

            while(e.hasNext()) {
               jsonStr.append(e.next());
            }

            e.close();
            JSONObject params = JSONObject.parseObject(jsonStr.toString());
            String url = params.getString("url");
            String user = params.getString("user").toUpperCase();
            String pwd = params.getString("pwd");
            String pkg = params.getString("pkg");
            ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader();
            Configuration cfg = Configuration.defaultConfiguration();
            GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
            Template t = gt.getTemplate("/other/pojo.btl");
            gt.registerFunctionPackage("StringUtil", StringUtil.class);
            gt.registerFunctionPackage("DateUtil", DateUtil.class);
            Db db = new Db("default", url, user, pwd);
            String[] var16;
            int var15 = (var16 = (String[])params.getJSONArray("tableNames").toArray(new String[0])).length;

            for(int var14 = 0; var14 < var15; ++var14) {
               String tableName = var16[var14];
               tableName = tableName.toUpperCase();
               HashMap ps = new HashMap();
               ps.put("owner", user);
               ps.put("tableName", tableName);
               JSONObject r = db.findFirst("pojo.tab_comments", (Map)ps);
               String classComments = r.getString("comments");
               List colList = db.find("pojo.cols_info", (Map)ps);
               t.binding("package", pkg);
               t.binding("tableName", tableName);
               t.binding("classComments", classComments);
               t.binding("colList", colList);
               t.renderTo(new FileOutputStream(StringUtil.upperCaseFast(StringUtil.underlineTohump(tableName)) + ".java"));
            }
         } catch (Exception var21) {
            var21.printStackTrace();
         }

      }
   }
}
