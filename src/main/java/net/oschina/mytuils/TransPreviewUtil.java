package net.oschina.mytuils;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.debug.BreakPointListener;
import org.pentaho.di.trans.debug.StepDebugMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.pentaho.di.trans.step.StepMeta;

public class TransPreviewUtil {
    public static final int MAX_BINARY_STRING_PREVIEW_SIZE = 1000000;
    private static Log log = LogFactory.getLog(TransPreviewUtil.class);
    private boolean cancelled = false;
    private String loggingText;
    private int[] previewSize;
    private String[] previewStepNames;
    private Trans trans;
    private TransDebugMeta transDebugMeta;
    private TransMeta transMeta;

    public TransPreviewUtil(TransMeta transMeta, String[] previewStepNames, int[] previewSize) {
        this.transMeta = transMeta;
        this.previewStepNames = previewStepNames;
        this.previewSize = previewSize;
    }

    public void doPreview() {
        this.trans = new Trans(this.transMeta);
        try {
            this.trans.prepareExecution(null);
            this.transDebugMeta = new TransDebugMeta(this.transMeta);
            for (int i = 0; i < this.previewStepNames.length; i++) {
                StepMeta stepMeta = this.transMeta.findStep(this.previewStepNames[i]);
                StepDebugMeta stepDebugMeta = new StepDebugMeta(stepMeta);
                stepDebugMeta.setReadingFirstRows(true);
                stepDebugMeta.setRowCount(this.previewSize[i]);
                this.transDebugMeta.getStepDebugMetaMap().put(stepMeta, stepDebugMeta);
            }
            this.transDebugMeta.addRowListenersToTransformation(this.trans);
            try {
                this.trans.startThreads();
                final List<String> previewComplete = new ArrayList();
                while (previewComplete.size() < this.previewStepNames.length && !this.trans.isFinished()) {
                    this.transDebugMeta.addBreakPointListers(new BreakPointListener() {
                        public void breakPointHit(TransDebugMeta transDebugMeta, StepDebugMeta stepDebugMeta, RowMetaInterface rowBufferMeta, List<Object[]> list) {
                            previewComplete.add(stepDebugMeta.getStepMeta().getName());
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        log.error("", e);
                    }
                }
                this.trans.stopAll();
                this.loggingText = KettleLogStore.getAppender().getBuffer(this.trans.getLogChannel().getLogChannelId(), true).toString();
            } catch (KettleException e2) {
                log.error("", e2);
            }
        } catch (KettleException e22) {
            log.error("", e22);
        }
    }

    public List<Object[]> getPreviewRows(String stepname) {
        if (this.transDebugMeta == null) {
            return null;
        }
        for (StepMeta stepMeta : this.transDebugMeta.getStepDebugMetaMap().keySet()) {
            if (stepMeta.getName().equals(stepname)) {
                return ((StepDebugMeta) this.transDebugMeta.getStepDebugMetaMap().get(stepMeta)).getRowBuffer();
            }
        }
        return null;
    }

    public RowMetaInterface getPreviewRowsMeta(String stepname) {
        if (this.transDebugMeta == null) {
            return null;
        }
        for (StepMeta stepMeta : this.transDebugMeta.getStepDebugMetaMap().keySet()) {
            if (stepMeta.getName().equals(stepname)) {
                return ((StepDebugMeta) this.transDebugMeta.getStepDebugMetaMap().get(stepMeta)).getRowBufferMeta();
            }
        }
        return null;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public String getLoggingText() {
        return this.loggingText;
    }

    public Trans getTrans() {
        return this.trans;
    }

    public TransDebugMeta getTransDebugMeta() {
        return this.transDebugMeta;
    }

    public static List<List<Object>> getData(RowMetaInterface rowMeta, List<Object[]> buffer) {
        List<List<Object>> result = new ArrayList();
        List<Object> row1 = new ArrayList();
        for (int i = 0; i < buffer.size(); i++) {
            row1 = new ArrayList();
            getDataForRow(rowMeta, row1, (Object[]) buffer.get(i));
            result.add(row1);
        }
        return result;
    }

    public static int getDataForRow(RowMetaInterface rowMeta, List<Object> row1, Object[] row) {
        int nrErrors = 0;
        for (int c = 0; c < rowMeta.size(); c++) {
            String show;
            ValueMetaInterface v = rowMeta.getValueMeta(c);
            try {
                show = v.getString(row[c]);
                if (v.isBinary() && show != null && show.length() > MAX_BINARY_STRING_PREVIEW_SIZE) {
                    show = show.substring(0, MAX_BINARY_STRING_PREVIEW_SIZE);
                }
            } catch (KettleValueException e) {
                nrErrors++;
                if (nrErrors < 25) {
                    log.error(Const.getStackTracker(e));
                }
                show = null;
            } catch (ArrayIndexOutOfBoundsException e2) {
                nrErrors++;
                if (nrErrors < 25) {
                    log.error(Const.getStackTracker(e2));
                }
                show = null;
            }
            if (show != null) {
                row1.add(show);
            } else {
                row1.add("<null>");
            }
        }
        return nrErrors;
    }
}
