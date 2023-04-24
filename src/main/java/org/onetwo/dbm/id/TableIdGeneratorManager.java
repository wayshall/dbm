package org.onetwo.dbm.id;

public interface TableIdGeneratorManager {

	/***
	 * 根据序列名称，生成下一个id
	 * @param idGeneratorName
	 * @return
	 */
	Long nextId(String idGeneratorName);
	
	String nextId(String idGeneratorName, String idExprTemplate, Object... vars);
	
	Long nextId(Class<?> entityClass);
	
	IdentifierGenerator<?> getIdentifierGenerator(Class<?> entityClass, String idGeneratorName);
	
}
