package org.onetwo.dbm.mapping;

import java.util.Collection;
import java.util.List;

import org.onetwo.common.annotation.AnnotationInfo;

public interface DbmMappedEntryMeta {

	Collection<DbmMappedField> getFields();
	Collection<DbmMappedField> getFields(DbmMappedFieldType... type);
	
	DbmMappedField getField(String fieldName);
	
	AnnotationInfo getAnnotationInfo();

	boolean contains(String field);

	boolean containsColumn(String col);


	DbmMappedField getFieldByColumnName(String columnName);
	
	/****
	 * 通过@DbmBindValueToField 注解绑定到此字段的其它字段
	 * @author weishao zeng
	 * @param fieldName
	 * @return
	 */
	Collection<DbmMappedField> getBindedFieldsByFieldName(String fieldName);


	DbmMappedEntryMeta addMappedField(AbstractMappedField field);

	Class<?> getEntityClass();
	
	String getEntityName();

	TableInfo getTableInfo();

	List<DbmMappedField> getIdentifyFields();
	
	/****
	 * 是否id自增策略
	 * @return
	 */
	default boolean hasIdentityStrategyField() {
		return getIdentifyFields().stream().anyMatch(field -> field.isIdentityStrategy());
	}
	
	default boolean hasGeneratedValueIdField() {
		return getIdentifyFields().stream().anyMatch(field -> field.isGeneratedValue());
	}
	
	MappedType getMappedType();

//	boolean isJoined();
	boolean isEntity();
	
	boolean isInstance(Object entity);

	DbmMappedField getVersionField();
	Object getVersionValue(Object entity);
	
	boolean isVersionControll();
	
	Class<?> getIdClass();
	/***
	 * 是否复合主键
	 * @author weishao zeng
	 * @return
	 */
	boolean isCompositePK();
	
}