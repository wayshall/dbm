package org.onetwo.common.db.sqlext;

import java.util.Map;

public interface SelectExtQuery extends ExtQueryInner {

	public boolean needSetRange();

	public Integer getFirstResult();

	public Integer getMaxResults();
	
	public void setMaxResults(Integer maxResults);
	
//	public boolean isIgnoreQuery();
	public Map<Object, Object> getQueryConfig();
	public String getCountSql();
	

	public boolean isSubQuery();

	public void setSubQuery(boolean subQuery);
	
	public boolean isCacheable();
	
}
