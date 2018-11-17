package org.onetwo.common.db.builder;

import javax.persistence.metamodel.SingularAttribute;


public interface WhereCauseBuilder {
	WhereCauseBuilder debug();

	WhereCauseBuilder or(QueryBuilder subQuery);

	WhereCauseBuilder and(QueryBuilder subQuery);


	WhereCauseBuilder addFields(Object entity);
	/***
	 * 
	 * @author weishao zeng
	 * @param entity
	 * @param useLikeIfStringVlue 当属性的值为string类型时，是否使用like查询
	 * @return
	 */
	WhereCauseBuilder addFields(Object entity, boolean useLikeIfStringVlue);
	
	WhereCauseBuilder addField(WhereCauseBuilderField field);

	WhereCauseBuilder ignoreIfNull();
	
	WhereCauseBuilder disabledDataFilter();

	WhereCauseBuilder throwIfNull();

	WhereCauseBuilder calmIfNull();

	DefaultWhereCauseBuilderField field(String... fields);
	DefaultWhereCauseBuilderField field(SingularAttribute<?, ?>... fields);
	
	QueryBuilder end();
	QueryAction toQuery();
}
