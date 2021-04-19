package org.onetwo.common.db.builder;

import java.util.Map;

import org.onetwo.dbm.dialet.DBDialect.LockInfo;

/***
 * @author way
 *
 */
public interface QueryBuilder<E> {

	public String getAlias();
	
	public <T> T as(Class<T> queryBuilderClass);

	public Class<?> getEntityClass();
	
	public WhereCauseBuilder<E> where();

	/*public QueryBuilder debug();

	public QueryBuilder or(QueryBuilder subQuery);

	public QueryBuilder and(QueryBuilder subQuery);

	public QueryBuilder addField(QueryBuilderField field);

	public QueryBuilder ignoreIfNull();

	public QueryBuilder throwIfNull();

	public QueryBuilder calmIfNull();

	public DefaultQueryBuilderField field(String... fields);*/

	public QueryBuilder<E> select(String... fields);
	public QueryBuilder<E> unselect(String...fields);

	/***
	 * 
	 * @author weishao zeng
	 * @param first  from 0
	 * @param size
	 * @return
	 */
	public QueryBuilder<E> limit(int first, int size);

	public QueryBuilder<E> asc(String... fields);

	public QueryBuilder<E> desc(String... fields);
	
	QueryBuilderImpl<E> ascRand(Object seed);
	QueryBuilderImpl<E> descRand(Object seed);

	public QueryBuilder<E> distinct(String... fields);
	public QueryBuilder<E> lock(LockInfo lock);
	
	public QueryBuilderJoin leftJoin(String table, String alias);

//	public QueryBuilder build();
	/***
	 * alias toSelect
	 * @return
	 */
	public QueryAction<E> toQuery();
	public QueryAction<E> toSelect();
	
	public int delete();
	
	public Map<Object, Object> getParams();
	
//	public ParamValues getParamValues();
//	public String getSql();
	
	
//	public int execute();
}