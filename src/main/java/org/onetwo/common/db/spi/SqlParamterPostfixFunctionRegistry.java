package org.onetwo.common.db.spi;

import org.onetwo.common.db.filequery.SqlParamterPostfixFunction;

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
	SqlParamterPostfixFunction getFunc(String postfix);

}