package org.onetwo.common.db.builder;

import java.util.Map;

import javax.persistence.metamodel.SingularAttribute;

import org.onetwo.common.db.builder.QueryBuilderImpl.SubQueryBuilder;
import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.mapping.DbmMappedEntry;

public class DefaultWhereCauseBuilder<E> implements WhereCauseBuilder<E> {
	final protected QueryBuilderImpl<E> queryBuilder;
	final protected Map<Object, Object> params;
	
	public DefaultWhereCauseBuilder(QueryBuilderImpl<E> queryBuilder) {
		super();
		this.queryBuilder = queryBuilder;
		this.params = queryBuilder.getParams();
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
		DbmSessionFactory sf = queryBuilder.getBaseEntityManager().getSessionFactory();
		DbmMappedEntry entry = sf.getMappedEntryManager().getEntry(entity);
		Map<String, Object> fieldMap = ReflectUtils.toMap(entity, (p, v)->{
			return v!=null && entry.contains(p.getName());
		});
		fieldMap.entrySet().forEach(e->{
			if(useLikeIfStringVlue && String.class.isInstance(e.getValue())){
				field(e.getKey()).like(e.getValue().toString());
			}else{
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
	public DefaultWhereCauseBuilderField<E> field(SingularAttribute<?, ?>... fields) {
		return new DefaultWhereCauseBuilderField<>(this, fields);
	}

	@Override
	public QueryBuilder<E> end(){
		return queryBuilder;
	}

	@Override
	public QueryAction<E> toQuery(){
		return queryBuilder.toQuery();
	}
}
