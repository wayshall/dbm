package org.onetwo.common.db.builder;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.ArrayUtils;
import org.onetwo.common.db.sqlext.ExtQueryUtils;
import org.onetwo.common.db.sqlext.QueryDSLOps;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.common.utils.func.Closure;


public class DefaultWhereCauseBuilderField<E> extends WhereCauseBuilderField<E, WhereCauseBuilder<E>> {
	
	private String[] fields;
	private QueryDSLOps op;
	private QueryDSLOps[] ops;
	private Object values;
	
	protected Supplier<Boolean> whenPredicate;
	// 是否已添加到queryBuilder
	private boolean added;
	private boolean autoAddField = true;

	public DefaultWhereCauseBuilderField(WhereCauseBuilder<E> squery, String... fields) {
		super(squery);
		this.fields = fields;
	}
	
	public DefaultWhereCauseBuilderField(WhereCauseBuilder<E> squery, SingularAttribute<?, ?>... fields) {
		super(squery);
		this.fields = Stream.of(fields)
							.map(f->f.getName())
							.collect(Collectors.toList())
							.toArray(new String[0]);
	}


	/***
	 *  like '%value'
	 * @author weishao zeng
	 * @param values
	 * @return
	 */
	public WhereCauseBuilder<E> prelike(String... values) {
		return this.doWhenPredicate(()-> {
			this.op = QueryDSLOps.LIKE;
			this.values = Stream.of(values)
								.map(val -> StringUtils.appendStartWith(val, "%"))
								.collect(Collectors.toList())
								.toArray(new String[0]);
		});
//		this.op = FieldOP.like;
//		this.values = Stream.of(values)
//							.map(val -> StringUtils.appendStartWith(val, "%"))
//							.collect(Collectors.toList())
//							.toArray(new String[0]);
//		this.queryBuilder.addField(this);
//		return queryBuilder;
	}

	/***
	 *  like 'value%'
	 * @author weishao zeng
	 * @param values
	 * @return
	 */
	public WhereCauseBuilder<E> postlike(String... values) {
		return this.doWhenPredicate(()-> {
			this.op = QueryDSLOps.LIKE;
			this.values = Stream.of(values)
								.map(val -> StringUtils.appendEndWith(val, "%"))
								.collect(Collectors.toList())
								.toArray(new String[0]);
		});
//		this.op = FieldOP.like;
//		this.values = Stream.of(values)
//							.map(val -> StringUtils.appendEndWith(val, "%"))
//							.collect(Collectors.toList())
//							.toArray(new String[0]);
//		this.queryBuilder.addField(this);
//		return queryBuilder;
	}

	public WhereCauseBuilder<E> notLike(String... values) {
		return this.doWhenPredicate(()-> {
			this.op = QueryDSLOps.NOT_LIKE;
			this.values = values;
		});
//		this.op = FieldOP.not_like;
//		this.values = values;
//		this.queryBuilder.addField(this);
//		return queryBuilder;
	}
	
	public WhereCauseBuilder<E> like(String... values) {
		return this.doWhenPredicate(()-> {
			this.op = QueryDSLOps.LIKE;
			this.values = values;
		});
//		this.op = FieldOP.like;
//		this.values = values;
//		this.queryBuilder.addField(this);
//		return queryBuilder;
	}
	
	/***
	 * 等于
	 * @param values
	 * @return
	 */
	public WhereCauseBuilder<E> equalTo(Object... values) {
		return this.doWhenPredicate(()->{
//			this.op = QueryDSLOps.EQ;
			this.values = values;
			setOp(QueryDSLOps.EQ);
		});
	}

	public WhereCauseBuilder<E> value(QueryDSLOps sqlOp, Supplier<Object> valueSupplier) {
		return this.doWhenPredicate(()->{
			this.op = sqlOp;
			this.values = new Object[] {valueSupplier.get()};
		});
	}

	public <T> WhereCauseBuilder<E> values(QueryDSLOps sqlOp, Supplier<T[]> valueSupplier) {
		return this.doWhenPredicate(()->{
			this.op = sqlOp;
			this.values = valueSupplier.get();
		});
	}

	public DefaultWhereCauseBuilderField<E> or(QueryDSLOps sqlOps, Object values) {
		this.ops = ArrayUtils.add(this.ops, sqlOps);
		this.values = CUtils.arrayAdd((Object[])this.values, values);
		return this;
	}

	public WhereCauseBuilder<E> end() {
		if (this.isAdded()) {
			return queryBuilder;
		}
		this.doWhenPredicate(()->{
		});
		return queryBuilder;
	}
	
	public WhereCauseBuilder<E> is(Object... values) {
		return equalTo(values);
	}
	
	public WhereCauseBuilder<E> is(Supplier<Object> valueSupplier) {
		return value(QueryDSLOps.EQ, valueSupplier);
	}
	
	public WhereCauseBuilder<E> isNull(boolean isNull) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.IS_NULL;
			this.values = new Object[]{isNull};
		});
	}
	
	protected WhereCauseBuilder<E> doWhenPredicate(Closure setValueAction){
		boolean rs = whenPredicate==null?true:Optional.ofNullable(whenPredicate.get()).orElse(false);
		if(rs){
			setValueAction.execute();
			if (autoAddField) {
				this.addField();
			}
		}
		return queryBuilder;
	}
	
	void addField() {
		if (this.added) {
			return ;
		}
		queryBuilder.addField(this);
		this.markAdded();
	}
	
