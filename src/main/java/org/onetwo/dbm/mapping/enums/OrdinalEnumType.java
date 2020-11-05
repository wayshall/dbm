package org.onetwo.dbm.mapping.enums;

import org.onetwo.common.convert.Types;
import org.onetwo.dbm.mapping.DbmMappedField;

/**
 * @author weishao zeng
 * <br/>
 */

public class OrdinalEnumType extends AbstractDbmEnumType {

	public OrdinalEnumType() {
		super(Integer.class);
	}

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

}
