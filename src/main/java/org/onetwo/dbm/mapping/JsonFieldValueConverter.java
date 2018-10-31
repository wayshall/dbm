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
	
	private JsonMapper jsonMapper = JsonMapper.ignoreNull().enableTyping();
	
	public JsonFieldValueConverter() {
		super();
	}

	@Override
	public Object forJava(DbmMappedField field, Object fieldValue) {
		return jsonMapper.fromJson(fieldValue.toString(), field.getPropertyInfo().getType());
	}

	@Override
	public Object forStore(DbmMappedField field, Object fieldValue) {
		return jsonMapper.toJson(fieldValue);
	}

}
