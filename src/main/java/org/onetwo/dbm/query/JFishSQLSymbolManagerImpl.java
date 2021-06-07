package org.onetwo.dbm.query;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.onetwo.common.db.sqlext.DefaultExtQueryDialetImpl;
import org.onetwo.common.db.sqlext.DefaultSQLSymbolManagerImpl;
import org.onetwo.common.db.sqlext.DeleteExtQuery;
import org.onetwo.common.db.sqlext.DeleteExtQueryImpl;
import org.onetwo.common.db.sqlext.ExtQueryDialet;
import org.onetwo.common.db.sqlext.ExtQueryListener;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.db.sqlext.SelectExtQuery;
import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.MappedEntryManager;

public class JFishSQLSymbolManagerImpl extends DefaultSQLSymbolManagerImpl {
	
//	public static final SQLSymbolManager SQL_SYMBOL_MANAGER = create();
	
	public static JFishSQLSymbolManagerImpl create(DBDialect dbDialect){
		ExtQueryDialet sqlDialet = new DbmExtQueryDialetImpl(dbDialect);
		JFishSQLSymbolManagerImpl newSqlSymbolManager = new JFishSQLSymbolManagerImpl(sqlDialet);
		return newSqlSymbolManager;
	}
	
	public static class DbmExtQueryDialetImpl extends DefaultExtQueryDialetImpl {
//		private final DBDialect dbDialect;

		public DbmExtQueryDialetImpl(DBDialect dbDialect) {
			super();
//			this.dbDialect = dbDialect;
		}

		/*@Override
		public String getLockSqlString(LockInfo lock) {
			return dbDialect.getLockSqlString(lock);
		}*/
	}

//	private DBDialect dialect;
	private MappedEntryManager mappedEntryManager;

	public JFishSQLSymbolManagerImpl(ExtQueryDialet sqlDialet) {
		super(sqlDialet);
	}

	@Override
	public DeleteExtQuery createDeleteQuery(Class<?> entityClass, Map<Object, Object> properties) {
		DbmMappedEntry entry = getDbmMappedEntry(entityClass);
		DeleteExtQuery q = new DbmDeleteExtQueryImpl(entry, entityClass, null, properties, this, this.getListeners());
		q.initQuery();
		return q;
	}

	@Override
	public SelectExtQuery createSelectQuery(Class<?> entityClass, String alias, Map<Object, Object> properties) {
		DbmMappedEntry entry = getDbmMappedEntry(entityClass);
		SelectExtQuery q = new DbmExtQueryImpl(entry, entityClass, alias, properties, this, this.getListeners());
		q.initQuery();
		return q;
	}
	
	protected DbmMappedEntry getDbmMappedEntry(Class<?> entityClass){
		DbmMappedEntry entry = null;
		if(mappedEntryManager!=null){
			entry = this.mappedEntryManager.getEntry(entityClass);
		}
		return entry;
	}

	/*public DBDialect getDialect() {
		return dialect;
	}

	public void setDialect(DBDialect dialect) {
		this.dialect = dialect;
	}*/

	public MappedEntryManager getMappedEntryManager() {
		return mappedEntryManager;
	}

	public void setMappedEntryManager(MappedEntryManager mappedEntryManager) {
		this.mappedEntryManager = mappedEntryManager;
	}
	
	public static class DbmDeleteExtQueryImpl extends DeleteExtQueryImpl {
		public DbmDeleteExtQueryImpl(DbmMappedEntry entry, Class<?> entityClass, String alias, Map<Object, Object> params, 
				SQLSymbolManager symbolManager, List<ExtQueryListener> listeners) {
			super(entityClass, alias, params, symbolManager, listeners);
			this.setQueryNameStrategy(new DbmQueryNameStrategy(entry, alias, Collections.emptyMap(), true));
		}
	}

}
