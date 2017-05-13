package org.onetwo.jpa.hibernate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.util.Assert;

/**
 * 在spring data的 Specifications 上扩展 
 * 没有全部实现操作符，用到哪个实现那个。。。
 * @author weishao zeng
 * <br/>
 */
@SuppressWarnings("serial")
public class SpecificationQuerys<T> implements Specification<T>, Serializable {

	public static final Order sortAsc(SingularAttribute<?, ?> field){
		return new Order(Direction.ASC, field.getName());
	}
	public static final Order sortAsc(String field){
		return new Order(Direction.ASC, field);
	}
	public static final Order sortDesc(String field){
		return new Order(Direction.DESC, field);
	}
	public static final Order sortDesc(SingularAttribute<?, ?> field){
		return new Order(Direction.DESC, field.getName());
	}
	
	private final Specification<T> spec;
	private Sort sort;

	private SpecificationQuerys(Specification<T> spec) {
		this.spec = spec;
	}

	private SpecificationQuerys(Specification<T> spec, Sort sort) {
		super();
		this.spec = spec;
		this.sort = sort;
	}

	public static <T> SpecificationQuerys<T> where(Specification<T> spec) {
		return new SpecificationQuerys<T>(spec);
	}

	public static <T> SpecificationQuerys<T> from(Class<T> clazz) {
		return new SpecificationQuerys<T>(null);
	}

	public QueryCauseField field(String field) {
		return new QueryCauseField(field);
	}
	public QueryCauseField field(SingularAttribute<?, ?> field) {
		return new QueryCauseField(field.getName());
	}

	public SpecificationQuerys<T> asc(String... fields) {
		Order[] orders = Stream.of(fields)
								.map(f->sortAsc(f))
								.collect(Collectors.toList())
								.toArray(new Order[0]);
		return orderBy(orders);
	}

	public SpecificationQuerys<T> asc(SingularAttribute<?, ?>... fields) {
		Order[] orders = Stream.of(fields)
								.map(f->sortAsc(f))
								.collect(Collectors.toList())
								.toArray(new Order[0]);
		return orderBy(orders);
	}

	public SpecificationQuerys<T> desc(String... fields) {
		Order[] orders = Stream.of(fields)
								.map(f->sortDesc(f))
								.collect(Collectors.toList())
								.toArray(new Order[0]);
		return orderBy(orders);
	}
	public SpecificationQuerys<T> desc(SingularAttribute<?, ?>... fields) {
		Order[] orders = Stream.of(fields)
				.map(f->sortDesc(f))
				.collect(Collectors.toList())
				.toArray(new Order[0]);
		return orderBy(orders);
	}

	public SpecificationQuerys<T> orderBy(Order... orders) {
		this.sort = new Sort(orders);
		return this;
	}
	
	public SpecificationQuerys<T> sort(Sort sort) {
		this.sort = sort;
		return this;
	}

	public SpecificationQuerys<T> and(Specification<T> other) {
		return new SpecificationQuerys<T>(new ComposedSpecification<T>(spec, other, CompositionType.AND), this.sort);
	}

	public SpecificationQuerys<T> or(Specification<T> other) {
		return new SpecificationQuerys<T>(new ComposedSpecification<T>(spec, other, CompositionType.OR), this.sort);
	}
	
