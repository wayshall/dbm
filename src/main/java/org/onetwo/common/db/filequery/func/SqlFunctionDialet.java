package org.onetwo.common.db.filequery.func;

/***
 * 注册自定义函数，生成的对应的数据库函数
 * 可用于统一sql函数写法
 * @author way
 *
 */
public interface SqlFunctionDialet {
	
	SQLFunctionGenerator get(String name);

}
