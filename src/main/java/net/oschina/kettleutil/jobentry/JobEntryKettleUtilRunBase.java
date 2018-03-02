package net.oschina.kettleutil.jobentry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class JobEntryKettleUtilRunBase {
    protected JSONObject configInfo;
    protected JobEntryKettleUtil jeku;
    protected Log log = LogFactory.getLog(getClass());

    protected abstract boolean run() throws Exception;

    public String getDefaultConfigInfo() throws Exception {
        return "{}";
    }

    public JSONObject getConfigInfo() {
        return this.configInfo;
    }

    public void setConfigInfo(JSONObject configInfo) {
        this.configInfo = configInfo;
    }

    public JobEntryKettleUtil getJeku() {
        return this.jeku;
    }

    public void setJeku(JobEntryKettleUtil jeku) {
        this.jeku = jeku;
        try {
            this.configInfo = JSON.parseObject(jeku.environmentSubstitute(jeku.getConfigInfo()));
        } catch (Exception e) {
            this.log.debug("配置信息不能转换为JSON对象", e);
            this.configInfo = new JSONObject();
        }
    }
}
