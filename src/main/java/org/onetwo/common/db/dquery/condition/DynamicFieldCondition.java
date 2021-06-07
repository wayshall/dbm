package org.onetwo.common.db.dquery.condition;

import org.onetwo.common.db.sqlext.QueryDSLOps;

/**
 * @author weishao zeng
 * <br/>
 */

public class DynamicFieldCondition {
	/***
	 * sql字段名，如：u.user_name
	 */
	private String fieldName;
	private QueryDSLOps operator;
	/***
	 * 查询值的参数名，如：u.user_name = :userName
	 */
	private String parameterName;
	private Object value;
	
	public DynamicFieldCondition(String fieldName, QueryDSLOps operator, String parameterName) {
		super();
		this.fieldName = fieldName;
		this.parameterName = parameterName;
		this.operator = operator;
	}
	public String getFieldName() {
		return fieldName;
	}
	public String getParameterName() {
		return parameterName;
	}
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	public QueryDSLOps getOperator() {
		return operator;
	}
	public void setOperator(QueryDSLOps operator) {
		this.operator = operator;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
}
