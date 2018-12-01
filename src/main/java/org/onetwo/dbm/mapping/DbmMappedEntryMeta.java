package org.onetwo.dbm.mapping;

import java.util.Collection;
import java.util.List;

import org.onetwo.common.annotation.AnnotationInfo;

public interface DbmMappedEntryMeta {

	Collection<AbstractMappedField> getFields();
	Collection<AbstractMappedField> getFields(DbmMappedFieldType... type);
	
	DbmMappedField getField(String fieldName);
	
	AnnotationInfo getAnnotationInfo();

	boolean contains(String field);

	boolean containsColumn(String col);


	DbmMappedField getFieldByColumnName(String columnName);


	DbmMappedEntryMeta addMappedField(AbstractMappedField field);

	Class<?> getEntityClass();
	
	String getEntityName();

	TableInfo getTableInfo();

	List<DbmMappedField> getIdentifyFields();
	
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