package org.onetwo.dbm.core.internal;

public enum SessionTransactionType {
	/****
	 * 使用容器（spring）管理的事务（创建、提交、回滚）
	 */
	CONTEXT_MANAGED,
	/****
	 * dbm会自动为所有标记了注解 @DbmJdbcOperationMark 的方法创建事务
	 */
	PROXY,
	/****
	 * 手动管理事务
	 * 即显式调用  sessionFactory#openSession 打开会话后，通过 session.beginTransaction 创建事务（DbmTransaction）
	 * 并通过事务api提交或者回滚事务
	 */
	MANUAL

}
