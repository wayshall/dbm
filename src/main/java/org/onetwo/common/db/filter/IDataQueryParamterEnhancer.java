package org.onetwo.common.db.filter;

import java.util.Map;

import org.onetwo.common.db.sqlext.ExtQuery;

public interface IDataQueryParamterEnhancer {
	
	public Map<Object, Object> enhanceParameters(ExtQuery query);

}
