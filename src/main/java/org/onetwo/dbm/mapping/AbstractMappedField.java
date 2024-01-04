package org.onetwo.dbm.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.annotation.DbmFieldConvert;
import org.onetwo.dbm.annotation.DbmFieldListeners;
import org.onetwo.dbm.annotation.DbmGenerated;
import org.onetwo.dbm.annotation.DbmJsonField;
import org.onetwo.dbm.event.spi.DbmEventAction;
import org.onetwo.dbm.id.StrategyType;
import org.onetwo.dbm.jpa.GeneratedValueIAttrs;
import org.onetwo.dbm.mapping.enums.DbmMappingEnumType;
import org.onetwo.dbm.mapping.version.VersionableType;
import org.onetwo.dbm.utils.DbmUtils;
import org.onetwo.dbm.utils.SpringAnnotationFinder;
import org.springframework.beans.ConfigurablePropertyAccessor;


@SuppressWarnings("unchecked")
abstract public class AbstractMappedField implements DbmMappedField{
	
	private final DbmMappedEntry entry;
	
	private String name;
	private String label;
	/***
	 * 是否@Id标识的主键
	 */
	private boolean identify;
//	private PropertyDescriptor property;
	private BaseColumnInfo column;

	private DbmMappedFieldType mappedFieldType = DbmMappedFieldType.FIELD;
	private StrategyType strategyType;
	private final JFishProperty propertyInfo;
	
//	private DataHolder<String, Object> dataHolder = new DataHolder<String, Object>();
	
	private boolean freezing;
	
	private List<DbmEntityFieldListener> fieldListeners = Collections.EMPTY_LIST;
	
	private VersionableType<? extends Object> versionableType;
	
	/****
	 * 实际映射类型
	 */
	private Class<?> actualMappingColumnType;
	final private DbmJsonField jsonFieldAnnotation;
//	final private DbmFieldConvert dbmFieldAnnotation;
	
	private DbmEnumType enumType;
	
	private DbmFieldValueConverter fieldValueConverter;
	final private boolean mappingGenerated;
	
	private Collection<DbmMappedField> bindedFields;
	/***
	 * 绑定到了哪个字段
	 */
	private DbmMappedField bindToField;
	
	public AbstractMappedField(DbmMappedEntry entry, JFishProperty propertyInfo) {
		super();
		this.entry = entry;
		this.propertyInfo = propertyInfo;
		this.name = propertyInfo.getName();
		
		this.mappingGenerated = propertyInfo.hasAnnotation(DbmGenerated.class);
		
		this.propertyInfo.getAnnotationInfo().setAnnotationFinder(SpringAnnotationFinder.INSTANCE);
		
		if(propertyInfo.hasAnnotation(Enumerated.class)){
			Enumerated enumerated = propertyInfo.getAnnotation(Enumerated.class);
			if (enumerated.value()==EnumType.STRING) {
				this.enumType = DbmEnumType.STRING;
			} else {
				this.enumType = DbmEnumType.ORDINAL;
			}
		} else if (Enum.class.isAssignableFrom(propertyInfo.getType())){
			this.enumType = DbmEnumType.STRING;
		}
		// 如果配置了ORDINAL，并且实现了DbmEnumValueMapping接口，则修改为DbmMappingEnumType
		if(DbmEnumValueMapping.class.isAssignableFrom(this.propertyInfo.getType())) {
			Class<?> actualEnumValueType = ReflectUtils.resolveClassOfGenericType(DbmEnumValueMapping.class, this.propertyInfo.getType());
			this.enumType = new DbmMappingEnumType(actualEnumValueType);
		}
		
		DbmFieldListeners listenersAnntation = propertyInfo.getAnnotation(DbmFieldListeners.class);
		if(listenersAnntation!=null){
			fieldListeners = DbmUtils.initDbmEntityFieldListeners(listenersAnntation);
		}else{
			if(!entry.getFieldListeners().isEmpty()){
				this.fieldListeners = new ArrayList<DbmEntityFieldListener>(entry.getFieldListeners());
			}
		}
		this.jsonFieldAnnotation = propertyInfo.getAnnotation(DbmJsonField.class);
		

//		CompositedFieldValueConverter compositedConverter = CompositedFieldValueConverter.composited();
//		if(enumType!=null){
//			compositedConverter.addFieldValueConverter(CompositedFieldValueConverter.ENUM_CONVERTER);
//		}
		/*if (getName().equals("appCode") && getEntry().getEntityName().equals("org.onetwo.common.dbm.model.hib.entity.UserEntity")) {
			System.out.println("test");
		}*/
//		this.dbmFieldAnnotation = propertyInfo.getAnnotation(DbmField.class);
		/*if(jsonFieldAnnotation != null){
			compositedConverter.addFieldValueConverter(JsonFieldValueConverter.getInstance());
		}*/
//		if(this.dbmFieldAnnotation!=null){
//			Class<? extends DbmFieldValueConverter> converterClass = this.dbmFieldAnnotation.converterClass();
//			DbmFieldValueConverter converter = DbmUtils.createDbmBean(converterClass);
//			compositedConverter.addFieldValueConverter(converter);
//			/*this.dbmFieldAnnotation.forEach(f -> {
//				Class<? extends DbmFieldValueConverter> converterClass = f.converterClass();
//				DbmFieldValueConverter converter = DbmUtils.createDbmBean(converterClass);
//				compositedConverter.addFieldValueConverter(converter);
//			});*/
//		}
		this.fieldValueConverter = this.scanDbmFieldConverters(propertyInfo);
		
		
		this.obtainActaulMappingType();
	}
	
