package org.onetwo.dbm.dialet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onetwo.common.db.DataBase;
import org.onetwo.common.db.DbmQueryValue;
import org.onetwo.common.db.filequery.SqlFunctionFactory;
import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.dbm.event.internal.DbmBatchInsertEventListener;
import org.onetwo.dbm.event.internal.DbmBatchUpdateEventListener;
import org.onetwo.dbm.event.internal.DbmDeleteEventListener;
import org.onetwo.dbm.event.internal.DbmExtQueryEventListener;
import org.onetwo.dbm.event.internal.DbmFindEventListener;
import org.onetwo.dbm.event.internal.DbmInsertEventListener;
import org.onetwo.dbm.event.internal.DbmInsertOrUpdateListener;
import org.onetwo.dbm.event.internal.DbmLockEventListener;
import org.onetwo.dbm.event.internal.DbmUpdateEventListener;
import org.onetwo.dbm.event.internal.DefaultCoreEventListenerManager;
import org.onetwo.dbm.event.spi.DbmCoreEventListenerManager;
import org.onetwo.dbm.event.spi.DbmEventAction;
import org.onetwo.dbm.id.StrategyType;
import org.onetwo.dbm.mapping.DbmTypeMapping;
import org.onetwo.dbm.mapping.DefaultSQLBuilderFactory;
import org.onetwo.dbm.mapping.SQLBuilderFactory;
import org.onetwo.dbm.utils.DbmLock;

import com.google.common.collect.Sets;


abstract public class AbstractDBDialect implements InnerDBDialet, DBDialect {

	
//	protected MappedEntryManager mappedEntryManager;
	protected DBMeta dbmeta;
//	protected DataBaseConfig dataBaseConfig;
	
	private List<StrategyType> idStrategy = new ArrayList<StrategyType>();
	/***
	 * 是否自动检测设置id策略
	 */
	private boolean autoDetectIdStrategy;

//	protected JFishQueryableEventListener[] queryableEventListeners;
	protected DefaultCoreEventListenerManager dbmEventListenerManager;
	
	private SQLBuilderFactory sqlBuilderFactory;
	
	private DbmTypeMapping typeMapping;
	
	private SqlFunctionDialet sqlFunctionDialet;
	
	/***
	 * 摘取了部分常用的保留字
	 * https://dev.mysql.com/doc/refman/8.0/en/keywords.html
	 */
	private Set<String> keywrods = Sets.newHashSet("key", "keys", "order", "all", "code", "column", "data", "desc", "user", 
													"group", "hour", "month", "options", "plugin", "plugins", "range", "year");
	
	public AbstractDBDialect(DBMeta dbmeta) {
		super();
		this.dbmeta = dbmeta;
		this.typeMapping = new DbmTypeMapping();
//		this.dataBaseConfig = dataBaseConfig;
		this.sqlFunctionDialet = SqlFunctionFactory.getSqlFunctionDialet(dbmeta.getDataBase());
		
//		addKeyword("key");
	}
	
	final protected AbstractDBDialect addKeyword(String kw){
		keywrods.add(kw);
		return this;
	}

	@Override
	public String wrapKeywordColumnName(String columnName){
		if(!keywrods.contains(columnName.toLowerCase())){
			return columnName;
		}
		return "`"+columnName+"`";
	}
	
	@Override
	public SqlFunctionDialet getSqlFunctionDialet() {
		return sqlFunctionDialet;
	}

	public void setSqlFunctionDialet(SqlFunctionDialet sqlFunctionDialet) {
		this.sqlFunctionDialet = sqlFunctionDialet;
	}

	final protected void setSqlTypeMapping(DbmTypeMapping sqlTypeMapping) {
		this.typeMapping = sqlTypeMapping;
	}

	public DbmTypeMapping getTypeMapping() {
		return typeMapping;
	}

	@PostConstruct
	public void initialize() {
		/*if(this.dataBaseConfig==null){
			this.dataBaseConfig = SpringUtils.getHighestOrder(applicationContext, DataBaseConfig.class);
			this.dataBaseConfig = dataBaseConfig!=null?dataBaseConfig:new DefaultDataBaseConfig();
		}*/
//		Assert.notNull(dataBaseConfig, "dataBaseConfig can't be null!");
		
		this.registerIdStrategy();
		//优先使用自定义的 DbEventListenerManager
		if(this.dbmEventListenerManager==null){
//			DbEventListenerManager dbelm = this.findDbEventListenerManagerFromContext(applicationContext);
			DefaultCoreEventListenerManager listMg = new DefaultCoreEventListenerManager();
//			dbelm.registerDefaultEventListeners();
			this.onDefaultDbEventListenerManager(listMg);
			this.dbmEventListenerManager = listMg;
		}
		this.dbmEventListenerManager.freezed();
		
		if(sqlBuilderFactory==null){
			this.sqlBuilderFactory = new DefaultSQLBuilderFactory(this);
		}
		
		this.initOtherComponents();
	}
	
	/****
	 * 注册id策略
	 */
	protected void registerIdStrategy(){
	}
	
