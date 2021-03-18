package org.onetwo.dbm.mapping.enums;

import java.util.stream.Stream;

import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.mapping.DbmEnumValueMapping;
import org.onetwo.dbm.mapping.DbmMappedField;

import com.google.common.primitives.Primitives;

/**
 * @author weishao zeng
 * <br/>
 */

public class DbmMappingEnumType extends AbstractDbmEnumType {

	public DbmMappingEnumType(Class<?> javaType) {
		super(javaType);
	}

	@Override
	protected Enum<?> toEnumValue(DbmMappedField field, Object value) {
		if (value==null) {
			return null;
		}
		
		Class<?> wrapClass = null;
		if (getJavaType().isPrimitive()) {
			// 如果是简单类型，则获取包装类型
			wrapClass = Primitives.wrap(getJavaType());
		} else {
			// 如果是包装类型，则获取简单类型
			wrapClass = Primitives.unwrap(getJavaType());
		}
		if (!value.getClass().equals(this.getJavaType()) && !wrapClass.equals(value.getClass())) {
			throw new DbmException("the enum mapping is not supported the value type: " + value.getClass());
		}
		DbmEnumValueMapping<?>[] values = (DbmEnumValueMapping[]) field.getPropertyInfo().getType().getEnumConstants();
		DbmEnumValueMapping<?> valueMapping = Stream.of(values)
//												.filter(dvm->Integer.valueOf(dvm.getMappingValue()).equals(value))
												.filter(dvm->dvm.getEnumMappingValue().equals(value))
												.findFirst()
												.orElseThrow(()-> {
													return new DbmException("error enum mapping value: " + value + ", type: " + value.getClass());
												});
		return (Enum<?>) valueMapping;
	}

	@Override
	protected Object toMappingValue(DbmMappedField field, Object value) {
		DbmEnumValueMapping<?> enumValue = (DbmEnumValueMapping<?>) value;
		return enumValue.getEnumMappingValue();
	}
}
