package org.onetwo.common.db.filequery;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.QueryConfigData;
import org.onetwo.common.db.spi.QueryContextVariable;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.list.JFishList;
import org.onetwo.common.utils.list.NoIndexIt;
import org.springframework.util.Assert;

public class ParserContext implements Map<Object, Object> {
	
	public static ParserContext create(NamedQueryInfo queryInfo, Object...params){
		ParserContext ctx = null;
		if(LangUtils.isEmpty(params)) {
			ctx = new ParserContext(queryInfo, new HashMap<Object, Object>());
		} else {
			ctx = new ParserContext(queryInfo, CUtils.asMap(params));
		}
		ctx.setQueryConfig(queryInfo.getQueryConfig());
		return ctx;
	}


	public static final String CONTEXT_KEY = ParserContextFunctionSet.CONTEXT_KEY;//_func; helper ParserContextFunctionSet
	public static final String QUERY_CONFIG = "_queryConfig";
//	private static final String QUERY_CONFIG_FUNC = "_queryfunc";
	public static final String DB_KEY = SqlFunctionFactory.CONTEXT_KEY;
//	public static final String PARSER_ACCESS_KEY = NamedQueryInfo.FRAGMENT_KEY;
	
	private Map<Object, Object> context;
	final private NamedQueryInfo queryInfo;
	
	public ParserContext(NamedQueryInfo queryInfo, Map<Object, Object> context){
		this.queryInfo = queryInfo;
		this.context = context;
		this.context.put(CONTEXT_KEY, ParserContextFunctionSet.getInstance());
	}
	

	public NamedQueryInfo getQueryInfo() {
		return queryInfo;
	}

	final public void setQueryConfig(QueryConfigData config){
		Assert.notNull(config, "QueryConfigData can not be null");
		QueryConfigData oldConfig = (QueryConfigData)this.context.put(QUERY_CONFIG, config);
		if(oldConfig!=null){
			//remove old
			/*Stream.of(oldConfig.getVariables()).forEach(e -> 
														context.remove(e.varName()));*/
			//兼容java8以下
			JFishList.wrap(oldConfig.getVariables()).each(new NoIndexIt<QueryContextVariable>() {
				@Override
				protected void doIt(QueryContextVariable e) throws Exception {
					context.remove(e.varName());
				}
			});
		}
		if(config.getVariables()!=null){
			/*Stream.of(config.getVariables()).forEach(e -> 
														context.put(e.varName(), e));*/
			JFishList.wrap(config.getVariables()).each(new NoIndexIt<QueryContextVariable>() {
				@Override
				protected void doIt(QueryContextVariable e) throws Exception {
					context.put(e.varName(), e);
				}
			});
		}
	}
	
	/*public QueryConfigData getQueryConfig(){
		QueryConfigData config = (QueryConfigData)context.get(QUERY_CONFIG);
		return config==null?ParsedSqlUtils.EMPTY_CONFIG:config;
	}*/
	public int size() {
		return context.size();
	}

	public boolean isEmpty() {
		return context.isEmpty();
	}

	public boolean containsKey(Object key) {
		return context.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return context.containsValue(value);
	}

	public Object get(Object key) {
		return context.get(key);
	}

	public Object put(Object key, Object value) {
		return context.put(key, value);
	}

	public Object remove(Object key) {
		return context.remove(key);
	}

	public void putAll(Map<? extends Object, ? extends Object> m) {
		context.putAll(m);
	}

	public void clear() {
		context.clear();
	}

	public Set<Object> keySet() {
		return context.keySet();
	}

	public Collection<Object> values() {
		return context.values();
	}

	public Set<java.util.Map.Entry<Object, Object>> entrySet() {
		return context.entrySet();
	}

	public boolean equals(Object o) {
		return context.equals(o);
	}

	public int hashCode() {
		return context.hashCode();
	}
	
}
