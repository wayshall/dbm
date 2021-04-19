package org.onetwo.common.db.builder;

import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.metamodel.SingularAttribute;

import org.onetwo.common.db.sqlext.ExtQueryUtils;
import org.onetwo.common.db.sqlext.SQLOps;
import org.onetwo.common.db.sqlext.SQLSymbolManager.FieldOP;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.common.utils.func.Closure;


@SuppressWarnings("unchecked")
public class DefaultWhereCauseBuilderField<E> extends WhereCauseBuilderField<E> {
	
	private String[] fields;
	private String op;
	private Object[] values;
	
	private Supplier<Boolean> whenPredicate;

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

	public DefaultWhereCauseBuilderField<E> when(Supplier<Boolean> predicate) {
		this.whenPredicate = predicate;
		return this;
	}

	/***
	 *  like '%value'
	 * @author weishao zeng
	 * @param values
	 * @return
	 */
	public WhereCauseBuilder<E> prelike(String... values) {
		return this.doWhenPredicate(()-> {
			this.op = FieldOP.like;
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
			this.op = FieldOP.like;
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
			this.op = FieldOP.not_like;
			this.values = values;
		});
//		this.op = FieldOP.not_like;
//		this.values = values;
//		this.queryBuilder.addField(this);
//		return queryBuilder;
	}
	
	public WhereCauseBuilder<E> like(String... values) {
		return this.doWhenPredicate(()-> {
			this.op = FieldOP.like;
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
	public <T> WhereCauseBuilder<E> equalTo(T... values) {
		return this.doWhenPredicate(()->{
			this.op = FieldOP.eq;
			this.values = values;
		});
	}

	public <T> WhereCauseBuilder<E> value(SQLOps sqlOp, Supplier<T> valueSupplier) {
		return this.doWhenPredicate(()->{
			this.op = sqlOp.getSymbol();
			this.values = new Object[] {valueSupplier.get()};
		});
	}

	public <T> WhereCauseBuilder<E> values(SQLOps sqlOp, Supplier<T[]> valueSupplier) {
		return this.doWhenPredicate(()->{
			this.op = sqlOp.getSymbol();
			this.values = valueSupplier.get();
		});
	}
	
	public <T> WhereCauseBuilder<E> is(T... values) {
		return equalTo(values);
	}
	
	public <T> WhereCauseBuilder<E> is(Supplier<T> valueSupplier) {
		return value(SQLOps.EQUAL, valueSupplier);
	}
	
	public WhereCauseBuilder<E> isNull(boolean isNull) {
		return this.doWhenPredicate(()->{
			this.op = FieldOP.is_null;
			this.values = new Object[]{isNull};
		});
	}
	
	protected WhereCauseBuilder<E> doWhenPredicate(Closure whenAction){
		boolean rs = whenPredicate==null?true:Optional.ofNullable(whenPredicate.get()).orElse(false);
		if(rs){
			whenAction.execute();
			queryBuilder.addField(this);
			whenPredicate = null;
		}
		return queryBuilder;
	}

	/****
	 * 不等于
	 * @param values
	 * @return
	 */
	public <T> WhereCauseBuilder<E> notEqualTo(T... values) {
		return this.doWhenPredicate(()->{
			this.op = FieldOP.neq;
			this.values = values;
		});
	}

	/****
	 * 大于
	 * @param values
	 * @return
	 */
	public <T> WhereCauseBuilder<E> greaterThan(T... values) {
		return this.doWhenPredicate(()->{
			this.op = FieldOP.gt;
			this.values = values;
		});
		/*
		this.op = FieldOP.gt;
		this.values = values;
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}
	
	public <T> WhereCauseBuilder<E> in(T... values) {
		return this.doWhenPredicate(()->{
			this.op = FieldOP.in;
			this.values = values;
		});
		/*
		this.op = FieldOP.in;
		this.values = values;
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}
	
	public <T> WhereCauseBuilder<E> notIn(T... values) {
		return this.doWhenPredicate(()->{
			this.op = FieldOP.not_in;
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
			this.op = FieldOP.date_in;
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
			this.op = FieldOP.between;
			this.values = new Object[]{start, end};
		});
	}
	
	/****
	 * 大于或者等于
	 * @param values
	 * @return
	 */
	public <T> WhereCauseBuilder<E> greaterEqual(T... values) {
		return this.doWhenPredicate(()->{
			this.op = FieldOP.ge;
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
	public <T> WhereCauseBuilder<E> lessThan(T... values) {
		return this.doWhenPredicate(()->{
			this.op = FieldOP.lt;
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
	public <T> WhereCauseBuilder<E> lessEqual(T... values) {
		return this.doWhenPredicate(()->{
			this.op = FieldOP.le;
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
			this.op = FieldOP.is_null;
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
			this.op = FieldOP.is_null;
			this.setValues(false);
		});
		
		/*this.op = FieldOP.is_null;
		this.setValues(false);
		this.queryBuilder.addField(this);
		return queryBuilder;*/
	}
	
	protected void setValues(Object val){
		this.values = new Object[this.fields.length];
		for(int i=0; i<this.fields.length; i++){
			this.values[i] = val;
		}
	}
	
	public String[] getOPFields(){
		return ExtQueryUtils.appendOperationToFields(fields, op);
	}

	public Object[] getValues() {
		return values;
	}

}
