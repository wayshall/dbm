package org.onetwo.dbm.dialet;

import java.util.List;

import org.onetwo.common.db.DbmQueryValue;
import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.dbm.dialet.AbstractDBDialect.DBMeta;
import org.onetwo.dbm.event.spi.DbmCoreEventListenerManager;
import org.onetwo.dbm.id.StrategyType;
import org.onetwo.dbm.mapping.DbmTypeMapping;
import org.onetwo.dbm.mapping.SQLBuilderFactory;
import org.onetwo.dbm.utils.DbmLock;
import org.onetwo.dbm.utils.Initializable;

public interface DBDialect extends Initializable {
	
//	public String BEAN_NAME = "jfishdbDialect";
	
//	public MappedEntryManager getMappedEntryManager();
	
//	public void initialize();
//	public DataBase getDbmeta();
	public DBMeta getDbmeta();
	
	public String wrapKeywordColumnName(String columnName);

	public SQLBuilderFactory getSqlBuilderFactory();
	
	public boolean isSupportedIdStrategy(StrategyType type);
	
	public boolean isAutoDetectIdStrategy();

	public List<StrategyType> getIdStrategy();
	public DbmCoreEventListenerManager getDbmEventListenerManager();
//	public JFishEventListener[] getQueryableEventListeners();
	
//	public int getMaxResults(int first, int size);
	
	public String getLimitString(String sql);
	
	public String getLimitStringWithNamed(String sql, String firstName, String maxResultName);
	
	public void addLimitedValue(DbmQueryValue params, String firstName, int firstResult, String maxName, int maxResults);
//	public boolean isPrintSql();
//	public DataBaseConfig getDataBaseConfig();
	
	public DbmTypeMapping getTypeMapping();
	
	public SqlFunctionDialet getSqlFunctionDialet();
	
	public String getLockSqlString(LockInfo lock);
	
	public static class LockInfo {
		public static LockInfo lock(DbmLock lock, int timeoutInMillis){
			return new LockInfo(lock, timeoutInMillis);
		}
		public static LockInfo write(){
			return new LockInfo(DbmLock.PESSIMISTIC_WRITE, WAIT_FOREVER);
		}
		public static LockInfo read(){
			return new LockInfo(DbmLock.PESSIMISTIC_READ, WAIT_FOREVER);
		}
		
		public static final int NO_WAIT = 0;
		public static final int WAIT_FOREVER = -1;
		
		private final DbmLock lock;
		private final int timeoutInMillis;
		public LockInfo(DbmLock lock, int timeoutInMillis) {
			super();
			this.lock = lock;
			this.timeoutInMillis = timeoutInMillis;
		}
		public DbmLock getLock() {
			return lock;
		}
		public int getTimeoutInMillis() {
			return timeoutInMillis;
		}
	}
}
