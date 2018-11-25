package org.onetwo.dbm.mapping;

import java.util.List;

import org.onetwo.common.utils.JFishProperty;
import org.onetwo.dbm.annotation.DbmJsonField;
import org.onetwo.dbm.event.spi.DbmEventAction;
import org.onetwo.dbm.id.IdentifierGenerator;
import org.onetwo.dbm.id.StrategyType;
import org.onetwo.dbm.jpa.GeneratedValueIAttrs;
import org.onetwo.dbm.mapping.version.VersionableType;

public interface DbmMappedField {

	IdentifierGenerator<?> getIdGenerator();
	GeneratedValueIAttrs getGeneratedValueIAttrs();
	void addIdGenerator(IdentifierGenerator<?> idGenerator);
	void setGeneratedValueIAttrs(GeneratedValueIAttrs generatedValueIAttrs);
	
	void setValue(Object entity, Object value);

	Object getValue(Object entity);

//	void setValueFromJdbc(Object entity, Object value);

//	Object getValueForJdbc(Object entity);
	
	List<DbmEntityFieldListener> getFieldListeners();

	boolean isIdentify();

	void setIdentify(boolean identify);

	BaseColumnInfo getColumn();

	void setColumn(BaseColumnInfo column);

	String getName();

	DbmMappedEntry getEntry();

	String getLabel();

	void setLabel(String label);

	/********
	 * 自动生成值是否需要在插入之前fetch数据，一般就是oracle序列
	 * @return
	 */
//	boolean isGeneratedValueFetchBeforeInsert();

	boolean isGeneratedValue();

	boolean isSeqStrategy();

	boolean isIdentityStrategy();

	StrategyType getStrategyType();

	void setStrategyType(StrategyType strategyType);

	JFishProperty getPropertyInfo();
	
	/***
	 * 获取实际映射到类型
	 * @return
	 */
	Class<?> getColumnType();

	void freezing();

	void checkFreezing(String name);

	boolean isFreezing();

	DbmMappedFieldType getMappedFieldType();

	void setMappedFieldType(DbmMappedFieldType mappedFieldType);

	boolean isJoinTableField();

//	DataHolder<String, Object> getDataHolder();
	
//	SqlParameterValue getValueForJdbcAndFireDbmEventAction(Object entity, JFishEventAction eventAction);
	/***
	 * 
	 * @param fieldValue
	 * @param eventAction
	 * @return newFieldValue
	 */
	Object fireDbmEntityFieldEvents(Object fieldValue, DbmEventAction eventAction);

	boolean isVersionControll();
	<T> VersionableType<T> getVersionableType();
	void setVersionableType(VersionableType<?> versionableType);
	
	boolean isEnumerated();
	DbmEnumType getEnumType();
//	GeneratedValueIAttrs getGeneratedValueIAttrs();
	
	DbmFieldValueConverter getFieldValueConverter();
	
	DbmJsonField getJsonFieldAnnotation();
	
	boolean isMappingGenerated();
	
}