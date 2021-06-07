package org.onetwo.common.db.dquery;

import java.util.List;
import java.util.Map;

import org.onetwo.common.db.dquery.condition.DynamicFieldCondition;

/**
 * @author weishao zeng
 * <br/>
 */
public class DefaultDynamicQuerySettings implements DynamicQuerySettings {
	
	private Class<?> resultType;
	private Class<?> rowType;
	private Map<Object, Object> queryParseContext;
	
	private List<DynamicFieldCondition> dynamicFields;
	
	public Class<?> getResultType() {
		return resultType;
	}
	public void setResultType(Class<?> resultType) {
		this.resultType = resultType;
	}
	public Class<?> getRowType() {
		return rowType;
	}
	public void setRowType(Class<?> rowType) {
		this.rowType = rowType;
	}
	public Map<Object, Object> getQueryParseContext() {
		return queryParseContext;
	}
	public void setQueryParseContext(Map<Object, Object> queryParseContext) {
		this.queryParseContext = queryParseContext;
	}
	public List<DynamicFieldCondition> getDynamicFields() {
		return dynamicFields;
	}
	public void setDynamicFields(List<DynamicFieldCondition> dynamicFields) {
		this.dynamicFields = dynamicFields;
	}

}
