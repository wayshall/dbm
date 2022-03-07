package org.onetwo.common.db.dquery;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.onetwo.common.db.dquery.annotation.BatchObject;
import org.onetwo.common.db.filequery.ParserContext;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.db.spi.SqlTemplateParser;
import org.onetwo.common.db.spi.SqlTemplateParserAware;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.utils.LangUtils;

import com.google.common.collect.Sets;

public class MethodDynamicQueryInvokeContext implements NamedQueryInvokeContext {

	/***
	 * 特殊的context变量前缀，若有此前缀，还会作为执行sql的参数对象params
	 */
	public static final String SPECIAL_CONTEXT_PREFIX = ":";
	
	final private DynamicMethod dynamicMethod;
	final private Object[] parameterValues;
	final private Map<Object, Object> parsedParams;
//	private TemplateParser parser;
	final private QueryProvideManager queryProvideManager;
	private NamedQueryInfo namedQueryInfo;
	private ParserContext parserContext;
	
	public MethodDynamicQueryInvokeContext(QueryProvideManager manager, DynamicMethod dynamicMethod, Object[] parameterValues, NamedQueryInfo namedQueryInfo) {
		super();
		this.dynamicMethod = dynamicMethod;
		this.parameterValues = parameterValues;
		this.namedQueryInfo = namedQueryInfo;
		//ImmutableMap.copyOf key和value不能为null
//		this.parsedParams = ImmutableMap.copyOf(dynamicMethod.toMapByArgs(parameterValues));
		Map<Object, Object> methodParams = dynamicMethod.toMapByArgs(parameterValues);
		this.queryProvideManager = manager;
		this.parserContext = ParserContext.create(namedQueryInfo);
		
		Set<String> vars = Sets.newHashSet(dynamicMethod.getSqlParameterVars());
		// 处理特殊前缀的变量
		Map<?, ?> ctx = dynamicMethod.getQueryParseContext(this.parameterValues);
		if (ctx!=null) {
			for (Entry<?, ?> entry : ctx.entrySet()) {
				if (entry.getKey() instanceof String) {
					String key = entry.getKey().toString();
					if (key.startsWith(SPECIAL_CONTEXT_PREFIX)) {
						key = key.substring(SPECIAL_CONTEXT_PREFIX.length());
						methodParams.put(key, entry.getValue());
					} else if (vars.contains(key)) {
						methodParams.put(key, entry.getValue());
					}
					this.parserContext.put(key, entry.getValue());
				} else {
					this.parserContext.put(entry.getKey(), entry.getValue());
				}
			}
		}
		this.parsedParams = Collections.unmodifiableMap(methodParams);
	}

	public NamedQueryInfo getNamedQueryInfo() {
		return namedQueryInfo;
	}
	
	public SqlTemplateParser getDynamicSqlTemplateParser() {
		// 默认为StringTemplateLoaderFileSqlParser
		SqlTemplateParser defaultParser = queryProvideManager.getFileNamedQueryManager().getNamedSqlFileManager().getSqlStatmentParser();
		SqlTemplateParser parser = dynamicMethod.getDynamicSqlTemplateParser();
		if (parser==null) {
			// 没有指定parser，则使用默认的  
			parser = defaultParser;
		} else {
			if (parser instanceof SqlTemplateParserAware) {
				SqlTemplateParserAware aware = (SqlTemplateParserAware) parser;
				aware.setSqlTemplateParser(defaultParser);
			}
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

	
	/*public boolean matcher(JFishNamedFileQueryInfo queryInfo){
		if(queryInfo.getMatchers().isEmpty()){
			return !dynamicMethod.hasMatcher();
		}
		return queryInfo.getMatchers().contains(getQueryMatcherValue());
	}*/
	
	
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

	/***
	 * 获取方法返回类型的组件类型
	 * 如：List<User> -> User.class
	 * @author weishao zeng
	 * @return
	 */
	public Class<?> getResultComponentClass() {
		return dynamicMethod.getComponentClass(this.parameterValues);
	}
	
	public ParserContext getQueryParseContext() {
		return parserContext;
	}

}
