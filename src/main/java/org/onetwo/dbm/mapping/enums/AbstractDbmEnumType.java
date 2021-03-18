package org.onetwo.dbm.mapping.enums;

import org.onetwo.dbm.mapping.DbmEnumType;
import org.onetwo.dbm.mapping.DbmMappedField;

abstract public class AbstractDbmEnumType implements DbmEnumType {
    
    private final Class<?> javaType;

	protected AbstractDbmEnumType(Class<?> javaType) {
		this.javaType = javaType;
	}

	public Class<?> getJavaType() {
		return javaType;
	}
	
	public Object forJava(DbmMappedField field, Object value) {
		if(value==null){
			return null;
		}
		return toEnumValue(field, value);
	}
	abstract protected Enum<?> toEnumValue(DbmMappedField field, Object value);
	
	public Object forStore(DbmMappedField field, Object value) {
		if(value==null){
			return null;
		}
		return toMappingValue(field, value);
	}
	abstract protected Object toMappingValue(DbmMappedField field, Object value);
    
}
