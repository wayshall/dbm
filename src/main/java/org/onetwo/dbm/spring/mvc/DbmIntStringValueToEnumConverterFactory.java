package org.onetwo.dbm.spring.mvc;

import org.onetwo.common.convert.Types;
import org.onetwo.common.spring.converter.ValueEnum;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.mapping.DbmEnumValueMapping;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.Assert;

import net.jodah.typetools.TypeResolver;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DbmIntStringValueToEnumConverterFactory implements ConverterFactory<String, Enum> {
	
	public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
		Class<?> enumType = targetType;
		while(enumType != null && !enumType.isEnum()) {
			enumType = enumType.getSuperclass();
		}
		Assert.notNull(enumType, "The target type " + targetType.getName()
				+ " does not refer to an enum");
		return new StringToEnum(enumType);
	}

	private class StringToEnum<T extends Enum> implements Converter<String, T> {

		private final Class<T> enumType;

		public StringToEnum(Class<T> enumType) {
			this.enumType = enumType;
		}

		public T convert(String source) {
			if (source.length() == 0) {
				return null;
			}
			if (ValueEnum.class.isAssignableFrom(enumType)) {
				Class<T> genericClass = (Class<T>)TypeResolver.resolveRawArgument(ValueEnum.class, enumType);
				T value = Types.convertValue(source, genericClass);
				return Types.asValue(value, enumType);
			} else if(DbmEnumValueMapping.class.isAssignableFrom(enumType)) {
				Class<T> genericClass = (Class<T>)TypeResolver.resolveRawArgument(DbmEnumValueMapping.class, enumType);
				Object value = Types.convertValue(source, genericClass);

				Enum<?>[] values = (Enum<?>[]) enumType.getEnumConstants();
				for (Enum<?> ev : values) {
					DbmEnumValueMapping dbmMapping = (DbmEnumValueMapping) ev;
					if (dbmMapping.getEnumMappingValue().equals(value)) {
						return (T)ev;
					}
				}
				throw new DbmException("can not convert to enum for value: " + source);
			} else{
				T enumValue = Types.convertValue(source, enumType);
				return enumValue;
			}
		}
	}

}
