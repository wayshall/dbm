package org.onetwo.common.db.builder;

import java.util.List;
import java.util.Map;

import org.onetwo.common.db.DbmQueryValue;
import org.onetwo.common.db.InnerBaseEntityManager;
import org.onetwo.common.db.sqlext.ExtQueryInner;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.db.sqlext.SelectExtQuery;
import org.onetwo.common.spring.copier.CopyUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.Page;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.DbmMapRowMapperResultSetExtractor;
import org.onetwo.dbm.jdbc.SimpleMapRowMapperResultSetExtractor;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("unchecked")
public class QueryActionImpl<E> implements QueryAction<E> {

	protected InnerBaseEntityManager baseEntityManager;
	private ExtQueryInner extQuery;
	final private QueryBuilder<E> queryBuilder;
	
//	private final Class<?> entityClass;
//	private final String alias;
//	private final Map<Object, Object> properties;

	public QueryActionImpl(QueryBuilderImpl<E> queryBuilder){
		if(queryBuilder.getBaseEntityManager()==null){
			throw new DbmException("to create QueryAction, the baseEntityManager can not be null!");
		}
		this.queryBuilder = queryBuilder;
		this.baseEntityManager = queryBuilder.getBaseEntityManager();
		
//		this.entityClass = entityClass;
//		this.alias = alias;
//		this.properties = properties;
//		extQuery = getSQLSymbolManager().createSelectQuery(entityClass, alias, properties);
//		extQuery.build();
	}
	
	final protected SelectExtQuery createSelectQuery() {
		SelectExtQuery extQuery = getSQLSymbolManager().createSelectQuery(this.queryBuilder.getEntityClass(), this.queryBuilder.getAlias(), this.queryBuilder.getParams());
		return extQuery;
	}
	
	public SelectExtQuery getSelectQuery() {
//		throwIfHasNotBuild();
		if (extQuery==null) {
			this.extQuery = this.createSelectQuery();
		}
		if (!SelectExtQuery.class.isInstance(extQuery)) {
			throw new DbmException("It's not a SelectExtQuery: " + extQuery.getClass().getSimpleName());
		}
		return (SelectExtQuery) extQuery;
	}

	protected SQLSymbolManager getSQLSymbolManager() {
//		SQLSymbolManager symbolManager = SQLSymbolManagerFactory.getInstance().getJdbc();
		return this.baseEntityManager.getSQLSymbolManager();
	}

	protected void checkOperation() {
		if(this.baseEntityManager==null)
			throw new UnsupportedOperationException("no entityManager");
//		this.build();
	}
	
	@Override
	public E one(){
		checkOperation();
//		this.getQueryBuilder().limit(0, 1);
		// 这句必须在创建extQuery前调用
		this.queryBuilder.limit(0, 1);
		
		List<E> list = baseEntityManager.select(getSelectQuery());
		if (LangUtils.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}
	

	@Override
	public boolean exist() {
		// 这句必须在创建extQuery前调用
		this.queryBuilder.limit(0, 1);
		// 创建后，设置选择id
		this.getSelectQuery().selectId();
		E one = one();
		return one!=null;
	}
	
	/***
	 * 查找唯一结果，如果找不到则返回null，找到多个则抛异常 IncorrectResultSizeDataAccessException，详见：DataAccessUtils.requiredSingleResult
	 */
	@Override
	public E unique() {
		checkOperation();
		return (E)baseEntityManager.selectUnique(getSelectQuery());
	}

	@Override
	public List<E> list() {
		checkOperation();
		return baseEntityManager.select(getSelectQuery());
	}

	@Override
	public <T> List<T> listAs(Class<T> toClass){
		checkOperation();
		if (LangUtils.isSimpleType(toClass)) {
			throw new DbmException("target class can not be a simple type: " + toClass);
		}
		List<?> datas = baseEntityManager.select(getSelectQuery());
		return CopyUtils.copy(toClass, datas);
	}

	@Override
	public Page<E> page(Page<E> page) {
		checkOperation();
		baseEntityManager.selectPage(page, getSelectQuery());
		return page;
	}

	protected DbmQueryValue convertAsDbmQueryValue(ExtQueryInner extQuery) {
		extQuery.build();
		DbmQueryValue qv = DbmQueryValue.create(extQuery.getSql());
		qv.setResultClass(extQuery.getEntityClass());
		/*if(extQuery.getParamsValue().isList()){
			qv.setValue(extQuery.getParamsValue().asList());
		}else{
			qv.setValue(extQuery.getParamsValue().asMap());
		}*/
		qv.setValue(extQuery.getParamsValue().asMap());
		
		return qv;
	}
	
	public <T> T extractAs(ResultSetExtractor<T> rse) {
		T res = this.getDbmEntityManager().getCurrentSession().find(convertAsDbmQueryValue(getSelectQuery()), rse);
		return res;
	}
	
	public <K, V> Map<K, V> asMap(DbmMapRowMapperResultSetExtractor<K, V> rse) {
		Map<K, V> res = this.getDbmEntityManager().getCurrentSession().find(convertAsDbmQueryValue(getSelectQuery()), rse);
		return res;
	}
	
	@Override
	public <K, V> Map<K, V> asMap(SimpleMapRowMapperResultSetExtractor<K, V> rse) {
		Map<K, V> res = this.getDbmEntityManager().getCurrentSession().find(convertAsDbmQueryValue(getSelectQuery()), rse);
		return res;
	}

	public <T> List<T> listWith(RowMapper<T> rowMapper) {
		SelectExtQuery query = getSelectQuery();
		DbmQueryValue dqv = convertAsDbmQueryValue(query);
		List<T> res = this.getDbmEntityManager().getCurrentSession().findListWihtLimit(dqv, rowMapper, query.getFirstResult(), query.getMaxResults());
		return res;
	}
	
	@Override
	public Number count() {
		checkOperation();
		return baseEntityManager.count(getSelectQuery());
	}

	protected DbmEntityManager getDbmEntityManager(){
		return (DbmEntityManager) getBaseEntityManager();
	}

	public InnerBaseEntityManager getBaseEntityManager() {
		return baseEntityManager;
	}

	protected QueryBuilder<E> getQueryBuilder() {
		return queryBuilder;
	}
	
}
