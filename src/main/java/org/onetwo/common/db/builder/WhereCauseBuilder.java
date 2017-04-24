package org.onetwo.common.db.builder;

import javax.persistence.metamodel.SingularAttribute;


public interface WhereCauseBuilder {
	public WhereCauseBuilder debug();

	public WhereCauseBuilder or(QueryBuilder subQuery);

	public WhereCauseBuilder and(QueryBuilder subQuery);


	public WhereCauseBuilder addFields(Object entity);
	
	public WhereCauseBuilder addField(WhereCauseBuilderField field);

	public WhereCauseBuilder ignoreIfNull();

	public WhereCauseBuilder throwIfNull();

	public WhereCauseBuilder calmIfNull();

	public DefaultWhereCauseBuilderField field(String... fields);
	public DefaultWhereCauseBuilderField field(SingularAttribute<?, ?>... fields);
	
	public QueryBuilder end();
}
