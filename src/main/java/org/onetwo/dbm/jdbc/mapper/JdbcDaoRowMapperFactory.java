package org.onetwo.dbm.jdbc.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.onetwo.common.db.dquery.NamedQueryInvokeContext;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.map.CamelMap;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

public class JdbcDaoRowMapperFactory implements RowMapperFactory {
	
	private static final HashsetRowMapper HASHSET_ROW_MAPPER = new HashsetRowMapper();
	private static final ListRowMapper LIST_ROW_MAPPER = new ListRowMapper();
	private static final ObjectArrayRowMapper OBJECT_ARRAY_ROW_MAPPER = new ObjectArrayRowMapper();
	private static final CamelNameRowMapper CAMEL_NAME_ROW_MAPPER = new CamelNameRowMapper();
	private static final UnknowTypeRowMapper UNKNOW_TYPE_ROW_MAPPER = new UnknowTypeRowMapper();

	
	public JdbcDaoRowMapperFactory() {
		super();
	}
	
	public boolean isSingleColumnRowType(Class<?> type){
		return LangUtils.isSimpleType(type) || 
				LangUtils.isTimeClass(type) ||
				isMathBigNumber(type);
	}
	
	public static boolean isMathBigNumber(Class<?> clazz){
		if(clazz==null)
			return false;
		return BigDecimal.class.isAssignableFrom(clazz) || BigInteger.class.isAssignableFrom(clazz);
	}

	@Override
	public DataRowMapper<?> createRowMapper(Class<?> type) {
		DataRowMapper<?> rowMapper = null;
		if(type==null || type==Object.class){
//			rowMapper = new SingleColumnRowMapper(Object.class);
			rowMapper = UNKNOW_TYPE_ROW_MAPPER;
		}else if(isSingleColumnRowType(type)){
			//唯一，而且返回类型是简单类型，则返回单列的RowMapper
			rowMapper = new SingleColumnRowMapperAdapter<>(type);
		}else if(type==LinkedHashMap.class){
			rowMapper = new CustomMapRowMapper(colCount -> new LinkedHashMap<>(colCount));
		}else if(LangUtils.isMapClass(type)){
//			rowMapper = new ColumnMapRowMapper();
			rowMapper = CAMEL_NAME_ROW_MAPPER;
			
		}else if(Object[].class==type){
			rowMapper = OBJECT_ARRAY_ROW_MAPPER;
		}else if(List.class.isAssignableFrom(type)){
			rowMapper = LIST_ROW_MAPPER;
		}else if(Collection.class.isAssignableFrom(type)){
			rowMapper = HASHSET_ROW_MAPPER;
		}else{
			rowMapper = getBeanPropertyRowMapper(type);
		}
		return rowMapper;
	}

	@Override
	public DataRowMapper<?> createRowMapper(NamedQueryInvokeContext invokeContext) {
		return createRowMapper(invokeContext.getResultComponentClass());
	}

	protected DataRowMapper<?> getBeanPropertyRowMapper(Class<?> entityClass) {
//		return new BeanPropertyRowMapper<>(entityClass);
		return new BeanPropertyRowMapperAdapter<>(entityClass);
	}
	
	public static class CamelNameRowMapper extends ColumnMapRowMapper implements DataRowMapper<Map<String, Object>>{

		protected Map<String, Object> createColumnMap(int columnCount) {
			return new CamelMap<Object>(columnCount);
		}
	}
	
	public static class CustomMapRowMapper extends ColumnMapRowMapper implements DataRowMapper<Map<String, Object>>{
		private Function<Integer, Map<String, Object>> mapSupplier;
		
		public CustomMapRowMapper(Function<Integer, Map<String, Object>> mapSupplier) {
		}

		protected Map<String, Object> createColumnMap(int columnCount) {
			return mapSupplier.apply(columnCount);
		}
	}
	
	public static class SingleColumnRowMapperAdapter<T> extends SingleColumnRowMapper<T> implements DataRowMapper<T>{

		public SingleColumnRowMapperAdapter() {
		}
		
		public SingleColumnRowMapperAdapter(Class<T> requiredType) {
			setRequiredType(requiredType);
		}
	}

	public static class BeanPropertyRowMapperAdapter<T> extends BeanPropertyRowMapper<T> implements DataRowMapper<T>{

		public BeanPropertyRowMapperAdapter() {
		}

		public BeanPropertyRowMapperAdapter(Class<T> mappedClass) {
			initialize(mappedClass);
		}
	}

}
