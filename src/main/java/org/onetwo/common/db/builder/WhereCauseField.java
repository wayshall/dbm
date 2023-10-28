package org.onetwo.common.db.builder;

/**
 * @author weishao zeng
 * <br/>
 */
public interface WhereCauseField<E, R> {
	
//	WhereCauseField<E, R> when(Supplier<Boolean> predicate);

//	/***
//	 *  like '%value'
//	 * @author weishao zeng
//	 * @param values
//	 * @return
//	 */
//	WhereCauseBuilder<E> prelike(String... values);
//
//	/***
//	 *  like 'value%'
//	 * @author weishao zeng
//	 * @param values
//	 * @return
//	 */
//	WhereCauseBuilder<E> postlike(String... values);
//
//	WhereCauseBuilder<E> notLike(String... values);
//
//	WhereCauseBuilder<E> like(String... values);
//
//	/***
//	 * 等于
//	 * @param values
//	 * @return
//	 */
//	WhereCauseBuilder<E> equalTo(Object... values);
//
//	WhereCauseBuilder<E> value(QueryDSLOps sqlOp, Supplier<Object> valueSupplier);
//
//	WhereCauseBuilderField<E> or(QueryDSLOps sqlOps, Object values);
//
//	WhereCauseBuilder<E> end();
//
	R is(Object... values);
//
//	WhereCauseBuilder<E> isNull(boolean isNull);
//
//	/****
//	 * 不等于
//	 * @param values
//	 * @return
//	 */
//	WhereCauseBuilder<E> notEqualTo(Object... values);
//
//	/****
//	 * 大于
//	 * @param values
//	 * @return
//	 */
//	WhereCauseBuilder<E> greaterThan(Object... values);
//
//	WhereCauseBuilder<E> in(Object... values);
//
//	WhereCauseBuilder<E> in(Collection<?> values);
//
//	WhereCauseBuilder<E> notIn(Object... values);
//
//	/***
//	 * 如果只有第一个参数，则条件为：>=start这天的零点，<start+1天的零点
//	 * 如果两个参数，则条件为：>=start, <end
//	 * @author weishao zeng
//	 * @param start
//	 * @param end
//	 * @return
//	 */
//	WhereCauseBuilder<E> dateIn(Date start, Date end);
//
//	/****
//	 * 解释为sql的between start and end
//	 * 是否包含边界值需要根据数据库来确定
//	 * mysql 和 oracle均包含边界值
//	 * @author weishao zeng
//	 * @param start
//	 * @param end
//	 * @return
//	 */
//	WhereCauseBuilder<E> between(Object start, Object end);
//
//	/****
//	 * 大于或者等于
//	 * @param values
//	 * @return
//	 */
//	WhereCauseBuilder<E> greaterEqual(Object... values);
//
//	/****
//	 * 少于
//	 * @param values
//	 * @return
//	 */
//	WhereCauseBuilder<E> lessThan(Object... values);
//
//	/****
//	 * 少于或等于
//	 * @param values
//	 * @return
//	 */
//	WhereCauseBuilder<E> lessEqual(Object... values);
//
//	WhereCauseBuilder<E> isNull();
//
//	WhereCauseBuilder<E> isNotNull();

	String[] getOPFields();

	Object getValues();

}