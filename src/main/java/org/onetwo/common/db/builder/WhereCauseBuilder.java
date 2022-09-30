package org.onetwo.common.db.builder;

import javax.persistence.metamodel.SingularAttribute;


public interface WhereCauseBuilder<E> {
	WhereCauseBuilder<E> debug();

	/****
	 * @deprecated instead of {@link #or()}
	 * 
	 * @author weishao zeng
	 * @param subQuery
	 * @return
	 */
	@Deprecated
	WhereCauseBuilder<E> or(QueryBuilder<E> subQuery);

	/***
	 * @deprecated instead of {@link #and()}
	 * @author weishao zeng
	 * @param subQuery
	 * @return
	 */
	@Deprecated
	WhereCauseBuilder<E> and(QueryBuilder<E> subQuery);


	WhereCauseBuilder<E> addFields(Object entity);
	
	WhereCauseBuilder<E> operatorFields(String[] operatorFields, Object[] values);
	
	WhereCauseBuilder<E> or();
	WhereCauseBuilder<E> and();
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
