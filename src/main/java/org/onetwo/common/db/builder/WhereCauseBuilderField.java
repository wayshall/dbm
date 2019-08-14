package org.onetwo.common.db.builder;

abstract public class WhereCauseBuilderField<E> {
	
	protected WhereCauseBuilder<E> queryBuilder;

	public WhereCauseBuilderField(WhereCauseBuilder<E> squery) {
		super();
		this.queryBuilder = squery;
	}

	protected WhereCauseBuilder<E> getQueryBuilder() {
		return queryBuilder;
	}

	abstract public String[] getOPFields();
	
	abstract public Object[] getValues();

}
