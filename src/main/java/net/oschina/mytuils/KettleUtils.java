package net.oschina.mytuils;

import cn.benma666.myutils.JdbcUtil;
import cn.benma666.myutils.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.oschina.kettleutil.common.KuConstInterface;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleClientEnvironment.ClientType;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KettleUtils {
    private static JobMeta jobMetaTemplate;
    public static Logger log = LoggerFactory.getLogger(KettleUtils.class);
    private static Map<String, Repository> repMap = new HashMap();
    private static Repository repository;
    private static TransMeta transMetaTemplate;

    public static Repository getInstanceRep() throws KettleException {
        if (repository != null) {
            return repository;
        }
        throw new KettleException("没有初始化资源库");
    }

    public static Repository use(String repId) {
        return (Repository) repMap.get(repId);
    }

    public static String dbTypeToKettle(String dbType) {
        if (KuConstInterface.DS_TYPE_ORACLE.equals(dbType)) {
            return "Oracle";
        }
        if (KuConstInterface.DS_TYPE_MYSQL.equals(dbType)) {
            return KuConstInterface.DS_TYPE_MYSQL;
        }
        return null;
    }

    public static void connectKettle(String name, String type, String kuser, String kpass) throws Exception {
        destroy();
        createDBRepByJndi(name, dbTypeToKettle(type), name);
        connect(kuser, kpass);
    }

    public static void connectKettle(String name, String url, String user, String pass, String kuser, String kpass) throws Exception {
        destroy();
        createDBRep(name, url, user, pass);
        connect(kuser, kpass);
    }

    public static void connectKettle(String name, String type, String access, String host, String db, String port, String user, String pass, JSONObject params, String kuser, String kpass) throws Exception {
        destroy();
        createDBRep(name, dbTypeToKettle(type), access, host, db, port, user, pass, params);
        connect(kuser, kpass);
    }

    public static Repository createFileRep(String id, String repName, String description, String baseDirectory) throws KettleException {
        initEnv();
        if (!KettleEnvironment.isInitialized()) {
            KettleEnvironment.init();
        }
        return createRep(new KettleFileRepositoryMeta(id, repName, description, baseDirectory), id, repName, description);
    }

    public static Repository createDBRepByJndi(String name, String type, String db) throws KettleException {
        return createDBRep(name, type, DatabaseMeta.dbAccessTypeCode[4], null, db, null, null, null, null);
    }

    private static Repository createDBRep(String name, String url, String user, String pass) throws Exception {
        return createDBRep(name, url, user, pass, name, name, name + "数据库资源库");
    }

    public static Repository createDBRep(String name, String type, String access, String host, String db, String port, String user, String pass, JSONObject params) throws KettleException {
        return createDBRep(name, type, access, host, db, port, user, pass, name, name, name + "数据库资源库", params);
    }

    private static Repository createDBRep(String name, String url, String user, String pass, String id, String repName, String description) throws Exception {
        initEnv();
        return createDBRep(createDatabaseMeta(name, url, user, pass, true, null), id, repName, description);
    }

    public static Repository createDBRep(String name, String type, String access, String host, String db, String port, String user, String pass, String id, String repName, String description, JSONObject params) throws KettleException {
        initEnv();
        return createDBRep(createDatabaseMeta(name, type, access, host, db, port, user, pass, params, true, null), id, repName, description);
    }

    public static Repository createDBRep(DatabaseMeta dataMeta, String id, String repName, String description) throws KettleException {
        return createRep(new KettleDatabaseRepositoryMeta(id, repName, description, dataMeta), id, repName, description);
    }

    public static DatabaseMeta createDatabaseMeta(String name, String url, String user, String pass, boolean replace, Repository repository) throws Exception {
        JSONObject urlObj = JdbcUtil.parseJdbcUrl(url);
        if (KuConstInterface.DS_TYPE_ORACLE.equals(urlObj.getString("dbType"))) {
            return createDatabaseMeta(name, dbTypeToKettle(urlObj.getString("dbType")), DatabaseMeta.dbAccessTypeCode[0], urlObj.getString("hostname"), urlObj.getString("databaseName"), urlObj.getString("port"), user, pass, null, replace, repository);
        } else if (!KuConstInterface.DS_TYPE_MYSQL.equals(urlObj.getString("dbType"))) {
            return null;
        } else {
            return createDatabaseMeta(name, dbTypeToKettle(urlObj.getString("dbType")), DatabaseMeta.dbAccessTypeCode[0], urlObj.getString("hostname"), urlObj.getString("databaseName"), urlObj.getString("port"), user, pass, urlObj.getJSONObject("paramObj"), replace, repository);
        }
    }

    public static DatabaseMeta createDatabaseMeta(String name, String type, String access, String host, String db, String port, String user, String pass, JSONObject params, boolean replace, Repository repository) {
        DatabaseMeta dm = null;
        if (repository != null) {
            try {
                ObjectId dbId = repository.getDatabaseID(name);
                if (!(dbId == null || replace)) {
                    dm = repository.loadDatabaseMeta(dbId, null);
                }
            } catch (KettleException e) {
                log.error("创建数据库元数据失败", e);
            }
        }
        if (dm == null) {
            dm = new DatabaseMeta(name, type, access, host, db, port, user, pass);
            if (params != null) {
                for (Entry<String, Object> ent : params.entrySet()) {
                    dm.addExtraOption(type, (String) ent.getKey(), ent.getValue() + "");
                }
            }
            dm.setForcingIdentifiersToLowerCase(true);
            if (repository != null) {
                try {
                    repository.save(dm, null, null, true);
                } catch (KettleException e2) {
                    log.error("保存数据库元数据失败", e2);
                }
            }
        }
        return dm;
    }

    public static void initEnv() throws KettleException {
        if (System.getenv("KETTLE_HOME") != null) {
            System.setProperty("DI_HOME", System.getenv("KETTLE_HOME"));
            System.setProperty("KETTLE_HOME", System.getenv("KETTLE_HOME"));
            System.setProperty("org.osjava.sj.root", System.getenv("KETTLE_HOME") + "/simple-jndi");
            log.info("KETTLE_HOME配置[能自动加载该目录下plugins中的插件]：" + System.getenv("KETTLE_HOME"));
        }
        if (System.getenv("KETTLE_JNDI_ROOT") != null) {
            System.setProperty("org.osjava.sj.root", System.getenv("KETTLE_JNDI_ROOT"));
            log.info("Simple-jndi配置根路径：" + System.getenv("KETTLE_JNDI_ROOT"));
        }
        if (!KettleEnvironment.isInitialized()) {
            KettleEnvironment.init();
            KettleClientEnvironment.getInstance().setClient(ClientType.SPOON);
        }
    }

    public static Repository createRep(BaseRepositoryMeta baseRepositoryMeta, String id, String repName, String description) throws KettleException {
        if (use(id) != null) {
            if (repository.getName().equals(use(id).getName())) {
                repository = null;
            }
            use(id).disconnect();
        }
        if (baseRepositoryMeta instanceof KettleDatabaseRepositoryMeta) {
            repository = new KettleDatabaseRepository();
            repository.init((KettleDatabaseRepositoryMeta) baseRepositoryMeta);
        } else {
            repository = new KettleFileRepository();
            repository.init((KettleFileRepositoryMeta) baseRepositoryMeta);
        }
        if (repository == null) {
            repository = repository;
        }
        repMap.put(id, repository);
        log.info(repository.getName() + "资源库初始化成功");
        return repository;
    }

    public static Repository connect() throws KettleSecurityException, KettleException {
        return connect(null, null);
    }

    public static Repository connect(String username, String password) throws KettleSecurityException, KettleException {
        repository.connect(username, password);
        log.info(repository.getName() + "资源库连接成功");
        return repository;
    }

    public static void setRepository(Repository repository) {
        repository = repository;
    }

    public static void destroy() {
        if (repository != null) {
            repository.disconnect();
            log.info(repository.getName() + "资源库释放成功");
            repository = null;
        }
    }

    public static JobMeta loadJob(long jobId) throws KettleException {
        return repository.loadJob(new LongObjectId(jobId), null);
    }

    public static JobMeta loadJob(String jobId) throws KettleException {
        return repository.loadJob(new StringObjectId(jobId), null);
    }

    public static JobMeta loadJob(String jobname, String directory) throws KettleException {
        return loadJob(jobname, directory, repository);
    }

    public static JobMeta loadJob(String jobname, String directory, Repository repository) throws KettleException {
        return repository.loadJob(jobname, repository.findDirectory(directory), null, null);
    }

    public static JobMeta loadJob(String jobname, long directory) throws KettleException {
        return loadJob(jobname, directory, repository);
    }

    public static JobMeta loadJob(String jobname, long directory, Repository repository) throws KettleException {
        return repository.loadJob(jobname, repository.findDirectory(new LongObjectId(directory)), null, null);
    }

    public static void delJob(long id_job) throws KettleException {
        delJob(id_job, repository);
    }

    public static void delJob(long id_job, Repository repository) throws KettleException {
        repository.deleteJob(new LongObjectId(id_job));
    }

    public static TransMeta loadTrans(String transname, String directory) throws KettleException {
        return loadTrans(transname, directory, repository);
    }

    public static TransMeta loadTrans(String transname, String directory, Repository repository) throws KettleException {
        return repository.loadTransformation(transname, repository.findDirectory(directory), null, true, null);
    }

    public static TransMeta loadTrans(JobMeta jobMeta, String teansName) throws KettleException {
        JobEntryTrans trans = (JobEntryTrans) jobMeta.findJobEntry(teansName).getEntry();
        return loadTrans(trans.getTransname(), trans.getDirectory());
    }

    public static <T extends JobEntryBase> T loadJobEntry(JobMeta jobMeta, String jobEntryName, T jobEntryMeta) throws KettleException {
        return loadJobEntry(jobMeta.findJobEntry(jobEntryName).getEntry().getObjectId(), jobEntryMeta);
    }

    public static <T extends JobEntryBase> T loadJobEntry(ObjectId entryId, T jobEntryMeta) throws KettleException {
        jobEntryMeta.loadRep(repository, null, entryId, null, null);
        jobEntryMeta.setObjectId(entryId);
        return jobEntryMeta;
    }

    public static JobEntrySpecial findStart(JobMeta jobMeta) {
        for (int i = 0; i < jobMeta.nrJobEntries(); i++) {
            JobEntryInterface je = jobMeta.getJobEntry(i).getEntry();
            if (je.getPluginId().equals("SPECIAL")) {
                return (JobEntrySpecial) je;
            }
        }
        return null;
    }

    public static void saveRepositoryElement(RepositoryElementInterface repositoryElement) throws KettleException {
        saveRepositoryElement(getInstanceRep(), repositoryElement);
    }

    public static void saveRepositoryElement(Repository repository, RepositoryElementInterface repositoryElement) throws KettleException {
        repository.save(repositoryElement, null, null, true);
    }

    public static void saveTrans(TransMeta transMeta) throws KettleException {
        saveRepositoryElement(repository, transMeta);
    }

    public static void saveTrans(Repository repository, TransMeta transMeta) throws KettleException {
        saveRepositoryElement(repository, transMeta);
    }

    public static void saveJob(JobMeta jobMeta) throws KettleException {
        saveRepositoryElement(repository, jobMeta);
    }

    public static void saveJob(Repository repository, JobMeta jobMeta) throws KettleException {
        saveRepositoryElement(repository, jobMeta);
    }

    public static boolean isDirectoryExist(Repository repository, String directoryName) {
        try {
            if (repository.findDirectory(directoryName) == null) {
                return false;
            }
            return true;
        } catch (KettleException e) {
            log.error("判断job目录是否存在失败！", e);
            return false;
        }
    }

    public static RepositoryDirectoryInterface getOrMakeDirectory(String parentDirectory, String directoryName) throws KettleException {
        RepositoryDirectoryInterface dir = repository.findDirectory(parentDirectory + "/" + directoryName);
        if (dir == null) {
            return repository.createRepositoryDirectory(repository.findDirectory(parentDirectory), directoryName);
        }
        return dir;
    }

    public static RepositoryDirectoryInterface makeDirs(String directoryName) throws KettleException {
        if (!StringUtil.isNotBlank(directoryName)) {
            return null;
        }
        String parentDirectory = "/";
        for (String dirStr : directoryName.replace("\\", "/").replace("//", "/").split("/")) {
            parentDirectory = getOrMakeDirectory(parentDirectory, dirStr).getPath();
        }
        return getOrMakeDirectory(parentDirectory, null);
    }

    public static String getDirectory(long dirId) throws KettleException {
        return getDirectory(new LongObjectId(dirId));
    }

    public static String getDirectory(ObjectId dirId) throws KettleException {
        RepositoryDirectoryInterface dir = repository.findDirectory(dirId);
        if (dir == null) {
            return null;
        }
        return dir.getPath();
    }

    public static void setStepToTrans(TransMeta teans, String stepName, StepMetaInterface smi) {
        teans.findStep(stepName).setStepMetaInterface(smi);
    }

    public static void setStepToTransAndSave(TransMeta teans, String stepName, StepMetaInterface smi) throws KettleException {
        setStepToTrans(teans, stepName, smi);
        saveTrans(teans);
    }

    public static List<List<Object>> stepPreview(TransMeta teans, String testStep, StepMetaInterface smi, int previewSize) {
        TransPreviewUtil tpu = new TransPreviewUtil(TransPreviewFactory.generatePreviewTransformation(teans, smi, testStep), new String[]{testStep}, new int[]{previewSize});
        tpu.doPreview();
        return TransPreviewUtil.getData(tpu.getPreviewRowsMeta(testStep), tpu.getPreviewRows(testStep));
    }

    public static void jobCopy(String jobName, String jobPath, Repository fromRepository, Repository toRepository) throws KettleException {
        JobMeta jobMeta = loadJob(jobName, jobPath, fromRepository);
        for (JobEntryCopy jec : jobMeta.getJobCopies()) {
            if (jec.isTransformation()) {
                JobEntryTrans jet = (JobEntryTrans) jec.getEntry();
                transCopy(jet.getObjectName(), jet.getDirectory(), fromRepository, toRepository);
            } else if (jec.isJob()) {
                JobEntryJob jej = (JobEntryJob) jec.getEntry();
                jobCopy(jej.getObjectName(), jej.getDirectory(), fromRepository, toRepository);
            }
        }
        jobMeta.setRepository(toRepository);
        jobMeta.setMetaStore(toRepository.getMetaStore());
        if (!isDirectoryExist(toRepository, jobPath)) {
            toRepository.createRepositoryDirectory(toRepository.findDirectory("/"), jobPath);
        }
        saveJob(toRepository, jobMeta);
    }

    public static void transCopy(String transName, String transPath, Repository fromRepository, Repository toRepository) throws KettleException {
        TransMeta tm = loadTrans(transName, transPath, fromRepository);
        for (StepMeta sm : tm.getSteps()) {
            if (sm.isJobExecutor()) {
                JobExecutorMeta jem = (JobExecutorMeta) sm.getStepMetaInterface();
                jobCopy(jem.getJobName(), jem.getDirectoryPath(), fromRepository, toRepository);
            } else if (sm.getStepMetaInterface() instanceof TransExecutorMeta) {
                TransExecutorMeta te = (TransExecutorMeta) sm.getStepMetaInterface();
                transCopy(te.getTransName(), te.getDirectoryPath(), fromRepository, toRepository);
            }
        }
        if (!isDirectoryExist(toRepository, transPath)) {
            toRepository.createRepositoryDirectory(toRepository.findDirectory("/"), transPath);
        }
        tm.setRepository(toRepository);
        tm.setMetaStore(toRepository.getMetaStore());
        saveTrans(toRepository, tm);
    }

    public static ObjectId getJobId(JobMeta jm) {
        return getJobId(jm.getName(), jm.getRepositoryDirectory());
    }

    public static ObjectId getJobId(String name, RepositoryDirectoryInterface repositoryDirectory) {
        try {
            return repository.getJobId(name, repositoryDirectory);
        } catch (KettleException e) {
            log.debug("获取作业id失败", e);
            return null;
        }
    }

    public static ObjectId getTransformationID(TransMeta tm) {
        return getTransformationID(tm.getName(), tm.getRepositoryDirectory());
    }

    public static ObjectId getTransformationID(String name, RepositoryDirectoryInterface repositoryDirectory) {
        try {
            return repository.getTransformationID(name, repositoryDirectory);
        } catch (KettleException e) {
            log.debug("获取转换id失败", e);
            return null;
        }
    }

    public static void repairTransHop(TransMeta tm) {
        for (int i = 0; i < tm.nrTransHops(); i++) {
            TransHopMeta hop = tm.getTransHop(i);
            hop.setFromStep(tm.findStep(hop.getFromStep().getName()));
            hop.setToStep(tm.findStep(hop.getToStep().getName()));
        }
    }

    public static void setParams(NamedParams target, NamedParams source, Map<String, String> params) {
        target.eraseParameters();
        try {
            for (String key : source.listParameters()) {
                String defaultVal = source.getParameterDefault(key);
                if (params.containsKey(key)) {
                    defaultVal = (String) params.get(key);
                }
                target.addParameterDefinition(key, defaultVal, source.getParameterDescription(key));
            }
        } catch (Exception e) {
            log.error("保存JOB失败", e);
        }
    }

    public static void repairHop(JobMeta jm) {
        for (JobHopMeta hop : jm.getJobhops()) {
            hop.setFromEntry(jm.findJobEntry(hop.getFromEntry().getName()));
            hop.setToEntry(jm.findJobEntry(hop.getToEntry().getName()));
        }
    }

    public static TransMeta getTransMetaTemplate() {
        return transMetaTemplate;
    }

    public static void setTransMetaTemplate(TransMeta transMetaTemplate) {
        transMetaTemplate = transMetaTemplate;
    }

    public static JobMeta getJobMetaTemplate() {
        return jobMetaTemplate;
    }

    public static void setJobMetaTemplate(JobMeta jobMetaTemplate) {
        jobMetaTemplate = jobMetaTemplate;
    }

    public static String getProp(VariableSpace vs, String key) {
        String value = vs.environmentSubstitute("${" + key + "}");
        if (value.startsWith("${")) {
            return "";
        }
        return value;
    }

    public static JSONObject getPropJSONObject(VariableSpace vs, String key) {
        String value = getProp(vs, key);
        if (StringUtil.isNotBlank(value)) {
            return JSON.parseObject(value);
        }
        return null;
    }

    public static Job getRootJob(Job rootjob) {
        while (rootjob != null && rootjob.getParentJob() != null) {
            rootjob = rootjob.getParentJob();
        }
        return rootjob;
    }

    public static Job getRootJob(JobEntryBase jee) {
        return getRootJob(jee.getParentJob());
    }

    public static Job getRootJob(StepInterface si) {
        return getRootJob(si.getTrans().getParentJob());
    }

    public static String getRootJobId(JobEntryBase jee) {
        return getRootJob(jee).getObjectId().getId();
    }

    public static String getRootJobId(StepInterface si) {
        Job rootjob = getRootJob(si);
        if (rootjob != null) {
            return rootjob.getObjectId().getId();
        }
        return null;
    }

    public static String getRootJobName(StepInterface si) {
        Job rootjob = getRootJob(si);
        if (rootjob != null) {
            return rootjob.getObjectName();
        }
        return null;
    }
}
