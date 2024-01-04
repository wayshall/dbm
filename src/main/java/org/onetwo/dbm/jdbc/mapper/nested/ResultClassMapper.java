package org.onetwo.dbm.jdbc.mapper.nested;

import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import jakarta.persistence.Column;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onetwo.common.reflect.ClassIntroManager;
import org.onetwo.common.reflect.Intro;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.annotation.DbmNestedResult;
import org.onetwo.dbm.annotation.DbmNestedResult.NestedType;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.JdbcUtils;
import org.onetwo.dbm.jdbc.mapper.DataColumnMapper;
import org.onetwo.dbm.jdbc.mapper.DataRowMapper;
import org.onetwo.dbm.jdbc.mapper.DbmChildrenRowNestedMapping;
import org.onetwo.dbm.jdbc.mapper.RowResultContext;
import org.onetwo.dbm.jdbc.mapper.nested.AbstractNestedBeanMapper.ClassMapperContext;
import org.onetwo.dbm.jdbc.mapper.nested.AbstractNestedBeanMapper.ColumnProperty;
import org.onetwo.dbm.jdbc.mapper.nested.AbstractNestedBeanMapper.DbmNestedResultData;
import org.onetwo.dbm.jdbc.mapper.nested.AbstractNestedBeanMapper.PropertyMeta;
import org.onetwo.dbm.jdbc.spi.ColumnValueGetter;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;

import com.google.common.collect.Maps;

