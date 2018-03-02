package net.oschina.kettleutil;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class KettleUtil extends BaseStep implements StepInterface {
    private KettleUtilData data;
    private KettleUtilRunBase kui;
    private KettleUtilMeta meta;

    public KettleUtil(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
        super(s, stepDataInterface, c, t, dis);
    }

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        this.meta = (KettleUtilMeta) smi;
        this.data = (KettleUtilData) sdi;
        if (!StringUtils.isNotBlank(this.meta.getClassName())) {
            return defaultRun();
        }
        try {
            if (this.first) {
                this.kui = (KettleUtilRunBase) Class.forName(environmentSubstitute(this.meta.getClassName())).newInstance();
                this.kui.setKu(this);
                this.kui.setMeta(this.meta, this);
            }
            this.kui.setData(this.data);
            return this.kui.run();
        } catch (Exception e) {
            setErrors(getErrors() + 1);
            logError("运行失败," + this.meta.getClassName() + "," + environmentSubstitute(this.meta.getConfigInfo()), e);
            return defaultRun();
        }
    }

    public boolean defaultRun() throws KettleException, KettleStepException {
        Object[] r = getRow();
        if (r == null) {
            setOutputDone();
            return false;
        }
        if (this.first) {
            this.first = false;
            this.data.outputRowMeta = getInputRowMeta().clone();
            this.meta.getFields(this.data.outputRowMeta, getStepname(), null, null, this);
            logBasic("template step initialized successfully");
        }
        putRow(this.data.outputRowMeta, RowDataUtil.createResizedCopy(r, this.data.outputRowMeta.size()));
        if (checkFeedback(getLinesRead())) {
            logBasic("Linenr " + getLinesRead());
        }
        return true;
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        this.meta = (KettleUtilMeta) smi;
        this.data = (KettleUtilData) sdi;
        return super.init(smi, sdi);
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        this.meta = (KettleUtilMeta) smi;
        this.data = (KettleUtilData) sdi;
        super.dispose(smi, sdi);
    }
}
