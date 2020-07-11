package org.onetwo.dbm.mapping.converter;

import java.util.Collection;

import org.onetwo.common.jackson.JsonMapper;
import org.onetwo.dbm.mapping.DbmFieldValueConverter;
import org.onetwo.dbm.mapping.DbmMappedField;

/**
 * @author wayshall
 * <br/>
 */
public class JsonFieldValueConverter implements DbmFieldValueConverter {
	
	/*private static final JsonFieldValueConverter INSTANCE = new JsonFieldValueConverter();
	
	public static DbmFieldValueConverter getInstance(){
		return INSTANCE;
	}*/

	private JsonMapper jsonMapper = JsonMapper.ignoreNull();
	private JsonMapper typingJsonMapper = JsonMapper.ignoreNull().enableTyping();
	
	public JsonFieldValueConverter() {
		super();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object forJava(DbmMappedField field, Object fieldValue) {
		Class<?> propertyType = field.getPropertyInfo().getType();
		if (Collection.class.isAssignableFrom(propertyType)) {
//			ReflectUtils.get
			Class<?> valueType = field.getJsonFieldAnnotation().valueType();
			if (!valueType.equals(void.class)) {
				Object value = getActaulJsonMapper(field).fromJsonAsCollection((String)fieldValue, (Class<Collection>)propertyType, valueType);
				return value;
			}
		}
		return getActaulJsonMapper(field).fromJson(fieldValue, propertyType);
	}

	@Override
	public Object forStore(DbmMappedField field, Object fieldValue) {
		JsonMapper jsonMapper = getActaulJsonMapper(field);
//		field.getColumnType()
		Object value = null;
		if (byte[].class==field.getColumnType()) {
			value = jsonMapper.toJsonBytes(fieldValue);
		} else {
			value = jsonMapper.toJson(fieldValue);
		}
		return value;
	}
	
	private JsonMapper getActaulJsonMapper(DbmMappedField field) {
		boolean typingJson = field.getJsonFieldAnnotation()!=null && field.getJsonFieldAnnotation().storeTyping();
		return typingJson?typingJsonMapper:jsonMapper;
	}

}