public class ResultClassMapper /*implements DataColumnMapper */{
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
//		this.classIntro = ClassIntroManager.getInstance().getIntro(mappedClass);
//		this.initialize(mappedClass);
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
			// 因为简单类型的集合使用了包装对象（SimpleValueNestedMappingHoder），所以这里必须强制其对应的idPropertyName为value
			// 在mapResultClassObjectWithoutResultSet方法通过判断是否SimpleValueNestedMappingHoder对象来使用不同的设置值方法后，可以注释下面的代码
//			if(StringUtils.isBlank(idPropertyName)){
//				idPropertyName = "value";
//			}
//			if(!"value".equals(idPropertyName)){
//				throw new DbmException("the value of id property (in @DbmNestedResult) must be 'value' if nested mapped type is a simple type!");
//			}
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
			if (idJProperty==null) {
				throw new DbmException("idPropertyName[" + idPropertyName + "] cannot be found on class: " + this.classIntro.getClazz().getName());
			}
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
					propertyMapper = new CollectionPropertyResultClassMapper(this, result, appendPrefix(result.getColumnPrefix(accessPath)), jproperty);
				}else if(nestedType==NestedType.MAP){
					propertyMapper = new MapPropertyResultClassMapper(this, result, appendPrefix(result.getColumnPrefix(accessPath)), jproperty);
				}else{
					propertyMapper = new PropertyResultClassMapper(this, result, appendPrefix(result.getColumnPrefix(accessPath)), jproperty);
				}
				propertyMapper.initialize();
				complexFields.put(jproperty.getName(), propertyMapper);
			}else{
				// 属性名
				String propertyName = pd.getName();
				if (classIntro.getClazz()==SimpleValueNestedMappingHoder.class) {
					propertyName = idProperty.getName();
				}
				
				ColumnProperty prop1 = new ColumnProperty(toClumnName1(propertyName), jproperty);
				ColumnProperty prop2 = new ColumnProperty(toClumnName2(propertyName), jproperty);
				this.simpleFields.put(getFullName(prop1.getColumnName()), prop1);
				this.simpleFields.put(getFullName(prop2.getColumnName()), prop2);
				
				// 增加@Column支持
				JFishProperty field = jproperty.getCorrespondingJFishProperty().orElse(null);
				if (field!=null) {
					Column fieldColumn = field.getAnnotation(Column.class);
					if (fieldColumn!=null) {
						ColumnProperty prop3 = new ColumnProperty(fieldColumn.name(), jproperty);
						this.simpleFields.put(prop3.getColumnName(), prop3);
					}
				}
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
//		return columnPrefix + name;
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
	
	protected boolean isIgnoreIfIdColumnNotFound() {
		return false;
	}
	
//	public Object mapResult(Map<String, Integer> names, ResultSetWrappingSqlRowSet resutSetWrapper){
	public Object mapResult(RowResultContext parentContext, Map<String, Integer> names, ColumnValueGetter columnValueGetter, int rowNum){
		ResultSetWrappingSqlRowSet resutSetWrapper = null;
		if (parentContext!=null) {
			resutSetWrapper = parentContext.getRowSet();
		}
		Integer hash = null;
		Object entity = null;
		BeanWrapper bw = null;
		boolean isNew = true;
		
		boolean userIdAsKey = hasIdField();
		
		if (userIdAsKey) {
			//根据id属性作为区分一条记录的标志
			String actualColumnName = getActualColumnName(names, idProperty);
			if (actualColumnName==null && isIgnoreIfIdColumnNotFound()) { // 若配置了id列不存在时忽略
				userIdAsKey = false;
			}
		}
		
		if(userIdAsKey){
			//根据id属性作为区分一条记录的标志
			String actualColumnName = getActualColumnName(names, idProperty);
			if(actualColumnName==null){
				throw new DbmException("id column not found on resultSet, id: " + idPropertyName+", columnPrefix:"+columnPrefix);
			}
			int index = names.get(actualColumnName);
			Object idValue = columnValueGetter.getColumnValue(index, idProperty.getType());
			if(idValue==null){
//				throw new DbmException("id column can not be null for specified id field: " + idPropertyName+", columnPrefix:"+columnPrefix);
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
		// 增加RowResultContext，便于子类控制
		RowResultContext rowContext = new RowResultContext(resutSetWrapper, bw, hash);
		for(PropertyResultClassMapper pm : this.complexFields.values()){
			Object propertyValue = pm.mapResult(rowContext, names, columnValueGetter, rowNum);
			boolean linkParent = false;
			if (propertyValue instanceof DbmChildrenRowNestedMapping) {
				DbmChildrenRowNestedMapping linkToPrent = (DbmChildrenRowNestedMapping) propertyValue;
				linkParent = linkToPrent.linkParent(pm.getBelongToProperty().getName(), entity);
			}
			// 如果linkParent为false，则dbm自动设置到parent属性
			if (!linkParent) {
				pm.linkToParent(bw, propertyValue);
			}
		}
		return afterMapResult(entity, hash, isNew);
	}
	protected BeanWrapper mapResultClassObject(ResultSetWrappingSqlRowSet resutSetWrapper, Map<String, Integer> names, ColumnValueGetter columnValueGetter, int rowNum){
		if (resutSetWrapper==null || dataColumnMapper==null || resultClass==SimpleValueNestedMappingHoder.class) {
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
		
//		DataRowMapper<?> drm = context.rowMapperFactory.getBeanPropertyRowMapper(classIntro.getClazz());
//		BeanWrapper bw;
//		try {
//			bw = drm.mapRowWithBeanWrapper(resutSetWrapper.getResultSet(), rowNum);
//		} catch (SQLException e) {
//			throw new DbmException("dbm row mapping error: " + e.getMessage(), e);
//		}
//		return bw;
	}

	protected BeanWrapper mapResultClassObjectWithoutResultSet(Map<String, Integer> names, ColumnValueGetter columnValueGetter){
		BeanWrapper bw = null;
//		boolean hasColumn = false;
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
				if (bw.getWrappedClass()==SimpleValueNestedMappingHoder.class) {
					SimpleValueNestedMappingHoder holder = (SimpleValueNestedMappingHoder) bw.getWrappedInstance();
					holder.setValue(value);
				} else {
					bw.setPropertyValue(jproperty.getName(), value);
				}
			}
		}
		return bw;
	}
	
	private Integer getIndexForColumnName(Map<String, Integer> names, String columnName){
		return names.get(columnName);
	}
	private String getActualColumnName(Map<String, Integer> names, PropertyMeta jproperty){
//		String fullName = getFullName(jproperty.getName());
//		String columName = toClumnName1(fullName);
		String columName = getFullName(toClumnName1(jproperty.getName()));
		if(names.containsKey(columName)){
			return columName;
		}
//		columName = toClumnName2(fullName);
		columName = getFullName(toClumnName2(jproperty.getName()));
		if(names.containsKey(columName)){
			return columName;
		}
		return null;
	}
	/***
	 * 第一种列名策略: 统一转小写
	 * @param fullName
	 * @return
	 */
	protected String toClumnName1(String fullName){
		return JdbcUtils.lowerCaseName(fullName);
	}
	/***
	 * 第二种列名策略：大小写转下划线
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

	protected static BeanWrapper createBeanWrapper(Object mappedObject) {
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
		return bw;
	}
}
