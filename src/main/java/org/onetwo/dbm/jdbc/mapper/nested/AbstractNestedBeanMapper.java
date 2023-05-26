package org.onetwo.dbm.jdbc.mapper.nested;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.dbm.annotation.DbmNestedResult;
import org.onetwo.dbm.annotation.DbmNestedResult.NestedType;
import org.onetwo.dbm.annotation.DbmResultMapping;
import org.onetwo.dbm.jdbc.mapper.DbmRowMapperFactory;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanWrapper;
import org.springframework.core.convert.ConversionService;

import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;
import lombok.Data;

abstract public class AbstractNestedBeanMapper<T> {
	final protected Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	private static ConversionService conversionService = DbmUtils.CONVERSION_SERVICE;

	public static ConversionService getConversionService() {
		return conversionService;
	}
	
	protected Class<T> mappedClass;
	//for hashCode and equals
	protected DbmResultMapping dbmResultMapping;
	
	protected ResultClassMapper resultClassMapper;
//	protected JdbcResultSetGetter jdbcResultSetGetter;
	private DbmRowMapperFactory rowMapperFactory;

	/*public AbstractNestedBeanMapper(JdbcResultSetGetter jdbcResultSetGetter, Class<T> mappedClass, DbmResultMapping dbmResultMapping) {
		this.mappedClass = mappedClass;
		this.dbmResultMapping = dbmResultMapping;
		if(jdbcResultSetGetter!=null){
			ClassMapperContext context = new ClassMapperContext(dbmResultMapping);
			ResultClassMapper resultClassMapper = new RootResultClassMapper(context, dbmResultMapping.idField(), dbmResultMapping.columnPrefix(), mappedClass);
			resultClassMapper.initialize();
			this.resultClassMapper = resultClassMapper;
		}
	}*/

	public AbstractNestedBeanMapper(DbmRowMapperFactory rowMapperFactory, Class<T> mappedClass, DbmResultMapping dbmResultMapping) {
		this.mappedClass = mappedClass;
		this.dbmResultMapping = dbmResultMapping;
		this.rowMapperFactory = rowMapperFactory;

		ClassMapperContext context = new ClassMapperContext(rowMapperFactory, dbmResultMapping);
		ResultClassMapper resultClassMapper = new RootResultClassMapper(context, dbmResultMapping.idField(), dbmResultMapping.columnPrefix(), mappedClass);
		resultClassMapper.initialize();
		this.resultClassMapper = resultClassMapper;
	}
	
	public DbmRowMapperFactory getRowMapperFactory() {
		return rowMapperFactory;
	}
	
	protected static void initBeanWrapper(BeanWrapper bw) {
		ConversionService cs = getConversionService();
		if (cs != null) {
			bw.setConversionService(cs);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dbmResultMapping == null) ? 0 : dbmResultMapping.hashCode());
		result = prime * result
				+ ((mappedClass == null) ? 0 : mappedClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractNestedBeanMapper<?> other = (AbstractNestedBeanMapper<?>) obj;
		if (dbmResultMapping == null) {
			if (other.dbmResultMapping != null)
				return false;
		} else if (!dbmResultMapping.equals(other.dbmResultMapping))
			return false;
		if (mappedClass == null) {
			if (other.mappedClass != null)
				return false;
		} else if (!mappedClass.equals(other.mappedClass))
			return false;
		return true;
	}

	static class DbmNestedResultData {
		final private String property;
		final private String id;
		final private String columnPrefix;
		final private NestedType nestedType;
		final private boolean ignoreIfIdColumnNotFound;
		public DbmNestedResultData(DbmNestedResult nested) {
			this(nested.property(), nested.id(), nested.columnPrefix(), nested.nestedType(), nested.ignoreIfIdColumnNotFound());
		}
		public DbmNestedResultData(String property, String id, String columnPrefix, NestedType nestedType, boolean ignoreIfIdColumnNotFound) {
			super();
			this.property = property;
			this.id = id;
			this.columnPrefix = columnPrefix;
			this.nestedType = nestedType;
			this.ignoreIfIdColumnNotFound = ignoreIfIdColumnNotFound;
		}
		public String getProperty() {
			return property;
		}
		public String getId() {
			return id;
		}
		public String getColumnPrefix() {
			return columnPrefix;
		}
		public String getColumnPrefix(String accessPath) {
			String fullPrefix = StringUtils.isBlank(columnPrefix)?accessPath.replace('.', '_')+"_":columnPrefix;
			return fullPrefix;
		}
		public NestedType getNestedType() {
			return nestedType;
		}
		public boolean isIgnoreIfIdColumnNotFound() {
			return ignoreIfIdColumnNotFound;
		}
		
	}

	protected static class ClassMapperContext {
//		protected JdbcResultSetGetter jdbcResultSetGetter;
		protected Map<String, DbmNestedResultData> accessPathResultClassMapperMap = Maps.newHashMap();
		private DbmRowMapperFactory rowMapperFactory;
		
		public ClassMapperContext(DbmRowMapperFactory rowMapperFactory, DbmResultMapping dbmResultMapping) {
			super();
//			this.jdbcResultSetGetter = jdbcResultSetGetter;
			this.rowMapperFactory = rowMapperFactory;
			for(DbmNestedResult nested : dbmResultMapping.value()){
				this.accessPathResultClassMapperMap.put(nested.property(), new DbmNestedResultData(nested));
			}
		}
		
		public DbmNestedResultData getDbmNestedResult(String accessPath){
			return this.accessPathResultClassMapperMap.get(accessPath);
		}

		public Optional<DbmRowMapperFactory> getRowMapperFactory() {
			return Optional.ofNullable(rowMapperFactory);
		}
		
	}


	protected static class RootResultClassMapper extends ResultClassMapper {

		public RootResultClassMapper(ClassMapperContext context,
				String idField, String columnPrefix, Class<?> mappedClass) {
			super(context, idField, columnPrefix, mappedClass);
		}

		@Override
		protected Object afterMapResult(Object entity, Integer hash, boolean isNew){
			if(!isNew){
				return null;
			}
			return entity;
		}
	}
	
	protected static class PropertyMeta {
		final String name;
		final Class<?> type;
		final boolean simpleValue;
		public PropertyMeta(String name, Class<?> type, boolean simpleValue) {
			super();
			this.name = name;
			this.type = type;
			this.simpleValue = simpleValue;
		}
		public String getName() {
			return name;
		}
		public Class<?> getType() {
			return type;
		}
		
	}
	@Data
	@AllArgsConstructor
	protected static class ColumnProperty {
		private String columnName;
		private JFishProperty property;
	}
	

}
