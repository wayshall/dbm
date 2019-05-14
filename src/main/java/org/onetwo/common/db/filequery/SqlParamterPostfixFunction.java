package org.onetwo.common.db.filequery;

/***
 * 处理sql参数值的后缀函数
 * 
 * 和ParserContextFunctionSet.getInstance()内置的帮助函数集不同
 * 
 * @author way
 *
 */
public interface SqlParamterPostfixFunction {
	
	public Object toSqlParameterValue(String paramName, Object value);

}
