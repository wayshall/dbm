package org.onetwo.common.db.builder;

import org.onetwo.common.db.sqlext.ExtQueryInner;

public interface QueryField {
	public static final char SPLIT_SYMBOL = ':';
	
	public void init(ExtQueryInner extQuery, Object value);
	
	public String getActualFieldName();
	
	public String getOperator();
	

	public ExtQueryInner getExtQuery();

	public Object getValue();

	public String getFieldName();
	/***
	 * 可用querys.when代替
	 * 单独控制每个条件是否忽略null，占坑
	 * @author weishao zeng
	 * @return
	 
	public default IfNull getIfNull() {
		return null;
	}*/
}
