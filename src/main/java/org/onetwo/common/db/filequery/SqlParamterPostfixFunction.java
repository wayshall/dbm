package org.onetwo.common.db.filequery;

import org.onetwo.common.db.filequery.SqlParamterPostfixFunctions.SqlPostfixFunctionInfo;
import org.onetwo.common.db.filequery.postfunc.SqlPostfixFunction;

/***
 * 处理sql参数值的后缀函数
 * 
 * 和ParserContextFunctionSet.getInstance()内置的帮助函数集不同
 * 
 * @author way
 *
 */
public interface SqlParamterPostfixFunction extends SqlPostfixFunction {
	
	public default Object execute(SqlPostfixFunctionInfo funcInfo, String paramName, Object value) {
		return toSqlParameterValue(paramName, value);
	}
	
	public Object toSqlParameterValue(String paramName, Object value);

}
