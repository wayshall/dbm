package org.onetwo.dbm.id;
/**
 * @author wayshall
 * <br/>
 */
public class TableGeneratorAttrs {

	final private String name;
	final private int allocationSize;
	final private String table;
	//列1，varchar 类型，存储生成ID的键
	final private String pkColumnName;  
	// 列2，int 类型，存储ID值
	final private String valueColumnName;
	//列1的键值
	final private String pkColumnValue;
	
	public TableGeneratorAttrs(String name, int allocationSize, String table,
			String pkColumnName, String valueColumnName, String pkColumnValue) {
		super();
		this.name = name;
		this.allocationSize = allocationSize;
		this.table = table;
		this.pkColumnName = pkColumnName;
		this.valueColumnName = valueColumnName;
		this.pkColumnValue = pkColumnValue;
	}

	public String getName() {
		return name;
	}

	public int getAllocationSize() {
		return allocationSize;
	}

	public String getTable() {
		return table;
	}

	public String getPkColumnName() {
		return pkColumnName;
	}

	public String getValueColumnName() {
		return valueColumnName;
	}

	public String getPkColumnValue() {
		return pkColumnValue;
	}  

}
