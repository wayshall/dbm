package org.onetwo.dbm.jdbc.mapper;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onetwo.common.convert.Types;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.ClassIntroManager;
import org.onetwo.common.reflect.Intro;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.annotation.DbmNestedResult;
import org.onetwo.dbm.annotation.DbmNestedResult.NestedType;
import org.onetwo.dbm.annotation.DbmResultMapping;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.JdbcUtils;
import org.onetwo.dbm.jdbc.spi.ColumnValueGetter;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;

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

	protected static BeanWrapper createBeanWrapper(Object mappedObject) {
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
		return bw;
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
		public DbmNestedResultData(DbmNestedResult nested) {
			this(nested.property(), nested.id(), nested.columnPrefix(), nested.nestedType());
		}
		public DbmNestedResultData(String property, String id, String columnPrefix, NestedType nestedType) {
			super();
			this.property = property;
			this.id = id;
			this.columnPrefix = columnPrefix;
			this.nestedType = nestedType;
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
	static class PropertyResultClassMapper extends ResultClassMapper {
		final private ResultClassMapper parentMapper;
		/***
		 * 映射对象所属的父对象的属性
		 */
		final private JFishProperty belongToProperty;
		public PropertyResultClassMapper(ResultClassMapper parentMapper, String idField, String columnPrefix, JFishProperty belongToProperty) {
			this(parentMapper, idField, columnPrefix, belongToProperty, belongToProperty.getType());
		}
		public PropertyResultClassMapper(ResultClassMapper parentMapper, String idField, String columnPrefix, JFishProperty belongToProperty, Class<?> propertyType) {
			super(parentMapper.context, idField, columnPrefix, propertyType);
			this.belongToProperty = belongToProperty;
			this.parentMapper = parentMapper;
			this.accessPathPrefix = getAcessPath(parentMapper.accessPathPrefix, belongToProperty.getName());
		}
		public JFishProperty getBelongToProperty() {
			return belongToProperty;
		}
		public void linkToParent(BeanWrapper parent, Object propertyValue){
			if(propertyValue!=null){
				parent.setPropertyValue(belongToProperty.getName(), propertyValue);
			}
		}
		public ResultClassMapper getParentMapper() {
			return parentMapper;
		}
		public Object getPropertyValue(Object propertyValue){
			if(propertyValue instanceof SimpleValueNestedMappingHoder){
				propertyValue = ((SimpleValueNestedMappingHoder)propertyValue).value;
			}
			return propertyValue;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static class CollectionPropertyResultClassMapper extends PropertyResultClassMapper {
		private Intro<? extends Collection> collectionClassIntro;
		public CollectionPropertyResultClassMapper(
				ResultClassMapper parentMapper, 
				String idField, String columnPrefix,
				JFishProperty belongToProperty) {
			super(parentMapper, idField, columnPrefix, belongToProperty, (Class<?>)belongToProperty.getFirstParameterType());
			if(belongToProperty.getFirstParameterType()==null){
				throw new DbmException("the collection property must be a parameterType: " + belongToProperty.getName());
			}
			collectionClassIntro = (Intro<? extends Collection>)belongToProperty.getTypeClassWrapper();
			if(!collectionClassIntro.isCollection()){
				throw new DbmException("the nested property ["+belongToProperty.getName()+"] must be Collection Type: " + belongToProperty.getName());
			}
		}
		
		public void linkToParent(BeanWrapper parent, Object propertyValue){
			if(propertyValue==null){
				return ;
			}
			
			propertyValue = getPropertyValue(propertyValue);
			String propName = getBelongToProperty().getName();
			Collection values = (Collection)parent.getPropertyValue(propName);
			if(values==null){
				values = collectionClassIntro.newInstance();
				parent.setPropertyValue(propName, values);
			}
			if(!values.contains(propertyValue)){
				values.add(propertyValue);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static class MapPropertyResultClassMapper extends PropertyResultClassMapper {
		private Intro<? extends Map> mapClassIntro;
		private Class<?> keyClass;
		public MapPropertyResultClassMapper(
				ResultClassMapper parentMapper, 
				String idField, String columnPrefix,
				JFishProperty belongToProperty) {
			super(parentMapper, idField, columnPrefix, belongToProperty, (Class<?>)belongToProperty.getParameterType(1));
			if(StringUtils.isBlank(idField)){
				throw new DbmException("you must configure the id property for map : " + belongToProperty.getName());
			}
			this.keyClass = (Class<?>)belongToProperty.getParameterType(0);
			if(keyClass==null || getResultClass()==null){
				throw new DbmException("the Map property must be a parameterType: " + belongToProperty.getName());
			}
			mapClassIntro = (Intro<? extends Map>)belongToProperty.getTypeClassWrapper();
			if(!mapClassIntro.isMap()){
				throw new DbmException("the nested property ["+belongToProperty.getName()+"] must be Map Type: " + belongToProperty.getName());
			}
		}
		
		@Override
		public void initialize() {
			super.initialize();
			if(getIdProperty()==null){
				throw new DbmException("the configured id property["+getIdPropertyName()+"] not found in : " + getResultClass());
			}
		}
		
		public void linkToParent(BeanWrapper parent, Object propertyValue){
			if(propertyValue==null){
				return ;
			}
			if(!hasIdField()){
				throw new DbmException("no id configured for map : " + this.getBelongToProperty().getName());
			}
			Object id = ReflectUtils.getPropertyValue(propertyValue, getIdProperty().getName());
			if(id==null){
				throw new DbmException("id value can not be null for map : " + this.getBelongToProperty().getName());
			}
			String propName = getBelongToProperty().getName();
			id = Types.convertValue(id, keyClass);
			Map values = (Map)parent.getPropertyValue(getBelongToProperty().getName());
			if(values==null){
				values = mapClassIntro.newInstance();
				parent.setPropertyValue(propName, values);
			}
			values.put(id, propertyValue);
		}
	}

	public static class SimpleValueNestedMappingHoder {
		Object value;
		public void setValue(Object value) {
			this.value = value;
		}
		public Object getValue() {
			return value;
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
	protected static class ResultClassMapper /*implements DataColumnMapper */{
		/****
		 * 用来决定某一个对象（一行数据）是否是相同的属性，如果此属性的值相同，则无论其它属性是否相同，均视为同一条数据
		 */
		private String idPropertyName = "";
		/***
		 * 用来决定某一个对象（一行数据）是否是相同的属性，如果此属性的值相同，则无论其它属性是否相同，均视为同一条数据
		 */
		private PropertyMeta idProperty;
		
		private String columnPrefix = "";
		private Intro<?> classIntro;
		private Map<String, ColumnProperty> simpleFields = Maps.newHashMap();
		private Map<String, PropertyResultClassMapper> complexFields = Maps.newHashMap();
		protected Map<Integer, BeanWrapper> datas = Maps.newHashMap();
		private Class<?> resultClass;
		protected String accessPathPrefix;
		protected final ClassMapperContext context;
		private DataColumnMapper dataColumnMapper;
		
		public ResultClassMapper(ClassMapperContext context, String idField, String columnPrefix, Class<?> mappedClass) {
			super();
			this.idPropertyName = idField;
			this.columnPrefix = columnPrefix;
//			this.classIntro = ClassIntroManager.getInstance().getIntro(mappedClass);
//			this.initialize(mappedClass);
			this.resultClass = mappedClass;
			this.context = context;
			
			this.dataColumnMapper = this.context.getRowMapperFactory().map(f -> {
				DataRowMapper<?> drm = f.createRowMapper(resultClass);
				if (drm instanceof DataColumnMapper) {
					return (DataColumnMapper)drm;
				} else {
					return null;
				}
			})
			.orElse(null);
			
			if(LangUtils.isSimpleType(mappedClass)){
				this.resultClass = SimpleValueNestedMappingHoder.class;
				if(StringUtils.isBlank(idPropertyName)){
					idPropertyName = "value";
				}
				if(!"value".equals(idPropertyName)){
					throw new DbmException("the value of id property (in @DbmNestedResult) must be 'value' if nested mapped type is a simple type!");
				}
				this.idProperty = new PropertyMeta(idPropertyName, mappedClass, true);
			}
		}
		
		public String getIdPropertyName() {
			return idPropertyName;
		}

		public Class<?> getResultClass() {
			return resultClass;
		}

		public PropertyMeta getIdProperty() {
			return idProperty;
		}

		public boolean hasIdField(){
			return idProperty!=null;
		}
		
		public static String getAcessPath(String accessPathPrefix, String propName){
			String acessPath = StringUtils.isBlank(accessPathPrefix)?propName:accessPathPrefix + "." + propName;
			return acessPath;
		}
		
		public void initialize() {
			this.classIntro = ClassIntroManager.getInstance().getIntro(resultClass);
			if(idProperty==null && StringUtils.isNotBlank(idPropertyName)){
				JFishProperty idJProperty = this.classIntro.getJFishProperty(idPropertyName, false);
				try {
					this.idProperty = new PropertyMeta(idPropertyName, idJProperty.getPropertyDescriptor().getPropertyType(), false);
				} catch (Exception e) {
					throw new DbmException("create id property error:"+e.getMessage(), e);
				}
			}
			/*if(this.idProperty!=null && this.idProperty.simpleValue){
				return ;
			}*/
			for (PropertyDescriptor pd : classIntro.getProperties()) {
				if(pd.getWriteMethod()==null){
					continue;
				}
				JFishProperty jproperty = getActualJFishProperty(pd.getName());
				String accessPath = getAcessPath(accessPathPrefix, jproperty.getName());
				DbmNestedResultData result = context.getDbmNestedResult(accessPath);
				if(result!=null){
					PropertyResultClassMapper propertyMapper = null;
					NestedType nestedType = result.getNestedType();
					if(nestedType==NestedType.COLLECTION){
						propertyMapper = new CollectionPropertyResultClassMapper(this, result.getId(), appendPrefix(result.getColumnPrefix(accessPath)), jproperty);
					}else if(nestedType==NestedType.MAP){
						propertyMapper = new MapPropertyResultClassMapper(this, result.getId(), appendPrefix(result.getColumnPrefix(accessPath)), jproperty);
					}else{
						propertyMapper = new PropertyResultClassMapper(this, result.getId(), appendPrefix(result.getColumnPrefix(accessPath)), jproperty);
					}
					propertyMapper.initialize();
					complexFields.put(jproperty.getName(), propertyMapper);
				}else{
					/*this.simpleFields.put(getFullName(toClumnName1(pd.getName())), jproperty);
					this.simpleFields.put(getFullName(toClumnName2(pd.getName())), jproperty);*/
					ColumnProperty prop1 = new ColumnProperty(toClumnName1(pd.getName()), jproperty);
					ColumnProperty prop2 = new ColumnProperty(toClumnName2(pd.getName()), jproperty);
					this.simpleFields.put(getFullName(prop1.getColumnName()), prop1);
					this.simpleFields.put(getFullName(prop2.getColumnName()), prop2);
				}
			}
		}
		
		private JFishProperty getActualJFishProperty(String propName){
			JFishProperty jproperty = classIntro.getJFishProperty(propName, false);
			JFishProperty actualProperty = jproperty;
			Optional<JFishProperty> fieldProperty = jproperty.getCorrespondingJFishProperty();
			if(!jproperty.hasAnnotation(DbmNestedResult.class) 
					&& fieldProperty.isPresent()
					&& fieldProperty.get().hasAnnotation(DbmNestedResult.class)){
				actualProperty = fieldProperty.get();
			}
			return actualProperty;
		}
		
		protected String appendPrefix(String name){
			//父类的前缀+自己的前缀
//			return columnPrefix + name;
			//直接返回自己的前缀，不再添加父类的前缀
			return name;
		}
		
		protected String getFullName(String propName){
			//前缀+属性
			return columnPrefix + propName;
		}
		
		protected Object afterMapResult(Object entity, Integer hash, boolean isNew){
			return entity;
		}
		
//		public Object mapResult(Map<String, Integer> names, ResultSetWrappingSqlRowSet resutSetWrapper){
		public Object mapResult(ResultSetWrappingSqlRowSet resutSetWrapper, Map<String, Integer> names, ColumnValueGetter columnValueGetter, int rowNum){
			Integer hash = null;
			Object entity = null;
			BeanWrapper bw = null;
			boolean isNew = true;
			if(hasIdField()){
				//根据id属性作为区分一条记录的标志
				String actualColumnName = getActualColumnName(names, idProperty);
				if(actualColumnName==null){
					throw new DbmException("no id column found on resultSet for specified id: " + idPropertyName+", columnPrefix:"+columnPrefix);
				}
				int index = names.get(actualColumnName);
				Object idValue = columnValueGetter.getColumnValue(index, idProperty.getType());
				if(idValue==null){
//					throw new DbmException("id column can not be null for specified id field: " + idPropertyName+", columnPrefix:"+columnPrefix);
					return null;
				}
				hash = idValue.hashCode();
				if(datas.containsKey(hash)){
					bw = datas.get(hash);
					isNew = false;
				}else{
					bw = mapResultClassObject(resutSetWrapper, names, columnValueGetter, rowNum);
					datas.put(hash, bw);
				}
			}else{
				bw = mapResultClassObject(resutSetWrapper, names, columnValueGetter, rowNum);
				if(bw==null){
					return null;
				}
				hash = HashCodeBuilder.reflectionHashCode(bw.getWrappedInstance());
				if(datas.containsKey(hash)){
					bw = datas.get(hash);
					isNew = false;
				}else{
					datas.put(hash, bw);
				}
			}
			entity = bw.getWrappedInstance();
			for(PropertyResultClassMapper pm : this.complexFields.values()){
				Object propertyValue = pm.mapResult(resutSetWrapper, names, columnValueGetter, rowNum);
				pm.linkToParent(bw, propertyValue);
			}
			return afterMapResult(entity, hash, isNew);
		}
		protected BeanWrapper mapResultClassObject(ResultSetWrappingSqlRowSet resutSetWrapper, Map<String, Integer> names, ColumnValueGetter columnValueGetter, int rowNum){
			if (resutSetWrapper==null || dataColumnMapper==null) {
				return mapResultClassObjectWithoutResultSet(names, columnValueGetter);
			}
			Object mappedObject = classIntro.newInstance();
			BeanWrapper bw = createBeanWrapper(mappedObject);
			for(Entry<String, ColumnProperty> entry : simpleFields.entrySet()){
				Integer index = getIndexForColumnName(names, entry.getKey());
				if(index==null){
					continue;
				}
				this.dataColumnMapper.setColumnValue(resutSetWrapper, bw, rowNum, index, entry.getValue().getColumnName());
			}
			return bw;
		}

		protected BeanWrapper mapResultClassObjectWithoutResultSet(Map<String, Integer> names, ColumnValueGetter columnValueGetter){
			BeanWrapper bw = null;
//			boolean hasColumn = false;
			for(Entry<String, ColumnProperty> entry : simpleFields.entrySet()){
				JFishProperty jproperty = entry.getValue().getProperty();
				/*String actualColumnName = getActualColumnName(names, jproperty);
				if(actualColumnName==null){
					continue;
				}else if(!hasColumn){
					hasColumn = true;
				}
				int index = names.get(actualColumnName);*/
				Integer index = getIndexForColumnName(names, entry.getKey());
				if(index==null){
					continue;
				}
				Object value = columnValueGetter.getColumnValue(index, jproperty.getPropertyDescriptor().getPropertyType());
				if(value!=null){
					if(bw==null){
						Object mappedObject = classIntro.newInstance();
						bw = createBeanWrapper(mappedObject);
					}
					bw.setPropertyValue(jproperty.getName(), value);
				}
			}
			return bw;
		}
		
		private Integer getIndexForColumnName(Map<String, Integer> names, String columnName){
			return names.get(columnName);
		}
		private String getActualColumnName(Map<String, Integer> names, PropertyMeta jproperty){
//			String fullName = getFullName(jproperty.getName());
//			String columName = toClumnName1(fullName);
			String columName = getFullName(toClumnName1(jproperty.getName()));
			if(names.containsKey(columName)){
				return columName;
			}
//			columName = toClumnName2(fullName);
			columName = getFullName(toClumnName2(jproperty.getName()));
			if(names.containsKey(columName)){
				return columName;
			}
			return null;
		}
		/***
		 * 第一种列名策略
		 * @param fullName
		 * @return
		 */
		protected String toClumnName1(String fullName){
			return JdbcUtils.lowerCaseName(fullName);
		}
		/***
		 * 第二种列名策略
		 * @param fullName
		 * @return
		 */
		protected String toClumnName2(String fullName){
			return JdbcUtils.underscoreName(fullName);
		}
		/*public Object getPropertyValue(Map<String, Integer> names, ResultSetWrappingSqlRowSet resutSetWrapper, JFishProperty jproperty, String colName){
			Object value = null;
			Integer index = names.get(colName);
			value = getColumnValue(resutSetWrapper, index, jproperty.getPropertyDescriptor());
			return value;
		}*/

	}

}
