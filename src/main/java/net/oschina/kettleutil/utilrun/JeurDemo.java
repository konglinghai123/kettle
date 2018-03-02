package net.oschina.kettleutil.utilrun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.oschina.kettleutil.jobentry.JobEntryKettleUtilRunBase;

public class JeurDemo extends JobEntryKettleUtilRunBase {
    protected boolean run() throws Exception {
        this.jeku.logBasic(this.jeku.getConfigInfo());
        this.jeku.logBasic(this.configInfo.toJSONString());
        return true;
    }

    public String getDefaultConfigInfo() throws Exception {
        JSONObject params = new JSONObject();
        params.put("key1", "");
        JSONArray arr = new JSONArray();
        arr.add("arr1");
        arr.add("arr2");
        params.put("array", arr);
        return JSON.toJSONString(params, true);
    }
}