	private CompositedFieldValueConverter scanDbmFieldConverters(JFishProperty propertyInfo) {
		CompositedFieldValueConverter compositedConverter = CompositedFieldValueConverter.composited();
		if(enumType!=null){
			compositedConverter.addFieldValueConverter(CompositedFieldValueConverter.ENUM_CONVERTER);
		}
		
		Set<DbmFieldConvert> fieldAnnos = propertyInfo.getMergedRepeatableAnnotations(DbmFieldConvert.class);
		fieldAnnos.forEach(f -> {
			Class<? extends DbmFieldValueConverter> converterClass = f.converterClass();
			DbmFieldValueConverter converter = DbmUtils.createDbmBean(converterClass);
			compositedConverter.addFieldValueConverter(converter);
		});
		compositedConverter.sort();
		return compositedConverter;
	}
	
	private void obtainActaulMappingType(){
		Class<?> actualType = propertyInfo.getType();
		if(enumType!=null){
			actualType = this.enumType.getJavaType();
		}else if(this.jsonFieldAnnotation !=null ){
//			actualType = String.class;
			actualType = this.jsonFieldAnnotation.convertibleJavaType().getJavaType();
		}
		this.actualMappingColumnType = actualType;
	}

	public boolean isMappingGenerated() {
		return mappingGenerated;
	}

	public DbmFieldValueConverter getFieldValueConverter() {
		return fieldValueConverter;
	}

	public boolean isEnumerated(){
		return enumType!=null;
	}
	
	public boolean isEnumeratedOrdinal() {
		return enumType==DbmEnumType.ORDINAL;
	}
	
	/****
	 * 和 {@link #getValue(Object)}对应
	 * 把value（一般从数据库获取）设置为实体的实际值（经过转换器转换的值，如转为java枚举量）
	 * @see 和 {@link #getValue(Object)}对应
	 */
	@Override
	public void setValue(Object entity, Object value){
		value = DbmUtils.convertFromSqlParameterValue(this, value);
//		Object actaulValue = this.fieldValueConverter.forJava(this, value);;
		Object actaulValue = this.getActualJavaValue(value);;
		propertyInfo.setValue(entity, actaulValue);
		
		// 同时设置此字段绑定的字段的值
		processBindedFieldOnSetValue(entity, actaulValue);
	}
	
	/***
	 * 设置通过@DbmBindValueToField 注解绑定到此字段的其它字段的值
	 * 
	 * @author weishao zeng
	 * @param entity
	 * @param value
	 */
	protected void processBindedFieldOnSetValue(Object entity, Object value) {
		Collection<DbmMappedField> bindedFields = this.getBindedFields();
		if (LangUtils.isEmpty(bindedFields)) {
			return ;
		}
		for(DbmMappedField field : bindedFields) {
			field.setValue(entity, value);
		}
	}
	
	/***
	 * 
	 * 和 {@link #setValue(Object)}对应
	 * 根据字段的配置信息，获取实体字段的实际值，用于保存到数据库
	 * 
	 * @see {@link #setValue(Object)}
	 */
	@Override
	public Object getValue(Object entity){
		Assert.notNull(entity);
		Object value = null;
		
		// 如果绑定到了某个字段，则从这个字段里获取值
		if (this.bindToField!=null) {
			value = this.bindToField.getValue(entity);
			return value;
		}
		
		// 处理复合主键的情况
//		if (!propertyInfo.getType().equals(entity.getClass())) {
		if (this.getEntry().isCompositePK()) {
			ConfigurablePropertyAccessor accessor = SpringUtils.newPropertyAccessor(entity, !propertyInfo.isBeanProperty());
			value = accessor.getPropertyValue(this.propertyInfo.getName());
		} else {
			value = propertyInfo.getValue(entity);
		}
//		value = this.fieldValueConverter.forStore(this, value);
		value = this.getActualStoreValue(value);
		return value;
	}
	

