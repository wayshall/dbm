package org.onetwo.dbm.mapping;

import java.util.stream.Stream;

import org.onetwo.common.convert.Types;
import org.onetwo.dbm.exception.DbmException;

public enum DbmEnumType {
    ORDINAL(int.class) {
		@Override
		protected Enum<?> toEnumValue(DbmMappedField field, Object value) {
			Enum<?> actualValue = (Enum<?>)Types.convertValue((Integer)value, field.getPropertyInfo().getType());
			return actualValue;
		}

		@Override
		protected Object toMappingValue(DbmMappedField field, Object value) {
			Enum<?> enumValue = (Enum<?>) value;
			return enumValue.ordinal();
		}
    },
    
    MAPPING(int.class){
		@Override
		protected Enum<?> toEnumValue(DbmMappedField field, Object value) {
			DbmEnumValueMapping<?>[] values = (DbmEnumValueMapping[]) field.getPropertyInfo().getType().getEnumConstants();
			DbmEnumValueMapping<?> valueMapping = Stream.of(values)
//													.filter(dvm->Integer.valueOf(dvm.getMappingValue()).equals(value))
													.filter(dvm->dvm.getEnumMappingValue().equals(value))
													.findFirst()
													.orElseThrow(()-> {
														return new DbmException("error enum mapping value: " + value);
													});
			return (Enum<?>) valueMapping;
		}

		@Override
		protected Object toMappingValue(DbmMappedField field, Object value) {
			DbmEnumValueMapping<?> enumValue = (DbmEnumValueMapping<?>) value;
			return enumValue.getEnumMappingValue();
		}
    },
    STRING(String.class){
		@Override
		protected Enum<?> toEnumValue(DbmMappedField field, Object value) {
			Enum<?> actualValue = (Enum<?>)Types.convertValue(value.toString(), field.getPropertyInfo().getType());
			return actualValue;
		}

		@Override
		protected Object toMappingValue(DbmMappedField field, Object value) {
			Enum<?> enumValue = (Enum<?>) value;
			return enumValue.name();
		}
    };
    
    private final Class<?> javaType;

	private DbmEnumType(Class<?> javaType) {
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
