package org.onetwo.common.db.filequery.func;

/***
 * 统一sql函数写法
 * @author way
 *
 */
public interface SqlFunctionDialet {
	
	SQLFunctionGenerator get(String name);

}
