package net.oschina.kettleutil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.oschina.mytuils.KettleUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

public abstract class KettleUtilRunBase {
    protected JSONObject configInfo;
    protected KettleUtilData data;
    protected KettleUtil ku;
    protected Log log = LogFactory.getLog(getClass());
    protected KettleUtilMeta meta;

    public abstract void getFields(RowMetaInterface rowMetaInterface, String str, RowMetaInterface[] rowMetaInterfaceArr, StepMeta stepMeta, VariableSpace variableSpace);

    public boolean run() throws Exception {
        Object[] r = this.ku.getRow();
        if (r == null) {
            end();
            this.ku.setOutputDone();
            return false;
        }
        if (this.ku.first) {
            this.data.outputRowMeta = this.ku.getInputRowMeta().clone();
            getFields(this.data.outputRowMeta, this.ku.getStepname(), null, null, this.ku);
            this.ku.first = false;
            init();
        }
        Object[] outputRow = RowDataUtil.createResizedCopy(r, this.data.outputRowMeta.size());
        disposeRow(outputRow);
        this.ku.putRow(this.data.outputRowMeta, outputRow);
        return true;
    }

    protected void disposeRow(Object[] outputRow) throws Exception {
    }

    protected void init() throws Exception {
    }

    protected void end() throws Exception {
    }

    public String getDefaultConfigInfo(TransMeta transMeta, String stepName) throws Exception {
        return "{}";
    }

    protected void addField(RowMetaInterface r, String name, int type, int trimType, String origin, String comments) {
        addField(r, name, type, trimType, origin, comments, 0);
    }

    protected void addField(RowMetaInterface r, String name, int type, int trimType, String origin, String comments, int length) {
        ValueMetaInterface v = new ValueMeta();
        v.setName(name.toUpperCase());
        v.setType(type);
        v.setTrimType(trimType);
        v.setOrigin(origin);
        v.setComments(comments);
        if (length > 0) {
            v.setLength(length);
        }
        r.addValueMeta(v);
    }

    public int getFieldIndex(String field) {
        return this.data.outputRowMeta.indexOfValue(field.toUpperCase());
    }

    public void setVariableRootJob(String variableName, String variableValue) {
        this.ku.setVariable(variableName, variableValue);
        Trans trans = this.ku.getTrans();
        while (trans.getParentTrans() != null) {
            trans.setVariable(variableName, variableValue);
            trans = trans.getParentTrans();
        }
        trans.setVariable(variableName, variableValue);
        for (Job job = trans.getParentJob(); job != null; job = job.getParentJob()) {
            job.setVariable(variableName, variableValue);
        }
    }

    public String getVariavle(String variableName) {
        return KettleUtils.getProp(this.ku, variableName);
    }

    public KettleUtil getKu() {
        return this.ku;
    }

    public void setKu(KettleUtil ku) {
        this.ku = ku;
    }

    public KettleUtilMeta getMeta() {
        return this.meta;
    }

    public void setMeta(KettleUtilMeta meta, VariableSpace space) {
        this.meta = meta;
        try {
            if (StringUtils.isNotBlank(meta.getConfigInfo())) {
                setConfigInfo(JSON.parseObject(space.environmentSubstitute(meta.getConfigInfo())));
            }
        } catch (Exception e) {
            this.log.info("配置信息转换为JSON对象失败：" + JSON.toJSONString(meta), e);
        }
    }

    public KettleUtilData getData() {
        return this.data;
    }

    public void setData(KettleUtilData data) {
        this.data = data;
    }

    public JSONObject getConfigInfo() {
        return this.configInfo;
    }

    public void setConfigInfo(JSONObject configInfo) {
        this.configInfo = configInfo;
    }
}