	protected void onDefaultDbEventListenerManager(DefaultCoreEventListenerManager listMg){
		listMg.registerListeners(DbmEventAction.insertOrUpdate, new DbmInsertOrUpdateListener())
				.registerListeners(DbmEventAction.insert, new DbmInsertEventListener())
				.registerListeners(DbmEventAction.batchInsert, new DbmBatchInsertEventListener())
				.registerListeners(DbmEventAction.batchUpdate, new DbmBatchUpdateEventListener())
				.registerListeners(DbmEventAction.update, new DbmUpdateEventListener())
				.registerListeners(DbmEventAction.delete, new DbmDeleteEventListener())
				.registerListeners(DbmEventAction.find, new DbmFindEventListener())
				.registerListeners(DbmEventAction.lock, new DbmLockEventListener())
				.registerListeners(DbmEventAction.extQuery, new DbmExtQueryEventListener());
	}
	
	protected void initOtherComponents(){
	}
	
	
	public boolean isSupportedIdStrategy(StrategyType type) {
		return idStrategy.contains(type);
	}
	
	public List<StrategyType> getIdStrategy() {
		return idStrategy;
	}

	/*public MappedEntryManager getMappedEntryManager() {
		return mappedEntryManager;
	}

	public JFishMappedEntry getEntry(Object object){
		return this.mappedEntryManager.getEntry(object);
	}*/

	public int getMaxResults(int first, int size){
		return size;
	}

	public String getLimitString(String sql) {
		return getLimitString(sql, null, null);
	}
	
	public String getLimitStringWithNamed(String sql, String firstName, String maxResultName) {
		return getLimitString(sql, firstName, maxResultName);
	}
	
	abstract public String getLimitString(String sql, String firstName, String maxResultName);
	
	public void addLimitedValue(DbmQueryValue params, String firstName, int firstResult, String maxName, int maxResults){
		params.setValue(firstName, firstResult);
		params.setValue(maxName, getMaxResults(firstResult, maxResults));
	}

	/*public void setMappedEntryManager(MappedEntryManager mappedEntryManager) {
		this.mappedEntryManager = mappedEntryManager;
	}*/

	

	public DBMeta getDbmeta() {
		return dbmeta;
	}

	@Override
	public String getLockSqlString(LockInfo lockInfo) {
		DbmLock lockMode = lockInfo.getLock();
		String sql = "";
		int timeoutInMillis = lockInfo.getTimeoutInMillis();
		if(lockMode==DbmLock.PESSIMISTIC_READ){
			sql = getReadLockString(timeoutInMillis);
		}else if(lockMode==DbmLock.PESSIMISTIC_WRITE){
			sql = getWriteLockString(timeoutInMillis);
		}
		return sql;
	}
	

	protected String getReadLockString(int timeoutInMillis) {
		return "for update";
	}

	protected String getWriteLockString(int timeoutInMillis) {
		return "for update";
	}

	public void setDbmeta(DBMeta dbmeta) {
		this.dbmeta = dbmeta;
	}

	public boolean isAutoDetectIdStrategy() {
		return autoDetectIdStrategy;
	}

	public void setAutoDetectIdStrategy(boolean autoDetectIdStrategy) {
		this.autoDetectIdStrategy = autoDetectIdStrategy;
	}


	public DbmCoreEventListenerManager getDbmEventListenerManager() {
		return dbmEventListenerManager;
	}

	public void setDbmEventListenerManager(
			DefaultCoreEventListenerManager jfishdbEventListenerManager) {
		this.dbmEventListenerManager = jfishdbEventListenerManager;
	}

	public SQLBuilderFactory getSqlBuilderFactory() {
		return sqlBuilderFactory;
	}

	protected void setSqlBuilderFactory(SQLBuilderFactory sqlBuilderFactory) {
		this.sqlBuilderFactory = sqlBuilderFactory;
	}

	/*public DataBaseConfig getDataBaseConfig() {
		return dataBaseConfig;
	}*/

	/*public JFishQueryableEventListener[] getQueryableEventListeners() {
		return queryableEventListeners;
	}

	public void setQueryableEventListeners(JFishQueryableEventListener[] queryableEventListeners) {
		this.queryableEventListeners = queryableEventListeners;
	}*/
	

	public static class DBMeta {
		
		public static DBMeta create(DataBase db){
			return new DBMeta(db);
		}
		
		final private DataBase dataBase;
		private String dbName;
		private String version;
		
		public DBMeta(String dbName) {
			super();
			this.dataBase = DataBase.of(dbName);
			this.dbName = dbName;
		}
		
		public DBMeta(DataBase db) {
			super();
			this.dataBase = db;
			this.dbName = db.getName();
		}
		public boolean isMySQL(){
			return DataBase.MySQL==dataBase;
		}
		public boolean isOracle(){
			return DataBase.Oracle==dataBase;
		}
		public String getDialetName(){
			return dbName + "Dialect";
		}
		
		public String getDbName() {
			return dataBase.getName();
		}

		public String getVersion() {
			return version;
		}

		public void setDbName(String dbName) {
			this.dbName = dbName.toLowerCase();
		}

		public void setVersion(String version) {
			this.version = version;
		}
		
		public DataBase getDataBase() {
			return dataBase;
		}
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("dbmeta [dbName:").append(dbName).append(", version:").append(version).append("]");
			return sb.toString();
		}
	}
}
