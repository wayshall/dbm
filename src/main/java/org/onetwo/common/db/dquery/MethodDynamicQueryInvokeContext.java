package org.onetwo.common.db.dquery;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.onetwo.common.db.dquery.annotation.BatchObject;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.db.spi.SqlTemplateParser;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.utils.LangUtils;
import org.springframework.util.Assert;

public class MethodDynamicQueryInvokeContext implements NamedQueryInvokeContext {

	final private DynamicMethod dynamicMethod;
	final private Object[] parameterValues;
	final private Map<Object, Object> parsedParams;
//	private TemplateParser parser;
	final private QueryProvideManager queryProvideManager;
	private NamedQueryInfo namedQueryInfo;
	
	public MethodDynamicQueryInvokeContext(QueryProvideManager manager, DynamicMethod dynamicMethod, Object[] parameterValues) {
		super();
		this.dynamicMethod = dynamicMethod;
		this.parameterValues = parameterValues;
		//ImmutableMap.copyOf key和value不能为null
//		this.parsedParams = ImmutableMap.copyOf(dynamicMethod.toMapByArgs(parameterValues));
		Map<Object, Object> methodParams = dynamicMethod.toMapByArgs(parameterValues);
		this.parsedParams = Collections.unmodifiableMap(methodParams);
		this.queryProvideManager = manager;
	}

	public NamedQueryInfo getNamedQueryInfo() {
		return namedQueryInfo;
	}
	
	public SqlTemplateParser getDynamicSqlTemplateParser() {
		SqlTemplateParser parser = dynamicMethod.getDynamicSqlTemplateParser();
		if (parser==null) {
			parser = queryProvideManager.getFileNamedQueryManager().getNamedSqlFileManager().getSqlStatmentParser();
		}
		return parser;
	}

	void setNamedQueryInfo(NamedQueryInfo namedQueryInfo) {
		this.namedQueryInfo = namedQueryInfo;
	}

	@Override
	public QueryProvideManager getQueryProvideManager() {
		return queryProvideManager;
	}

	public String getQueryName() {
		Object dispatcher = getQueryMatcherValue();
		String queryName = dynamicMethod.getQueryName(parameterValues);
		if(dispatcher!=null){
			Assert.notNull(dispatcher, "dispatcher can not be null!");
			return queryName + "(" + dispatcher+")";
		}
		return queryName;
	}
	
	/*public boolean matcher(JFishNamedFileQueryInfo queryInfo){
		if(queryInfo.getMatchers().isEmpty()){
			return !dynamicMethod.hasMatcher();
		}
		return queryInfo.getMatchers().contains(getQueryMatcherValue());
	}*/
	
	private Object getQueryMatcherValue(){
		return dynamicMethod.getMatcherValue(parameterValues);
	}
	
	public Collection<?> getBatchParameter(){
		Collection<?> batchParameter = (Collection<?>)parsedParams.get(BatchObject.class);
		
		if(batchParameter==null){
			if(LangUtils.size(parameterValues)!=1 || !Collection.class.isInstance(parameterValues[0])){
				throw new BaseException("BatchObject not found, the batch method parameter only supported one parameter and must a Collection : " + dynamicMethod.getMethod().toGenericString());
			}
			batchParameter = (Collection<?>)parameterValues[0];
		}
		return batchParameter;
	}
	
	
	public DynamicMethod getDynamicMethod() {
		return dynamicMethod;
	}

	public Object[] getParameterValues() {
		return parameterValues;
	}

	public Map<Object, Object> getParsedParams() {
		return parsedParams;
	}

}
