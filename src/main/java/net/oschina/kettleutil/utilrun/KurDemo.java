package net.oschina.kettleutil.utilrun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.oschina.kettleutil.KettleUtilRunBase;
import net.oschina.mytuils.KettleUtils;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

public class KurDemo extends KettleUtilRunBase {
    protected void disposeRow(Object[] outputRow) {
        outputRow[getFieldIndex("JOB_NAME")] = KettleUtils.getRootJobName(this.ku);
    }

    protected void init() {
        this.ku.logBasic("初始化插件");
    }

    protected void end() {
        this.ku.logBasic("数据处理结束");
    }

    public String getDefaultConfigInfo(TransMeta transMeta, String stepName) throws Exception {
        JSONObject params = new JSONObject();
        params.put("key1", "");
        RowMetaInterface fields = transMeta.getPrevStepFields(stepName);
        if (fields.size() == 0) {
            throw new RuntimeException("没有获取到上一步骤的字段，请确认连接好上一步骤");
        }
        params.put("PrevInfoFields", fields.toString());
        JSONArray arr = new JSONArray();
        arr.add("arr1");
        arr.add("arr2");
        params.put("array", arr);
        return JSON.toJSONString(params, true);
    }

    public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {
        addField(r, "JOB_NAME", 2, 3, origin, "JOB名称");
    }
}
