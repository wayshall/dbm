package org.onetwo.common.db.filequery.postfunc;

import org.onetwo.common.db.filequery.postfunc.SqlParamterPostfixFunctions.SqlPostfixFunctionInfo;

/***
 * 处理sql参数值的后缀函数
 * 
 * 和ParserContextFunctionSet.getInstance()内置的帮助函数集不同
 * 
 * @author way
 *
 */
public interface SqlPostfixFunction {
	
	Object execute(SqlPostfixFunctionInfo funcInfo, String paramName, Object value);
	

}
