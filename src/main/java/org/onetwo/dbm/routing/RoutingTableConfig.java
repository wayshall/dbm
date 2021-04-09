package org.onetwo.dbm.routing;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.Data;

/**
 * @author weishao zeng
 * <br/>
 */
@Data
public class RoutingTableConfig {
	
	/***
	 * 逻辑表名
	 */
	private String logicTableName;
	
	/****
	 * 路由策略
	 */
	private RoutingTableStrategys strategy = RoutingTableStrategys.EXPRESSION;
	
	/****
	 * 当使用自定义策略时，需要指定自定义策略类
	 */
	private String strategyClass;
	
	/***
	 * 对应策略的配置属性
	 */
	private Map<String, String> strategyConfig = Maps.newHashMap();

}
