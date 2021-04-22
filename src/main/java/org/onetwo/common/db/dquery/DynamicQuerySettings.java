package org.onetwo.common.db.dquery;

import java.util.List;
import java.util.Map;

import org.onetwo.common.db.dquery.condition.DynamicFieldCondition;

/**
 * @author weishao zeng
 * <br/>
 */

public interface DynamicQuerySettings {
	
	Class<?> getResultType();
	
	Class<?> getRowType();
	
	Map<Object, Object> getQueryParseContext();

	List<DynamicFieldCondition> getDynamicFields();
	
}
