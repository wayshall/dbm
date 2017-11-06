package org.onetwo.common.db.filequery;

public interface SqlParamterPostfixFunction {
	
	public Object toSqlParameterValue(String paramName, Object value);

}
