package org.onetwo.common.db.builder;

import java.util.Map;
import java.util.function.Supplier;

import javax.persistence.metamodel.SingularAttribute;

import org.onetwo.common.db.builder.QueryBuilderImpl.SubQueryBuilder;
import org.onetwo.common.db.filter.DataQueryParamaterEnhancer;
import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.db.sqlext.ExtQuery.KeyObject;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.mapping.DbmMappedEntry;

import com.google.common.collect.Maps;

public class DefaultWhereCauseBuilder<E> implements WhereCauseBuilder<E> {
	public static final String[] EXCLUDE_PROPERTIES = new String[] { "page", "pageNo", "pageSize", "pagination", "autoCount" };
	
	final protected QueryBuilderImpl<E> queryBuilder;
	final protected Map<Object, Object> params;
	private DefaultWhereCauseBuilder<E> parent;
	private KeyObject keyObject;
	
	public DefaultWhereCauseBuilder(QueryBuilderImpl<E> queryBuilder) {
		super();
		this.queryBuilder = queryBuilder;
		this.params = queryBuilder.getParams();
	}
	
	private DefaultWhereCauseBuilder(DefaultWhereCauseBuilder<E> parent, KeyObject keyObject) {
		super();
		this.parent = parent;
		this.queryBuilder = parent.queryBuilder;
		this.params = Maps.newLinkedHashMap();
		this.keyObject = keyObject;
	}
	
	Map<Object, Object> getParams() {
		return params;
	}

	@Override
	public DefaultWhereCauseBuilder<E> addField(WhereCauseBuilderField<E> field){
		this.params.put(field.getOPFields(), field.getValues());
		return self();
	}

	@Override
	public DefaultWhereCauseBuilder<E> addFields(Object entity){
		return addFields(entity, true);
	}

	@Override
	public DefaultWhereCauseBuilder<E> operatorFields(String[] operatorFields, Object[] values) {
		this.params.put(operatorFields, values);
		return this;
	}
	
	public DefaultWhereCauseBuilder<E> addFields(Object entity, boolean useLikeIfStringVlue){
		if (entity==null) {
			return self();
		}
		
		DbmSessionFactory sf = queryBuilder.getBaseEntityManager().getSessionFactory();
		DbmMappedEntry entry = sf.getMappedEntryManager().findEntry(entity);
		
		// 排除分页参数
		Map<String, Object> fieldMap = null;
		if (entry!=null) {
			fieldMap = ReflectUtils.toMap(entity, (p, v)->{
				return v!=null && entry.contains(p.getName());
			}, EXCLUDE_PROPERTIES);
		} else {
			fieldMap = ReflectUtils.toMap(entity, (p, v)->{
				return v!=null;
			}, EXCLUDE_PROPERTIES);
		}
		
		fieldMap.entrySet().forEach(e->{
			if (String.class.isInstance(e.getValue())) {
				if(useLikeIfStringVlue){
					field(e.getKey()).like(e.getValue().toString());
				}else{
					field(e.getKey()).equalTo(e.getValue());
				}
			} else {
				field(e.getKey()).equalTo(e.getValue());
			}
		});
		return self();
	}

	protected DefaultWhereCauseBuilder<E> self(){
		return (DefaultWhereCauseBuilder<E>)this;
	}
	
	@Override
	public DefaultWhereCauseBuilder<E> debug(){
		this.params.put(K.DEBUG, true);
		return self();
	}
	
	@Override
	public DefaultWhereCauseBuilder<E> or(QueryBuilder<E> subQuery){
		this.checkSubQuery(subQuery);
		this.params.put(K.OR, subQuery.getParams());
		return self();
	}
	
	protected void checkSubQuery(QueryBuilder<E> subQuery){
		if(!(subQuery instanceof SubQueryBuilder)){
			LangUtils.throwBaseException("please use "+SubQueryBuilder.class.getSimpleName()+".sub() method to create sub query .");
		}
	}
	
	@Override
	public DefaultWhereCauseBuilder<E> and(QueryBuilder<E> subQuery){
		this.checkSubQuery(subQuery);
		this.params.put(K.AND, subQuery.getParams());
		return self();
	}
	
	@Override
	public DefaultWhereCauseBuilder<E> ignoreIfNull(){
		this.params.put(K.IF_NULL, K.IfNull.Ignore);
		return self();
	}
	
	@Override
	public DefaultWhereCauseBuilder<E> disabledDataFilter() {
		this.params.put(K.DATA_FILTER, false);
		return self();
	}
	
	/***
	 * 
	 * @author weishao zeng
	 * @param predicate 返回true时，禁止使用DataQueryParamaterEnhancer
	 * @return
	 */
	@Override
	public DefaultWhereCauseBuilder<E> disabledDataQueryParamaterEnhancer(Supplier<Boolean> predicate) {
		if (predicate!=null && predicate.get()) {
			disabledDataQueryParamaterEnhancer();
		}
		return self();
	}
	
	@Override
	public DefaultWhereCauseBuilder<E> disabledDataQueryParamaterEnhancer() {
		this.params.put(DataQueryParamaterEnhancer.class, false);
		return self();
	}
	
	@Override
	public DefaultWhereCauseBuilder<E> throwIfNull(){
		this.params.put(K.IF_NULL, K.IfNull.Throw);
		return self();
	}
	
	@Override
	public DefaultWhereCauseBuilder<E> calmIfNull(){
		this.params.put(K.IF_NULL, K.IfNull.Calm);
		return self();
	}
	
	@Override
	public DefaultWhereCauseBuilderField<E> field(String...fields){
		return new DefaultWhereCauseBuilderField<>(this, fields);
	}
	
	@Override
	public WhereCauseBuilder<E> or() {
		return new DefaultWhereCauseBuilder<>(this, (KeyObject)K.OR);
	}
	
	@Override
	public WhereCauseBuilder<E> and() {
		return new DefaultWhereCauseBuilder<>(this, (KeyObject)K.AND);
	}

	@Override
	public DefaultWhereCauseBuilderField<E> field(SingularAttribute<?, ?>... fields) {
		return new DefaultWhereCauseBuilderField<>(this, fields);
	}

	@Override
	public QueryBuilder<E> end(){
		if (parent!=null && !params.isEmpty()) {
			parent.getParams().put(keyObject, params);
			return parent.end();
		}
		return queryBuilder;
	}

	@Override
	public QueryAction<E> toQuery(){
		end();
		return queryBuilder.toQuery();
	}

	@Override
	public ExecuteAction toExecute() {
		return queryBuilder.toExecute();
	}
}
