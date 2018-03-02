package net.oschina.kettleutil;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class KettleUtilData extends BaseStepData implements StepDataInterface {
    public RowMetaInterface outputRowMeta;
}
