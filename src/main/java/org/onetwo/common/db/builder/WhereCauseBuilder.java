package org.onetwo.common.db.builder;

import javax.persistence.metamodel.SingularAttribute;


public interface WhereCauseBuilder {
	WhereCauseBuilder debug();

	WhereCauseBuilder or(QueryBuilder subQuery);

	WhereCauseBuilder and(QueryBuilder subQuery);


	WhereCauseBuilder addFields(Object entity);
	
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
