package org.onetwo.common.db.spi;

import org.onetwo.common.db.filequery.postfunc.SqlPostfixFunction;

/***
 * 
 * 处理sql参数值的后缀函数
 * 
 * 和ParserContextFunctionSet.getInstance()内置的帮助函数集不同
 * 
 * @author way
 *
 */
public interface SqlParamterPostfixFunctionRegistry {

	String getFuncPostfixMark();
//	@Deprecated
//	SqlParamterPostfixFunction getFunc(String postfix);
	Object executeFunc(String property, Object value, String postfix);
//	SqlParamterPostfixFunction getFunc(Object value, String postfix);
	
	SqlParamterPostfixFunctionRegistry register(String postfix, SqlPostfixFunction func);

}