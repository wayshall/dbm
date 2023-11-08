package org.onetwo.common.db.builder;

import java.util.Optional;
import java.util.function.Supplier;

abstract public class WhereCauseBuilderField<E, R> implements WhereCauseField<E, R> {
	
	protected WhereCauseBuilder<E> queryBuilder;
	protected Supplier<Boolean> whenPredicate;
	// 是否已添加到queryBuilder
	protected boolean added;

	public WhereCauseBuilderField(WhereCauseBuilder<E> squery) {
		super();
		this.queryBuilder = squery;
	}

	protected WhereCauseBuilder<E> getQueryBuilder() {
		return queryBuilder;
	}

	public WhereCauseBuilder<E> end() {
		return queryBuilder;
	}

	abstract public String[] getOPFields();
	
	abstract public Object getValues();

	void markAdded() {
		this.added = true;
		this.whenPredicate = null;
	}
	
	boolean isAdded() {
		return this.added;
	}
	
	void addField() {
		if (isAdded()) {
			return ;
		}
		boolean rs = whenPredicate==null?true:Optional.ofNullable(whenPredicate.get()).orElse(false);
		if(rs){
			queryBuilder.addField(this);
			this.markAdded();
		}
	}
}