//	public WhereCauseBuilder<E> ifElse(boolean predicate, Closure1<DefaultWhereCauseBuilderField<E>> ifAction, Closure1<DefaultWhereCauseBuilderField<E>> elseAction){
//		return ifElse(()->predicate, ifAction, elseAction);
//	}
//	
//	public WhereCauseBuilder<E> ifElse(Supplier<Boolean> predicate, Closure1<DefaultWhereCauseBuilderField<E>> ifAction, Closure1<DefaultWhereCauseBuilderField<E>> elseAction){
//		Assert.notNull(predicate);
//		Assert.notNull(ifAction);
//		Assert.notNull(elseAction);
//		boolean rs = predicate==null?true:Optional.ofNullable(predicate.get()).orElse(false);
//		if(rs){
//			ifAction.execute(this);
//		} else {
//			elseAction.execute(this);
//		}
//		queryBuilder.addField(this);
//		whenPredicate = null;
//		return queryBuilder;
//	}
	
//	public WhereCauseBuilder<E> on(Consumer<DefaultWhereCauseBuilderField<E>> action){
//		return on(field -> {
//			action.accept(field);
//			return true;
//		});
//	}
//	
//	public WhereCauseBuilder<E> on(Function<DefaultWhereCauseBuilderField<E>, Boolean> action){
//		Assert.notNull(action);
//		Boolean res = action.apply(this);
//		if (res!=null && res) {
//			queryBuilder.addField(this);
//			this.markAdded();
//		}
//		whenPredicate = null;
//		return queryBuilder;
//	}

	/****
	 * 不等于
	 * @param values
	 * @return
	 */
	public WhereCauseBuilder<E> notEqualTo(Object... values) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.NEQ;
			this.values = values;
		});
	}

	/****
	 * 大于
	 * @param values
	 * @return
	 */
	public WhereCauseBuilder<E> greaterThan(Object... values) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.GT;
			this.values = values;
		});
		/*
		this.op = FieldOP.gt;
		this.values = values;
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}
	
	public WhereCauseBuilder<E> in(Object... values) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.IN;
			this.values = values;
		});
	}
	
	public WhereCauseBuilder<E> in(Collection<?> values) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.IN;
			this.values = values;
		});
		/*
		this.op = FieldOP.in;
		this.values = values;
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}
	
	public WhereCauseBuilder<E> notIn(Object... values) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.NOT_IN;
			this.values = values;
		});
		/*
		this.op = FieldOP.not_in;
		this.values = values;
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}
	
	/***
	 * 如果只有第一个参数，则条件为：>=start这天的零点，<start+1天的零点
	 * 如果两个参数，则条件为：>=start, <end
	 * @author weishao zeng
	 * @param start
	 * @param end
	 * @return
	 */
	public WhereCauseBuilder<E> dateIn(Date start, Date end) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.DATE_IN;
			if (end==null) {
				this.values = new Date[]{start};
			} else {
				this.values = new Date[]{start, end};
			}
		});
		/*
		this.op = FieldOP.date_in;
		this.values = new Date[]{start, end};
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}

	/****
	 * 解释为sql的between start and end
	 * 是否包含边界值需要根据数据库来确定
	 * mysql 和 oracle均包含边界值
	 * @author weishao zeng
	 * @param start
	 * @param end
	 * @return
	 */
	public WhereCauseBuilder<E> between(final Object start, final Object end) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.BETWEEN;
			this.values = new Object[]{start, end};
		});
	}
	
	/****
	 * 大于或者等于
	 * @param values
	 * @return
	 */
	public WhereCauseBuilder<E> greaterEqual(Object... values) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.GE;
			this.values = values;
		});
		/*
		this.op = FieldOP.ge;
		this.values = values;
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}

	/****
	 * 少于
	 * @param values
	 * @return
	 */
	public WhereCauseBuilder<E> lessThan(Object... values) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.LT;
			this.values = values;
		});
		/*
		this.op = FieldOP.lt;
		this.values = values;
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}

	/****
	 * 少于或等于
	 * @param values
	 * @return
	 */
	public WhereCauseBuilder<E> lessEqual(Object... values) {
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.LE;
			this.values = values;
		});
		/*
		this.op = FieldOP.le;
		this.values = values;
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}
	
	public WhereCauseBuilder<E> isNull(){
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.IS_NULL;
			this.setValues(true);
		});
		/*
		this.op = FieldOP.is_null;
		this.setValues(true);
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}
	
	public WhereCauseBuilder<E> isNotNull(){
		return this.doWhenPredicate(()->{
			this.op = QueryDSLOps.IS_NULL;
			this.setValues(false);
		});
		
		/*this.op = FieldOP.is_null;
		this.setValues(false);
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}
	
	protected void setValues(Object val){
		Object[] values = new Object[this.fields.length];
		for(int i=0; i<this.fields.length; i++){
			values[i] = val;
		}
		this.values = values;
	}
	
	public String[] getOPFields(){
		String[] opFields = null;
		if (ops!=null) {
			opFields = ExtQueryUtils.appendOperationToFields(fields, ops);
		} else {
			opFields = ExtQueryUtils.appendOperationToFields(fields, op);
		}
		return opFields;
	}

	public Object getValues() {
		return values;
	}

	protected void setOp(QueryDSLOps op) {
		this.op = op;
	}

}
