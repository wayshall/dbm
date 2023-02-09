package org.onetwo.common.db.sql;
/**
 * @author weishao zeng
 * <br/>
 */

public class SelectItemInfo {

	private String name;
	private String alias;
	public SelectItemInfo(String name, String alias) {
		super();
		this.name = name;
		this.alias = alias;
	}
	public String getName() {
		return name;
	}
	public String getAlias() {
		return alias;
	}
	@Override
	public String toString() {
		return "[name=" + name + ", alias=" + alias + "]";
	}
	
}
