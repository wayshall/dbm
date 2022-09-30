package org.onetwo.common.db.generator;

import java.util.HashMap;
import java.util.Map;

import org.onetwo.common.db.generator.DbGenerator.DbTableGenerator.TableGeneratedConfig;
import org.onetwo.common.db.generator.meta.TableMeta;
import org.onetwo.common.utils.StringUtils;

import com.google.common.collect.Maps;

import lombok.EqualsAndHashCode;

/**
 * @author weishao zeng
 * <br/>
 */

@SuppressWarnings({ "unchecked", "serial" })
@EqualsAndHashCode(callSuper=false)
public class GeneratedContext extends HashMap<String, Object> {
	private static final String TABLE_CONTEXT_KEY = "_tableContext";
//	private static final String TABLE_KEY = "table";
	private TableMeta table;
	private TableGeneratedConfig config;
	
	/****
	 * 主要为了兼容以前的模板
	 * @author weishao zeng
	 */
	public void initBasicContext() {
		String tableNameWithoutPrefix = config.getTableNameWithoutPrefix();
		String className = config.getClassName();
		String propertyName = config.getPropertyName();
		
		Map<String, Object> tableContext = Maps.newHashMap();
		tableContext.put("tableNameWithoutPrefix", tableNameWithoutPrefix);
		tableContext.put("shortTableName", tableNameWithoutPrefix);
		tableContext.put("className", className);
		tableContext.put("propertyName", propertyName);
		String localPackage = StringUtils.emptyIfNull(config.getLocalPackage());
		tableContext.put("localPackage", localPackage);
		tableContext.put("localFullPackage", config.globalGeneratedConfig().getJavaLocalPackage(localPackage));
		
		setTableContext(tableContext);
		put("table", table);
		put("config", config);
	}
	
	public void setTableContext(Map<String, Object> tableContext) {
		put(TABLE_CONTEXT_KEY, tableContext);
	}
	
	public Map<String, Object> getTableContext() {
		return (Map<String, Object>)get(TABLE_CONTEXT_KEY);
	}

	public TableMeta getTable() {
		return table;
	}

	public void setTable(TableMeta table) {
		this.table = table;
	}

	public TableGeneratedConfig getConfig() {
		return config;
	}

	public void setConfig(TableGeneratedConfig config) {
		this.config = config;
	}
}
