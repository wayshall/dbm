package org.onetwo.common.db.dquery;

import java.util.Map;

import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.db.spi.SqlTemplateParser;

public interface NamedQueryInvokeContext {

	String getQueryName();
	
	Map<Object, Object> getParsedParams();
	
	DynamicMethod getDynamicMethod();
	
	QueryProvideManager getQueryProvideManager();
	
	NamedQueryInfo getNamedQueryInfo();
	
	/***
	 * 获取自定义动态sql模板解释器
	 * @author weishao zeng
	 * @return
	 */
	SqlTemplateParser getDynamicSqlTemplateParser();
	
}