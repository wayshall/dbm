package org.onetwo.common.db.builder;

import java.util.Collection;
import java.util.Date;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.onetwo.common.db.builder.SingleFieldWhereCauseBuilderField.SingleFieldWhereCauseBuilderFieldEnder;
import org.onetwo.common.db.sqlext.ExtQueryUtils;
import org.onetwo.common.db.sqlext.QueryDSLOps;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.StringUtils;

@SuppressWarnings("unchecked")
public class SingleFieldWhereCauseBuilderField<E> extends WhereCauseBuilderField<E, SingleFieldWhereCauseBuilderFieldEnder<E>> {
	
	private String field;
	private QueryDSLOps[] ops;
	private Object values;
	private SingleFieldWhereCauseBuilderFieldEnder<E> ender;
	

	public SingleFieldWhereCauseBuilderField(WhereCauseBuilder<E> squery, String field) {
		super(squery);
		this.field = field;
		this.ender = new SingleFieldWhereCauseBuilderFieldEnder<>(this);
	}

	public SingleFieldWhereCauseBuilderField<E> when(Supplier<Boolean> predicate) {
		this.whenPredicate = predicate;
		return this;
	}
	
	/***
	 *  like '%value'
	 * @author weishao zeng
	 * @param values
	 * @return
	 */
	public SingleFieldWhereCauseBuilderFieldEnder<E> prelike(String... values) {
		Object[] strings = Stream.of(values)
							.map(val -> StringUtils.appendStartWith(val, "%"))
							.collect(Collectors.toList())
							.toArray();
		setOpValues(QueryDSLOps.LIKE, strings);
		return ender();
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
	public SingleFieldWhereCauseBuilderFieldEnder<E> postlike(String... values) {
		Object[] strings = Stream.of(values)
				.map(val -> StringUtils.appendEndWith(val, "%"))
				.collect(Collectors.toList())
				.toArray();
		setOpValues(QueryDSLOps.LIKE, strings);
		return ender();
	}

	public SingleFieldWhereCauseBuilderFieldEnder<E> notLike(String... values) {
		setOpValues(QueryDSLOps.NOT_LIKE, (Object[])values);
		return ender();
	}
	
	public SingleFieldWhereCauseBuilderFieldEnder<E> like(String... values) {
		setOpValues(QueryDSLOps.LIKE, (Object[])values);
		return ender();
	}
	
	/***
	 * 等于
	 * @param values
	 * @return
	 */
	public <T> SingleFieldWhereCauseBuilderFieldEnder<E> equalTo(T... values) {
		setOpValues(QueryDSLOps.EQ, values);
		return ender();
	}

	public SingleFieldWhereCauseBuilderFieldEnder<E> value(QueryDSLOps sqlOp, Supplier<Object> valueSupplier) {
		setOpValues(sqlOp, valueSupplier.get());
		return ender();
	}


	public WhereCauseBuilder<E> end() {
		this.addField();
		return queryBuilder;
	}
	
	public <T> SingleFieldWhereCauseBuilderFieldEnder<E> is(T... values) {
		return equalTo(values);
	}
	
	public SingleFieldWhereCauseBuilderFieldEnder<E> isNull(boolean isNull) {
//		return this.doWhenPredicate(()->{
//			this.op = QueryDSLOps.IS_NULL;
//			this.values = new Object[]{isNull};
//		});
//		this.setOpValue(QueryDSLOps.IS_NULL, isNull);
		this.setOpValues(QueryDSLOps.IS_NULL, isNull);
		return ender();
	}
	

	/****
	 * 不等于
	 * @param values
	 * @return
	 */
	public <T> SingleFieldWhereCauseBuilderFieldEnder<E> notEqualTo(T... values) {
		this.setOpValues(QueryDSLOps.NEQ, values);
		return ender();
	}

	/****
	 * 大于
	 * @param values
	 * @return
	 */
	public <T> SingleFieldWhereCauseBuilderFieldEnder<E> greaterThan(T... values) {
		this.setOpValues(QueryDSLOps.GT, values);
		return ender();
	}
	
	public <T> SingleFieldWhereCauseBuilderFieldEnder<E> in(T... values) {
//		this.setOpValues(QueryDSLOps.IN, values);
		this.setOpValues(QueryDSLOps.IN, new Object[] { values });
		return ender();
	}
	
	public <T> SingleFieldWhereCauseBuilderFieldEnder<E> in(Collection<T> values) {
//		this.setOpValues(QueryDSLOps.IN, values.toArray());
		this.setOpValues(QueryDSLOps.IN, new Object[] { values });
		return ender();
	}
	
	public <T> SingleFieldWhereCauseBuilderFieldEnder<E> notIn(T... values) {
//		this.setOpValues(QueryDSLOps.NOT_IN, values);
		this.setOpValues(QueryDSLOps.NOT_IN, new Object[] { values });
		return ender();
	}
	
	/***
	 * 如果只有第一个参数，则条件为：>=start这天的零点，<start+1天的零点
	 * 如果两个参数，则条件为：>=start, <end
	 * @author weishao zeng
	 * @param start
	 * @param end
	 * @return
	 */
	public SingleFieldWhereCauseBuilderFieldEnder<E> dateIn(Date start, Date end) {
		Object[] values = null;
		if (end==null) {
			values = new Date[]{start};
		} else {
			values = new Date[]{start, end};
		}
		setOpValues(QueryDSLOps.DATE_IN, new Object[] { values });
		return ender();
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
	public SingleFieldWhereCauseBuilderFieldEnder<E> between(final Object start, final Object end) {
		Object[] range = new Object[]{start, end};
		setOpValues(QueryDSLOps.BETWEEN, new Object[] { range });
		return ender();
	}
	
	/****
	 * 大于或者等于
	 * @param values
	 * @return
	 */
	public <T> SingleFieldWhereCauseBuilderFieldEnder<E> greaterEqual(T... values) {
		this.setOpValues(QueryDSLOps.GE, values);
		return ender();
	}

	/****
	 * 少于
	 * @param values
	 * @return
	 */
	public <T> SingleFieldWhereCauseBuilderFieldEnder<E> lessThan(T... values) {
		this.setOpValues(QueryDSLOps.LT, values);
		return ender();
	}

	/****
	 * 少于或等于
	 * @param values
	 * @return
	 */
	public <T> SingleFieldWhereCauseBuilderFieldEnder<E> lessEqual(T... values) {
		this.setOpValues(QueryDSLOps.LE, values);
		return ender();
	}
	
	public SingleFieldWhereCauseBuilderFieldEnder<E> isNull(){
		this.isNull(true);
		return ender();
	}
	
	protected SingleFieldWhereCauseBuilderFieldEnder<E> ender() {
		return ender;
	}
	
	public SingleFieldWhereCauseBuilderFieldEnder<E> isNotNull(){
		this.isNull(false);
		return ender();
	}
	
	@Override
	public String[] getOPFields(){
		String[] opFields = ExtQueryUtils.appendOperationToFields(field, this.ops);
		return opFields;
	}

	@Override
	public Object getValues() {
		return values;
	}

	protected void setOpValue(QueryDSLOps op, Object values) {
		this.setOp(op);
		this.values = new Object[] { values };
	}
	
	protected void setOpValues(QueryDSLOps op, Object... values) {
		QueryDSLOps[] tempOps = null;
		Object[] tempValues = null;
		if (values==null || values.length==0) {
			tempOps = new QueryDSLOps[] { op };
			tempValues = new Object[] { null };
		} else {
			tempOps = new QueryDSLOps[values.length];
			int index = 0;
			for (int i = 0; i < values.length; i++) {
				tempOps[index] = op;
				index++;
			}
			tempValues = values;
		}
		
		this.ops = ArrayUtils.addAll(this.ops, tempOps);
		this.values = CUtils.addAll((Object[])this.values, tempValues);
	}
	
	public void setOp(QueryDSLOps op) {
		this.ops = new QueryDSLOps[] { op };
	}

	static public class SingleFieldWhereCauseBuilderFieldEnder<E> {
		SingleFieldWhereCauseBuilderField<E> field;
		public SingleFieldWhereCauseBuilderFieldEnder(SingleFieldWhereCauseBuilderField<E> field) {
			super();
			this.field = field;
		}
		
		public SingleFieldWhereCauseBuilderField<E> field(String fieldName) {
			this.field.addField();
			return this.field.queryBuilder.field(fieldName);
		}

		public WhereCauseField<E, ?> field(String... fieldName) {
			this.field.addField();
			return this.field.queryBuilder.field(fieldName);
		}
		
		public SingleFieldWhereCauseBuilderField<E> or() {
			return this.field;
		}

		public WhereCauseBuilder<E> orQuery() {
			this.field.addField();
			return this.field.queryBuilder.or();
		}

		public WhereCauseBuilder<E> andQuery() {
			this.field.addField();
			return this.field.queryBuilder.and();
		}

		public WhereCauseBuilder<E> ignoreIfNull(){
			WhereCauseBuilder<E> wb = endSub();
			wb.ignoreIfNull();
			return wb;
		}


		public QueryAction<E> toQuery(){
			return end().toQuery();
		}
		
		public QueryAction<E> toSelect() {
			return end().toSelect();
		}
		
		public ExecuteAction toExecute() {
			WhereCauseBuilder<E> wb = endSub();
			return wb.toExecute();
		}
		
		public QueryBuilder<E> end() {
			this.field.addField();
			return this.field.queryBuilder.getQueryBuilder();
		}
		
		public WhereCauseBuilder<E> endSub() {
			this.field.addField();
			WhereCauseBuilder<E> q = this.field.queryBuilder;
			if (q.getParent()!=null) {
				return q.endSub();
			}
			return q;
		}
	}

}
