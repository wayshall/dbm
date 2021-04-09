package org.onetwo.dbm.routing;

import java.util.List;

import com.alibaba.druid.stat.TableStat.Condition;

import lombok.Data;

/**
 * @author weishao zeng
 * <br/>
 */
@Data
public class RoutingSqlContext {
	
	private List<String> tables;
	private List<Condition> conditions;

}
