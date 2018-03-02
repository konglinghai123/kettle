package net.oschina.kettleutil.jobentry;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

public class JobEntryKettleUtil extends JobEntryBase implements Cloneable, JobEntryInterface {
    private static Class<?> PKG = JobEntryKettleUtil.class;
    private String className = "net.oschina.kettleutil.utilrun.JeurDemo";
    private String configInfo = "{}";

    public JobEntryKettleUtil() {
        super("", "");
    }

    public Object clone() {
        return (JobEntryKettleUtil) super.clone();
    }

    public String getXML() {
        StringBuffer retval = new StringBuffer();
        retval.append(super.getXML());
        retval.append("      ").append(XMLHandler.addTagValue("configInfo", this.configInfo));
        retval.append("      ").append(XMLHandler.addTagValue("className", this.className));
        return retval.toString();
    }

    public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep, IMetaStore metaStore) throws KettleXMLException {
        try {
            super.loadXML(entrynode, databases, slaveServers);
            this.configInfo = XMLHandler.getTagValue(entrynode, "configInfo");
            this.className = XMLHandler.getTagValue(entrynode, "className");
        } catch (Exception e) {
            throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryKettleUtil.UnableToLoadFromXml", new String[0]), e);
        }
    }

    public void loadRep(Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> list, List<SlaveServer> list2) throws KettleException {
        try {
            this.configInfo = rep.getJobEntryAttributeString(id_jobentry, "configInfo");
            this.className = rep.getJobEntryAttributeString(id_jobentry, "className");
        } catch (KettleDatabaseException dbe) {
            throw new KettleException(BaseMessages.getString(PKG, "JobEntryKettleUtil.UnableToLoadFromRepo", new String[]{String.valueOf(id_jobentry)}), dbe);
        }
    }

    public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_job) throws KettleException {
        try {
            rep.saveJobEntryAttribute(id_job, getObjectId(), "configInfo", this.configInfo);
            rep.saveJobEntryAttribute(id_job, getObjectId(), "className", this.className);
        } catch (KettleDatabaseException dbe) {
            throw new KettleException(BaseMessages.getString(PKG, "JobEntryKettleUtil.UnableToSaveToRepo", new String[]{String.valueOf(id_job)}), dbe);
        }
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getConfigInfo() {
        return this.configInfo;
    }

    public void setConfigInfo(String configInfo) {
        this.configInfo = configInfo;
    }

    public Result execute(Result prev_result, int nr) {
        boolean result = true;
        if (StringUtils.isNotBlank(this.className)) {
            try {
                JobEntryKettleUtilRunBase kui = (JobEntryKettleUtilRunBase) Class.forName(environmentSubstitute(this.className)).newInstance();
                kui.setJeku(this);
                result = kui.run();
            } catch (Exception e) {
                prev_result.setNrErrors(1);
                result = false;
                logError("运行失败," + this.className + "," + environmentSubstitute(this.configInfo), e);
            }
        }
        prev_result.setResult(result);
        return prev_result;
    }

    public String getDefaultConfigInfo() throws Exception {
        if (!StringUtils.isNotBlank(getClassName())) {
            return null;
        }
        JobEntryKettleUtilRunBase kui = (JobEntryKettleUtilRunBase) Class.forName(environmentSubstitute(getClassName())).newInstance();
        kui.setJeku(this);
        return kui.getDefaultConfigInfo();
    }

    public boolean evaluates() {
        return true;
    }

    public boolean isUnconditional() {
        return false;
    }

    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space, Repository repository, IMetaStore metaStore) {
        JobEntryValidatorUtils.andValidator().validate(this, "KettleUtil", remarks, AndValidator.putValidators(new JobEntryValidator[]{JobEntryValidatorUtils.notBlankValidator()}));
    }
}
