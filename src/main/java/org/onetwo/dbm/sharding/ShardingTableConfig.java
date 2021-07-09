package org.onetwo.dbm.sharding;

import lombok.Data;

/**
dbm:
	shardingTables:
		- logicTableName: user
		  strategy: expression
		  expressionStrategy:
		   	routingColumn: id
		   	physicalTableExpression: user_${id%4}
 * @author weishao zeng
 * <br/>
 */
@Data
public class ShardingTableConfig {
	/***
	 * 逻辑表名
	 */
	String logicTableName;
	ShardingTableStrategies strategy = ShardingTableStrategies.EXPRESSION;
	ExpressionStrategy expressionStrategy = new ExpressionStrategy();
	
	@Data
	public static class ExpressionStrategy {
		/***
		 * 路由字段
		 */
		String routingColumn;
		/***
		 * 要路由到的物理表的表达式
		 */
		String physicalTableExpression;
	}
	
}
