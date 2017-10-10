package org.onetwo.dbm.mapping;

import org.onetwo.common.jackson.JsonMapper;

/**
 * @author wayshall
 * <br/>
 */
public class JsonFieldValueConverter implements DbmFieldValueConverter {
	
	public static final JsonFieldValueConverter INSTANCE = new JsonFieldValueConverter();
	
	private JsonMapper jsonMapper = JsonMapper.ignoreNull();
	
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
