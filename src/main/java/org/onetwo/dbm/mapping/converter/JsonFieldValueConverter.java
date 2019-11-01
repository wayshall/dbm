package org.onetwo.dbm.mapping.converter;

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
		return getActaulJsonMapper(field).fromJson(fieldValue.toString(), field.getPropertyInfo().getType());
	}

	@Override
	public Object forStore(DbmMappedField field, Object fieldValue) {
		JsonMapper jsonMapper = getActaulJsonMapper(field);
//		field.getColumnType()
		return jsonMapper.toJson(fieldValue);
	}
	
	private JsonMapper getActaulJsonMapper(DbmMappedField field) {
		boolean typingJson = field.getJsonFieldAnnotation()!=null && field.getJsonFieldAnnotation().storeTyping();
		return typingJson?typingJsonMapper:jsonMapper;
	}

}
