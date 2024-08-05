package org.onetwo.dbm.mapping;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.id.IdentifierGenerator;
import org.onetwo.dbm.mapping.SQLBuilderFactory.SqlBuilderType;

public interface DbmMappedEntry extends DbmMappedEntryMeta {
	DBDialect getDbDialect();
	Map<String, IdentifierGenerator<?>> getIdGenerators();
	void addIdGenerator(IdentifierGenerator<?> idGenerator);
	
	
	DbmTypeMapping getSqlTypeMapping();

	void setId(Object entity, Object value);

	/***
	 * 如果是复合主键，则返回复合主键对象
	 * @author weishao zeng
	 * @param entity
	 * @return
	 */
	Serializable getId(Object entity);
	/****
	 * 如果是复合主键，则返回多个值
	 * @author weishao zeng
	 * @param entity
	 * @return
	 */
	Object[] getIds(Object entity);

	void setFieldValue(Object entity, String fieldName, Object value);


	String getColumnName(String field);


	Object getFieldValue(Object entity, String fieldName);

	/*******
	 * 此方法会延迟调用，会设置各种属性和manager的事件回调后，才会调用，
	 * 所以，如果没有实现扫描和构建所有实体，而在运行时才build，就要注意多线程的问题
	 */
	void buildEntry();


	

	<T> T newInstance();

	boolean isDynamic();

	void setDynamic(boolean dynamic);



//	String getStaticInsertSql();

//	String getStaticUpdateSql();

//	String getStaticFetchSql();

	/*String getStaticSeqSql();
	String getStaticCreateSeqSql();*/
	
	JdbcStatementContext<Object[]> makeSelectVersion(Object object);
	
	JdbcStatementContext<Object[]> makeFetchAll();
	
	JdbcStatementContext<Object[]> makeDeleteAll();
	
	/****
	 * 
	 * @author wayshall
	 * @param objects
	 * @param isIdentify 是否根据id查询
	 * @return
	 */
	JdbcStatementContext<List<Object[]>> makeFetch(Object objects, boolean isIdentify);
	
//	JdbcStatementContext<Object[]> makeLockSelect(Object object, LockInfo lock);
	
	JdbcStatementContext<List<Object[]>> makeInsert(Object entity);
	
	JdbcStatementContext<List<Object[]>> makeMysqlInsertOrUpdate(Object entity);
	JdbcStatementContext<List<Object[]>> makeMysqlInsertOrIgnore(Object entity);

	/***
	 * make delete by id
	 * 
	 * @author weishao zeng
	 * @param objects
	 * @return
	 */
	JdbcStatementContext<List<Object[]>> makeDelete(Object objects);

	JdbcStatementContext<List<Object[]>> makeUpdate(Object entity);

	JdbcStatementContext<List<Object[]>> makeDymanicUpdate(Object entity);

	Map<String, DbmMappedField> getMappedFields();
	Map<String, DbmMappedField> getMappedColumns();

	/*boolean isQueryableOnly();

	void setQueryableOnly(boolean queryableOnly);*/
	
	/*void addAnnotations(Annotation...annotations);
	
	boolean hasAnnotation(Class<? extends Annotation> annoClass);
	
	<T extends Annotation> T getAnnotation(Class<T> annoClass);*/
	
	void freezing();
	boolean isFreezing();
	
	boolean hasIdentifyValue(Object entity);
//	Collection<JoinableMappedField> getJoinMappedFields();
	
	SQLBuilderFactory getSqlBuilderFactory();
	
//	DataHolder<String, Object> getDataHolder();
	
	EntrySQLBuilder createSQLBuilder(SqlBuilderType type);
	
//	JdbcStatementContextBuilder createJdbcStatementContextBuilder(SqlBuilderType type);
	
	List<DbmEntityListener> getEntityListeners();
	List<DbmEntityFieldListener> getFieldListeners();
	
	EntrySQLBuilder getStaticFetchSqlBuilder();
	
}