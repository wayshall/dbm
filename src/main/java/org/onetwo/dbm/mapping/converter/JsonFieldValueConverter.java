package org.onetwo.dbm.mapping.converter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

	@Override
	public Object forJava(DbmMappedField field, Object fieldValue) {
		String json = (String)fieldValue;
		if (StringUtils.isBlank(json)) {
			return null;
		}
		
		boolean typingJson = field.getJsonFieldAnnotation()!=null && field.getJsonFieldAnnotation().storeTyping();
		boolean smartyParse = field.getJsonFieldAnnotation().smartyParse();
		JsonMapper jsonMapper = null;
		if (typingJson) {
			jsonMapper = typingJsonMapper;
			if (smartyParse) {
				if (!json.contains("@class")) {
					jsonMapper = this.jsonMapper;
				}
			}
		} else {
			jsonMapper = this.jsonMapper;
			if (smartyParse) {
				if (json.contains("@class")) {
					jsonMapper = this.typingJsonMapper;
				}
			}
		}
		Class<?> propertyType = field.getPropertyInfo().getType();
		if (List.class.isAssignableFrom(propertyType)) {
//			ReflectUtils.get
			Class<?> valueType = field.getJsonFieldAnnotation().valueType();
			if (!valueType.equals(void.class)) {
				Object value = jsonMapper.fromJsonAsList((String)fieldValue, valueType);
				return value;
			} else {
				valueType = (Class<?>)field.getPropertyInfo().getParameterType(0);
				if (valueType!=null) {
					Object value = jsonMapper.fromJsonAsList((String)fieldValue, valueType);
					return value;
				}
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
