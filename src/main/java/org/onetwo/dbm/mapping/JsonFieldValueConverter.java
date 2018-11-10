package org.onetwo.dbm.mapping;

import org.onetwo.common.jackson.JsonMapper;

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
		return getActaulJsonMapper(field).toJson(fieldValue);
	}
	
	private JsonMapper getActaulJsonMapper(DbmMappedField field) {
		boolean typingJson = field.getJsonFieldAnnotation()!=null && field.getJsonFieldAnnotation().storeTyping();
		return typingJson?typingJsonMapper:jsonMapper;
	}

}
