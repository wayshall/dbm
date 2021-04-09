package org.onetwo.dbm.routing;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.dbm.routing.strategy.ExpressionRoutingStrategy;
import org.onetwo.dbm.utils.DbmUtils;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author weishao zeng
 * <br/>
 */
public class RoutingTableRoutingService implements InitializingBean {
	
	private List<RoutingTableConfig> shardingTableConfigs = Lists.newArrayList();
	private Map<String, RoutingStrategy> shardingStrategyMap = Maps.newHashMap();

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		for (RoutingTableConfig config : this.shardingTableConfigs) {
			String logicTable = config.getLogicTableName().toLowerCase();
			RoutingStrategy strategy = null;
			switch (config.getStrategy()) {
			case CUSTOM:
				Class<? extends RoutingStrategy> strategyClass = (Class<? extends RoutingStrategy>) ReflectUtils.loadClass(config.getStrategyClass());
				strategy = DbmUtils.createDbmBean(strategyClass);
				break;

			default:
				strategy = new ExpressionRoutingStrategy();
				break;
			}
			this.shardingStrategyMap.put(logicTable, strategy);
		}
	}
	
	/****
	 * 若逻辑表需要分表，则返回要路由到到目标表名；
	 * 否则返回空
	 * @author weishao zeng
	 * @param context
	 * @return
	 */
	public Optional<String> routingTable(RoutingSqlContext context) {
		String logicTable = context.getLogicTableName();
		if (!shardingStrategyMap.containsKey(logicTable.toLowerCase())) {
			return Optional.empty();
		}
		
		RoutingStrategy shardingStrategy = shardingStrategyMap.get(logicTable);
		String targetRoutingTable = shardingStrategy.routingTable();
		return Optional.of(targetRoutingTable);
	}

	public void setShardingTableConfigs(List<RoutingTableConfig> shardingTableConfigs) {
		this.shardingTableConfigs = shardingTableConfigs;
	}
	
	/***
	 * 是否有分表配置
	 * @author weishao zeng
	 * @return
	 */
	public boolean hasShardingConfigs() {
		return this.shardingStrategyMap.size()>0;
	}

}
