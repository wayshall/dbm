package org.onetwo.common.db.builder;

import java.util.List;
import java.util.Map;

import org.onetwo.common.db.DbmQueryValue;
import org.onetwo.common.db.InnerBaseEntityManager;
import org.onetwo.common.db.sqlext.ExtQueryInner;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.db.sqlext.SelectExtQuery;
import org.onetwo.common.spring.copier.CopyUtils;
import org.onetwo.common.utils.Page;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.onetwo.dbm.exception.DbmException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("unchecked")
public class QueryActionImpl implements QueryAction {

	protected InnerBaseEntityManager baseEntityManager;
	private SelectExtQuery extQuery;
	final private QueryBuilder queryBuilder;

	public QueryActionImpl(QueryBuilderImpl queryBuilder, Class<?> entityClass, String alias, Map<Object, Object> properties){
		if(queryBuilder.getBaseEntityManager()==null){
			throw new DbmException("to create QueryAction, the baseEntityManager can not be null!");
		}
		this.queryBuilder = queryBuilder;
		this.baseEntityManager = queryBuilder.getBaseEntityManager();
		extQuery = getSQLSymbolManager().createSelectQuery(entityClass, alias, properties);
//		extQuery.build();
	}
	
	public SelectExtQuery getExtQuery() {
//		throwIfHasNotBuild();
		return extQuery;
	}

	protected SQLSymbolManager getSQLSymbolManager(){
//		SQLSymbolManager symbolManager = SQLSymbolManagerFactory.getInstance().getJdbc();
		return this.baseEntityManager.getSQLSymbolManager();
	}

	protected void checkOperation(){
		if(this.baseEntityManager==null)
			throw new UnsupportedOperationException("no entityManager");
//		this.build();
	}
	
	@Override
	public <T> T one(){
		checkOperation();
//		this.getQueryBuilder().limit(0, 1);
		return (T)baseEntityManager.selectOne(getExtQuery());
	}
	
	/***
	 * 查找唯一结果，如果找不到则返回null，找到多个则抛异常 IncorrectResultSizeDataAccessException，详见：DataAccessUtils.requiredSingleResult
	 */
	@Override
	public <T> T unique(){
		checkOperation();
		return (T)baseEntityManager.selectUnique(getExtQuery());
	}

	@Override
	public <T> List<T> list(){
		checkOperation();
		return baseEntityManager.select(getExtQuery());
	}

	@Override
	public <T> List<T> listAs(Class<T> toClass){
		checkOperation();
		List<?> datas = baseEntityManager.select(getExtQuery());
		return CopyUtils.copy(toClass, datas);
	}

	@Override
	public <T> Page<T> page(Page<T> page){
		checkOperation();
		baseEntityManager.selectPage(page, getExtQuery());
		return page;
	}

	protected DbmQueryValue convertAsDbmQueryValue(ExtQueryInner extQuery){
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
	public <T> T extractAs(ResultSetExtractor<T> rse){
		T res = this.getDbmEntityManager().getCurrentSession().find(convertAsDbmQueryValue(getExtQuery()), rse);
		return res;
	}
	
	public <T> List<T> listWith(RowMapper<T> rowMapper){
		List<T> res = this.getDbmEntityManager().getCurrentSession().findList(convertAsDbmQueryValue(getExtQuery()), rowMapper);
		return res;
	}
	
	@Override
	public Number count() {
		checkOperation();
		return baseEntityManager.count(getExtQuery());
	}

	protected DbmEntityManager getDbmEntityManager(){
		return (DbmEntityManager) getBaseEntityManager();
	}

	public InnerBaseEntityManager getBaseEntityManager() {
		return baseEntityManager;
	}

	protected QueryBuilder getQueryBuilder() {
		return queryBuilder;
	}
	
}
