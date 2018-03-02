package net.oschina.kettleutil.common;

import cn.benma666.myutils.StringUtil;
import com.alibaba.fastjson.JSONObject;
import java.util.List;
import net.oschina.kettleutil.db.Db;

public class Dict {
    public static String dictCategoryToSql(String dictCategory) {
        String defaultVal = "select ocode ID,oname CN from metl_unify_dict where dict_category='" + dictCategory + "' and is_disable='" + KuConstInterface.SCHEDULER_TYPE_NOT_TIMING + "' order by oorder asc;ds=metl";
        String result = dictCategory;
        if (!StringUtil.isNotBlank(dictCategory) || dictCategory.toLowerCase().startsWith("select")) {
            return result;
        }
        try {
            result = JSONObject.parseObject(Db.use("metl").queryStr("select expand from metl_unify_dict t where t.dict_category='DICT_CATEGORY' and is_disable=? and t.ocode=?", new Object[]{KuConstInterface.SCHEDULER_TYPE_NOT_TIMING, dictCategory})).getString("sql");
            if (StringUtil.isBlank(result)) {
                return defaultVal;
            }
            return result;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static List<JSONObject> dictList(String dictCategory) {
        String[] dict = parseDictExp(dictCategoryToSql(dictCategory));
        return Db.use(dict[1]).find(dict[0], new Object[0]);
    }

    public static String dictValue(String dictCategory, String key) {
        String[] dict = parseDictExp(dictCategoryToSql(dictCategory));
        String sql = "select cn from (" + dict[0] + ") t where t.id=?";
        String result = Db.use(dict[1]).queryStr(sql, new Object[]{key});
        if (StringUtil.isBlank(result)) {
            return key;
        }
        return result;
    }

    public static List<JSONObject> dictObjList(String dictCategory) {
        return Db.use("metl").find("select * from metl_unify_dict where dict_category=? and is_disable=? order by oorder asc", new Object[]{dictCategory, KuConstInterface.SCHEDULER_TYPE_NOT_TIMING});
    }

    public static JSONObject dictObj(String dictCategory, String key) {
        return Db.use("metl").findFirst("select * from metl_unify_dict where dict_category=? and is_disable=? and ocode=?", new Object[]{dictCategory, KuConstInterface.SCHEDULER_TYPE_NOT_TIMING, key});
    }

    public static String[] parseDictExp(String exp) {
        if (StringUtil.isBlank(exp)) {
            return null;
        }
        String [] result = new String[2];
        String[] strs = exp.split(";");
        result[0] = strs[0];
        if (strs.length > 1) {
            result[1] = strs[1].substring(3);
            return result;
        }
        result[1] = "metl";
        return result;
    }
}
