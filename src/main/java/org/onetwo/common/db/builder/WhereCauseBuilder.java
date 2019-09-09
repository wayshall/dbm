package org.onetwo.common.db.builder;

import javax.persistence.metamodel.SingularAttribute;


public interface WhereCauseBuilder<E> {
	WhereCauseBuilder<E> debug();

	WhereCauseBuilder<E> or(QueryBuilder<E> subQuery);

	WhereCauseBuilder<E> and(QueryBuilder<E> subQuery);


	WhereCauseBuilder<E> addFields(Object entity);
	
	WhereCauseBuilder<E> operatorFields(String[] operatorFields, Object[] values);
	/***
	 * 
	 * @author weishao zeng
	 * @param entity
	 * @param useLikeIfStringVlue 当属性的值为string类型时，是否使用like查询
	 * @return
	 */
	WhereCauseBuilder<E> addFields(Object entity, boolean useLikeIfStringVlue);
	
	WhereCauseBuilder<E> addField(WhereCauseBuilderField<E> field);

	WhereCauseBuilder<E> ignoreIfNull();
	
	WhereCauseBuilder<E> disabledDataFilter();

	WhereCauseBuilder<E> throwIfNull();

	WhereCauseBuilder<E> calmIfNull();

	DefaultWhereCauseBuilderField<E> field(String... fields);
	DefaultWhereCauseBuilderField<E> field(SingularAttribute<?, ?>... fields);
	
	QueryBuilder<E> end();
	QueryAction<E> toQuery();
}