	public static <T> SpecificationQuerys<T> not(Specification<T> spec) {
		return new SpecificationQuerys<T>(new NegatedSpecification<T>(spec));
	}

	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		return spec == null ? null : spec.toPredicate(root, query, builder);
	}
	
	/***
	 * 
	 * @author weishao zeng
	 * @param executor
	 * @return
	 */
	public List<T> getList(JpaSpecificationExecutor<T> executor){
		List<T> list = executor.findAll(spec, sort);
		return list;
	}
	public T getOne(JpaSpecificationExecutor<T> executor){
		T data = executor.findOne(spec);
		return data;
	}
	
	/****
	 * get list and group by
	 * @author weishao zeng
	 * @param executor
	 * @param keyer 决定分组的key，即返回的Map对象的key
	 * @return
	 */
	public <K> Map<K, List<T>> getMap(JpaSpecificationExecutor<T> executor, Function<? super T, ? extends K> keyer){
		List<T> list = executor.findAll(spec, sort);
		return list.stream()
					.collect(Collectors.groupingBy(keyer));
	}

	/**
	 * Enum for the composition types for {@link Predicate}s.
	 * 
	 * @author Thomas Darimont
	 */
	enum CompositionType {

		AND {
			@Override
			public Predicate combine(CriteriaBuilder builder, Predicate lhs, Predicate rhs) {
				return builder.and(lhs, rhs);
			}
		},

		OR {
			@Override
			public Predicate combine(CriteriaBuilder builder, Predicate lhs, Predicate rhs) {
				return builder.or(lhs, rhs);
			}
		};

		abstract Predicate combine(CriteriaBuilder builder, Predicate lhs, Predicate rhs);
	}

	/**
	 * A {@link Specification} that negates a given {@code Specification}.
	 * 
	 * @author Thomas Darimont
	 * @since 1.6
	 */
	private static class NegatedSpecification<T> implements Specification<T>, Serializable {

		private static final long serialVersionUID = 1L;

		private final Specification<T> spec;

		/**
		 * Creates a new {@link NegatedSpecification} from the given {@link Specification}
		 * 
		 * @param spec may be {@iteral null}
		 */
		public NegatedSpecification(Specification<T> spec) {
			this.spec = spec;
		}

		public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
			return spec == null ? null : builder.not(spec.toPredicate(root, query, builder));
		}
	}

	/**
	 * A {@link Specification} that combines two given {@code Specification}s via a given {@link CompositionType}.
	 * 
	 * @author Thomas Darimont
	 * @since 1.6
	 */
	private static class ComposedSpecification<T> implements Specification<T>, Serializable {

		private static final long serialVersionUID = 1L;

		private final Specification<T> lhs;
		private final Specification<T> rhs;
		private final CompositionType compositionType;

		/**
		 * Creates a new {@link ComposedSpecification} from the given {@link Specification} for the left-hand-side and the
		 * right-hand-side with the given {@link CompositionType}.
		 * 
		 * @param lhs may be {@literal null}
		 * @param rhs may be {@literal null}
		 * @param compositionType must not be {@literal null}
		 */
		private ComposedSpecification(Specification<T> lhs, Specification<T> rhs, CompositionType compositionType) {

			Assert.notNull(compositionType, "CompositionType must not be null!");

			this.lhs = lhs;
			this.rhs = rhs;
			this.compositionType = compositionType;
		}

		/**
		 * Returns {@link Predicate} for the given {@link Root} and {@link CriteriaQuery} that is constructed via the given
		 * {@link CriteriaBuilder}.
		 */
		public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

			Predicate otherPredicate = rhs == null ? null : rhs.toPredicate(root, query, builder);
			Predicate thisPredicate = lhs == null ? null : lhs.toPredicate(root, query, builder);

			return thisPredicate == null ? otherPredicate : otherPredicate == null ? thisPredicate : this.compositionType
					.combine(builder, thisPredicate, otherPredicate);
		}
	}


	/***
	 * 非string类型，不能为null；
	 * string类型，除了不能为null，还不能为blank
	 */
	public static final java.util.function.Predicate<Object> VALUE_NOT_BLANK = v->{
		return v!=null && ( !String.class.isInstance(v) || StringUtils.isNotBlank((String)v) );
	};
	public static final java.util.function.Predicate<Object> VALUE_NOT_NULL = v->v!=null;
	
	public class QueryCauseField {
		private String name;
		private SpecificationQuerys<T> querys = SpecificationQuerys.this;
		private java.util.function.Predicate<Object> fieldValueFilter = VALUE_NOT_BLANK;
		
		public QueryCauseField(String name) {
			super();
			this.name = name;
		}

		@SuppressWarnings("unchecked")
		private <S> List<S> filterValues(S... values){
			if(ArrayUtils.isEmpty(values)){
				return Collections.emptyList();
			}
			if(fieldValueFilter==null){
				return Arrays.asList(values);
			}
			return Stream.of(values)
							.filter(fieldValueFilter)
							.collect(Collectors.toList());
		}
		private <S> S filterValue(S value){
			if(fieldValueFilter==null){
				return value;
			}
			if(fieldValueFilter.test(value)){
				return value;
			}else{
				return null;
			}
		}

		public SpecificationQuerys<T> equalTo(Object... values){
			return operator(values, (ctx, value)->ctx.builder.equal(ctx.getField(), value));
		}

		public SpecificationQuerys<T> like(String... values){
			return operator(values, (ctx, value)->ctx.builder.like(ctx.getField(), value));
		}
		
		public SpecificationQuerys<T> in(Object... values){
			return operators(values, (ctx, listValue)->ctx.getField().in(listValue));
		}

		public <S extends Comparable<S>> SpecificationQuerys<T> between(S start, S end){
			S startValue = filterValue(start);
			S endValue = filterValue(end);
			if(isIgnore(startValue) || isIgnore(endValue)){
				return querys;
			}
			Specification<T> operator = (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)->{
				return cb.between(getField(root), start, end);
			};
			querys = querys.and(operator);
			return querys;
		}

		/***
		 * 
		 * @author wayshall
		 * @param values
		 * @param operatorFunc 处理单个参数
		 * @return
		 */
		private <V> SpecificationQuerys<T> operator(V[] values, BiFunction<SpecificationContext, V, Predicate> operatorFunc){
			List<?> listValue = filterValues(values);
			if(isIgnore(listValue)){
				return querys;
			}
			Specification<T> operator = toSpecification(values, operatorFunc);
			querys = querys.and(operator);
			return querys;
		}

		/***
		 * 
		 * @author wayshall
		 * @param values
		 * @param operatorFunc 处理多个参数
		 * @return
		 */
		private <V> SpecificationQuerys<T> operators(V[] values, BiFunction<SpecificationContext, List<V>, Predicate> operatorFunc){
			List<V> listValue = filterValues(values);
			if(isIgnore(listValue)){
				return querys;
			}
			Specification<T> operator = (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)->{
				SpecificationContext ctx = new SpecificationContext(root, query, cb);
				return operatorFunc.apply(ctx, listValue);
			};
			querys = querys.and(operator);
			return querys;
		}
		
		private <V> Specification<T> toSpecification(V[] values, BiFunction<SpecificationContext, V, Predicate> operator){
			return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)->{
				SpecificationContext ctx = new SpecificationContext(root, query, cb);
				List<Predicate> predicates = Stream.of(values).map(value->{
					return operator.apply(ctx, value);
				})
				.collect(Collectors.toList());
				return cb.or(predicates.toArray(new Predicate[predicates.size()]));
			};
		}

		final public SpecificationQuerys<T> valueFilter(java.util.function.Predicate<Object> fieldValueFilter) {
			this.fieldValueFilter = fieldValueFilter;
			return querys;
		}

		final public SpecificationQuerys<T> valueIgnoreNull() {
			this.fieldValueFilter = VALUE_NOT_NULL;
			return querys;
		}

		final public SpecificationQuerys<T> valueNotIgnore() {
			this.fieldValueFilter = null;
			return querys;
		}

		final public SpecificationQuerys<T> valueIgnoreBlank() {
			this.fieldValueFilter = VALUE_NOT_BLANK;
			return querys;
		}
		
		protected boolean isIgnore(List<?> list){
			return list.isEmpty();
		}
		
		protected boolean isIgnore(Object value){
			return value==null;
		}
		

		@SuppressWarnings("unchecked")
		protected <E> From<T, E> getFrom(Root<T> root) {
	        if (name.contains(".")) {
	            String joinField = StringUtils.split(name, ".")[0];
	            return root.join(joinField, JoinType.LEFT);
	        }
	        return (From<T, E>)root;
		}
		public <E> Expression<E> getField(Root<T> root){
			return getFrom(root).get(name);
		}
		
		class SpecificationContext {
			final private Root<T> root;
			final private CriteriaQuery<?> query;
			final private CriteriaBuilder builder;
			public SpecificationContext(Root<T> root, CriteriaQuery<?> query,
					CriteriaBuilder cb) {
				super();
				this.root = root;
				this.query = query;
				this.builder = cb;
			}
			public From<T, T> getFrom() {
		        return QueryCauseField.this.getFrom(root);
			}
			public <E> Expression<E> getField(){
				return getFrom().get(name);
			}
			public CriteriaQuery<?> getQuery() {
				return query;
			}
			public CriteriaBuilder getBuilder() {
				return builder;
			}
			
		}

	}

}
