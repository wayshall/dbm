package org.onetwo.common.db.filter;

import java.util.Map;

import org.onetwo.common.db.sqlext.ExtQuery;

public interface IDataQueryParamterEnhancer {
	
	/****
	 * 返回的map会追加到条件查询，如果原来的query查询条件里已存在，则忽略
	 * @author wayshall
	 * @param query
	 * @return
	 */
	public Map<Object, Object> enhanceParameters(ExtQuery query);

}
