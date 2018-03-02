package net.oschina.kettleutil.common;

import net.oschina.kettleutil.db.Db;
import net.oschina.mytuils.KettleUtils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;

public class CommonUtil {
    public static DatabaseMeta getOrCreateDB(String dbCode) throws KettleException {
        Repository repository = KettleUtils.getInstanceRep();
        ObjectId dbId = repository.getDatabaseID(dbCode);
        if (dbId == null) {
            KettleUtils.saveRepositoryElement(new DatabaseMeta(dbCode, KettleUtils.dbTypeToKettle(Db.use("metl").findFirst("select * from metl_database db where db.ocode=?", new Object[]{dbCode}).getString("type")), DatabaseMeta.dbAccessTypeCode[4], null, dbCode, null, null, null));
            dbId = repository.getDatabaseID(dbCode);
        }
        return repository.loadDatabaseMeta(dbId, null);
    }
}
