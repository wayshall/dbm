package org.onetwo.common.db.dquery;

import java.util.Map;

import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.QueryProvideManager;

public interface NamedQueryInvokeContext {

	public String getQueryName();
	
	public Map<Object, Object> getParsedParams();
	
	public DynamicMethod getDynamicMethod();
	
	public QueryProvideManager getQueryProvideManager();
	
	public NamedQueryInfo getNamedQueryInfo();
	
}