package org.onetwo.dbm.id;

import org.onetwo.common.utils.StringUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.Assert;

/**
 * @author wayshall
 * <br/>
 */
public class TableGeneratorAttrs {

	final private String name;
	/***
	 * 批量分配时， 每次分配id的数量
	 */
	final private int allocationSize;
	private String table = "gen_ids";
	//列1，varchar 类型，存储生成ID的键
	private String pkColumnName = "gen_name";  
	// 列2，int 类型，存储ID值
	private String valueColumnName = "gen_value";
	//列1的键值
	final private String pkColumnValue;
	
	final private int initialValue;
	/***
	 * 默认使用 PROPAGATION_REQUIRES_NEW 
	 */
	private int transactionPropagation = TransactionDefinition.PROPAGATION_REQUIRES_NEW;
	
	
	public TableGeneratorAttrs(String name, String pkColumnValue, int allocationSize) {
		this(name, allocationSize, "gen_ids", "gen_name", "gen_value", pkColumnValue, 1);
	}
	
	public TableGeneratorAttrs(String name, int allocationSize, String table,
			String pkColumnName, String valueColumnName, String pkColumnValue,
			int initialValue) {
		super();
		this.name = name;
		this.allocationSize = allocationSize;
		if (StringUtils.isNotBlank(table)) {
			this.table = table;
		} else {
			Assert.hasText(table, "table can not be blank!");
		}
		if (StringUtils.isNotBlank(pkColumnName)) {
			this.pkColumnName = pkColumnName;
		} else {
			Assert.hasText(pkColumnName, "pkColumnName can not be blank!");
		}
		if (StringUtils.isNotBlank(valueColumnName)) {
			this.valueColumnName = valueColumnName;
		} else {
			Assert.hasText(valueColumnName, "valueColumnName can not be blank!");
		}
		this.pkColumnValue = pkColumnValue;
		this.initialValue = initialValue;
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

	public int getInitialValue() {
		return initialValue;
	}

	public int getTransactionPropagation() {
		return transactionPropagation;
	}

	public void setTransactionPropagation(int transactionPropagation) {
		this.transactionPropagation = transactionPropagation;
	}  

}