	@Override
	public Object getActualStoreValue(Object fieldValue) {
//		Assert.notNull(fieldValue);
		Object value = this.fieldValueConverter.forStore(this, fieldValue);
		return value;
	}

	@Override
	public Object getActualJavaValue(Object fieldValue) {
//		Assert.notNull(fieldValue);
		Object value = this.fieldValueConverter.forJava(this, fieldValue);
		return value;
	}
	
	public Object fireDbmEntityFieldEvents(final Object fieldValue, DbmEventAction eventAction){
//		boolean doListener = false;
		Object newFieldValue = fieldValue;
		if(eventAction.isInserting()){
			if(!fieldListeners.isEmpty()){
				for(DbmEntityFieldListener fl : fieldListeners){
					newFieldValue = fl.beforeFieldInsert(this, newFieldValue);
//					doListener = true;
				}
			}
		} else if(eventAction.isUpdating()){
			if(!fieldListeners.isEmpty()){
				for(DbmEntityFieldListener fl : fieldListeners){
					newFieldValue = fl.beforeFieldUpdate(this, newFieldValue);
//					doListener = true;
				}
			}
		}
		return newFieldValue;
	}
	
	public Class<?> getColumnType(){
		return actualMappingColumnType;
	}
	
	@Override
	public boolean isIdentify() {
		return identify;
	}


	@Override
	public void setIdentify(boolean identify) {
		this.checkFreezing("setIdentify");
		this.identify = identify;
	}

	@Override
	public BaseColumnInfo getColumn() {
		return column;
	}

	@Override
	public void setColumn(BaseColumnInfo column) {
		this.checkFreezing("setColumn");
		this.column = column;
	}

	@Override
	public String getName() {
		return name;
	}

	void setName(String name) {
		this.checkFreezing("setName");
		this.name = name;
	}


	@Override
	public DbmMappedEntry getEntry() {
		return entry;
	}

	@Override
	public String getLabel() {
		if(StringUtils.isBlank(label)){
			return getName();
		}
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	/*@Override
	public boolean isGeneratedValueFetchBeforeInsert() {
		return isSeqStrategy() || getGeneratedValueIAttrs().getGenerationType()==GenerationType.TABLE;
	}*/

	@Override
	public boolean isGeneratedValue() {
		GeneratedValueIAttrs attrs = getGeneratedValueIAttrs();
		return attrs!=null && !isIdentityStrategy();
	}

	@Override
	public boolean isSeqStrategy() {
		return this.strategyType==StrategyType.SEQ;
	}

	/***
	 * 是否id自增策略
	 */
	@Override
	public boolean isIdentityStrategy() {
		return this.strategyType==StrategyType.IDENTITY;
	}

	@Override
	public StrategyType getStrategyType() {
		return strategyType;
	}

	@Override
	public void setStrategyType(StrategyType strategyType) {
		this.strategyType = strategyType;
	}

	@Override
	public JFishProperty getPropertyInfo() {
		return propertyInfo;
	}

	@Override
	public void freezing() {
		freezing = true;
	}

	@Override
	public void checkFreezing(String name) {
		if(isFreezing()){
			throw new UnsupportedOperationException("the field["+getName()+"] is freezing now, don not supported this operation : " + name);
		}
	}

	@Override
	public boolean isFreezing() {
		return freezing;
	}

	@Override
	public DbmMappedFieldType getMappedFieldType() {
		return mappedFieldType;
	}

	@Override
	public final void setMappedFieldType(DbmMappedFieldType mappedFieldType) {
		this.mappedFieldType = mappedFieldType;
	}
	
	@Override
	public boolean isJoinTableField(){
		return false;
	}

	/*@Override
	public DataHolder<String, Object> getDataHolder() {
		return dataHolder;
	}*/
	
	public String toString(){
		return LangUtils.append(getName());
	}

	public List<DbmEntityFieldListener> getFieldListeners() {
		return fieldListeners;
	}

	public boolean isVersionControll() {
		return versionableType!=null;
	}

	public VersionableType<? extends Object> getVersionableType() {
		return versionableType;
	}

	public void setVersionableType(VersionableType<? extends Object> versionableType) {
		this.versionableType = versionableType;
	}
	
	@Override
	public DbmEnumType getEnumType() {
		return enumType;
	}
	
	public DbmJsonField getJsonFieldAnnotation() {
		return jsonFieldAnnotation;
	}

	public Collection<DbmMappedField> getBindedFields() {
		return bindedFields;
	}

	public void setBindedFields(Collection<DbmMappedField> bindedFields) {
		this.bindedFields = bindedFields;
	}

	public DbmMappedField getBindToField() {
		return bindToField;
	}

	public void setBindToField(DbmMappedField bindToField) {
		this.bindToField = bindToField;
	}
	
}
