package org.onetwo.common.db.sqlext;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onetwo.common.db.builder.QueryField;
import org.onetwo.common.db.builder.QueryFieldImpl;
import org.onetwo.common.db.sqlext.ExtQuery.K.IfNull;
import org.onetwo.common.exception.ServiceException;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.DbmException;
import org.slf4j.Logger;

import com.google.common.collect.Maps;

abstract public class AbstractExtQuery implements ExtQueryInner{
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());


	public static final String[] SQL_KEY_WORKDS = new String[]{" ", ";", ",", "(", ")", "'", "\"\"", "/", "+", "-"};

	protected Class<?> entityClass;
	protected String alias;
//	protected boolean aliasMainTableName=true;
	
	private QueryNameStrategy queryNameStrategy;
	
	protected Map<Object, Object> params;
	protected ParamValues paramsValue;
	// private Map<String, Object> paramsValue = new LinkedHashMap<String,
	// Object>();
	protected SQLSymbolManager symbolManager;

	private boolean debug;
	protected boolean hasBuilt;
//	private boolean sqlQuery = false;
//	private boolean throwIfCaseValueIsNull;
	private IfNull ifNull;

	protected StringBuilder sql;
	protected StringBuilder where;
	
	private List<ExtQueryListener> listeners;
	private boolean fireListeners = true;

	private final Map<?, ?> sourceParams;
	private final Map<String, Object> sourceParamNameSymbolMapping = Maps.newHashMap();
	
	public AbstractExtQuery(Class<?> entityClass, String alias, Map<?, ?> params, SQLSymbolManager symbolManager) {
		this(entityClass, alias, params, symbolManager, null);
	}
	public AbstractExtQuery(Class<?> entityClass, String alias, Map<?, ?> sourceParams, SQLSymbolManager symbolManager, List<ExtQueryListener> listeners) {
		this.entityClass = entityClass;
		if(StringUtils.isBlank(alias)){
			alias = StringUtils.uncapitalize(entityClass.getSimpleName());
		}
		this.alias = alias;
		this.symbolManager = symbolManager;
		this.sourceParams = sourceParams;//ImmutableMap.copyOf(sourceParams);
		this.listeners = (listeners==null?Collections.emptyList():listeners);
		
//		this.init(entityClass, this.alias);
		this.setQueryNameStrategy(new QueryNameStrategy(alias));
	}
	
	final public QueryNameStrategy getQueryNameStrategy() {
		return queryNameStrategy;
	}
	public Map<?, ?> getSourceParams() {
		return sourceParams;
	}
	protected void initParams(){
		if(sourceParams==null){
			this.params = CUtils.newLinkedHashMap();
		}else{
			this.params = new LinkedHashMap<Object, Object>(sourceParams);
		}
		
		this.params.forEach((k, v) -> {
			if (!(k instanceof String[])) {
				return ;
			}
			for (String key : (String[])k) {
				String name = StringUtils.split(key, ':')[0];
				this.sourceParamNameSymbolMapping.put(name, k);
			}
		});
	}


	public void initQuery(){
		this.hasBuilt = false;
		this.initParams();
		
//		setSqlQuery(getValueAndRemoveKeyFromParams(K.SQL_QUERY, sqlQuery));
		this.debug = getValueAndRemoveKeyFromParams(K.DEBUG, false);
		this.ifNull = getValueAndRemoveKeyFromParams(K.IF_NULL, IfNull.Calm);

		this.paramsValue = new ParamValues(symbolManager.getSqlDialet());
		
		this.fireListeners = getValueAndRemoveKeyFromParams(K.LISTENERS, true);
		
		this.fireInitListeners();
	}

	protected void beforeBuild(){
		//re-init-query if rebuild
		if(hasBuilt()){
			this.initQuery();
		}
	}
	protected void afaterBuild(){
//		this.fireAfterBuildListeners();
	}

	protected String getFromName(Class<?> entityClass){
		return this.queryNameStrategy.getFromName(entityClass);
	}
	
	protected void fireInitListeners(){
		if(this.fireListeners){
			for(ExtQueryListener l : this.listeners){
				l.onInit(this);
			}
		}
	}
	
	/*protected void fireAfterBuildListeners(){
		if(this.fireListeners){
			for(ExtQueryListener l : this.listeners){
				l.afterBuild(this);
			}
		}
	}*/
	
	public SQLFunctionManager getSqlFunctionManager() {
		return DefaultSQLFunctionManager.get();
	}

	@SuppressWarnings("unchecked")
	protected <T> T getValueAndRemoveKeyFromParams(Object key, T def){
		if(!this.params.containsKey(key))
			return def;
		T value = (T)this.params.get(key);
		this.params.remove(key);
		return value==null?def:value;
	}
	
	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
		this.alias = StringUtils.uncapitalize(entityClass.getSimpleName());
	}
	
	public Map<Object, Object> getParams() {
		return params;
	}

	
	public Object getParamByName(String name) {
		Object realName = this.sourceParamNameSymbolMapping.get(name);
		return this.params.get(realName);
	}

	protected boolean hasParams(Object key) {
		return this.params != null && !this.params.isEmpty() && this.params.containsKey(key);
	}

	protected ExtQuery buildWhere() {
		String swhere = this.buildWhere(params, false);
		if (StringUtils.isBlank(swhere))
			return this;
		where = new StringBuilder(swhere);
		return this;
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	String buildWhere(Map params, boolean isSubQuery) {
		/*String fname = "buildWhere";
		if(isDebug())
			UtilTimerStack.push(fname);*/
		StringBuilder where = new StringBuilder("");
		if (params == null || params.isEmpty())
			return where.toString();

		// List paramsValue = new ArrayList();
		// StringBuilder causeScript;
		// causeScript = new StringBuilder();
		boolean first = true;
		String h = null;
		int index = 0;
		for (Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>) params.entrySet()) {

			Object fields = entry.getKey();
			Object values = entry.getValue();

			if (K.ASC.equals(fields) || K.DESC.equals(fields) || LangUtils.isEmpty(fields)) {
				continue;
			}
			
			/*if(ExtQueryUtils.isContinueByCauseValue(values, ifNull)){
				continue;
			}*/

			if (fields instanceof KeyObject) {
				KeyObject keyObject = (KeyObject) fields;
				if (K.OR.key().equals(keyObject.key())) {
					if (!Map.class.isAssignableFrom(values.getClass())) {
						throw new ServiceException("sub query's vaue must be map!");
					}
					Map<?, ?> subParams = (Map<?, ?>) values;
					h = this.buildWhere(subParams, true);
					where.append("or ");
				} else if(K.AND.key().equals(keyObject.key())){
					if (!Map.class.isAssignableFrom(values.getClass())) {
						throw new ServiceException("sub query's vaue must be map!");
					}
					Map<?, ?> subParams = (Map<?, ?>) values;
					h = this.buildWhere(subParams, true);
					
					if(!first) {
						where.append("and ");
					}
				}
				
			} /*else if(K.RAW_QL.equals(fields)){
				if (!values.getClass().isArray() && !List.class.isAssignableFrom(values.getClass()) && !String.class.isAssignableFrom(values.getClass()))
					throw new ServiceException("raw-ql args error: " + values);
//				List listValue = L.tolist(values, true);
				List<?> listValue = CUtils.tolist(values, true);
				h = (String)listValue.get(0);
				if(!h.endsWith(" "))
					h += " ";
				this.paramsValue.directAddValue(listValue.subList(1, listValue.size()));
				
				if(!first)
					where.append("and ");
			}*/else{
				h = buildFieldQueryString(fields, values);
				if (StringUtils.isBlank(h))
					continue;
				
				if(index>0)
					where.append("and ");
			}

			if (StringUtils.isBlank(h))
				continue;

			// causeScript.append(h);

			if (first && !isSubQuery) {
				where.append("where ");
				first = false;
			}
			where.append(h);
			index++;

		}
		if (isSubQuery) {
			where.insert(0, "( ");
			where.insert(where.length(), ") ");
		}
		/*if(isDebug())
			UtilTimerStack.pop(fname);*/
		return where.toString();
	}

	protected String buildFieldQueryString(Object fields, Object values) {
		List<?> valueList = ExtQueryUtils.processValue(fields, values, ifNull);
		
		//ignore null
		/*if (valueList == null || valueList.isEmpty())
			return null;*/
		//actually, never can not be null
		//Fix: 去掉 valueList.isEmpty()条件, null没有被忽略，但empty却被忽略了，行为应该交给IfNull控制
		if (valueList == null) {
			return null;
		}
		
		if (valueList.isEmpty()) {
			if(ifNull==IfNull.Ignore){
				return null;
			}else if(ifNull==IfNull.Throw){
				throw new DbmException("the fields["+LangUtils.toString(fields)+"] 's value can not be empty.");
			}
		}
		

//		List<?> fieldList =  MyUtils.asList(fields);
		List<?> fieldList =  CUtils.trimAndexcludeTheClassElement(true, fields);
		int index = 0;
		String h = null;
		StringBuilder causeScript = new StringBuilder();
 
		for (int i = 0; i < fieldList.size(); i++) {
			Object p = fieldList.get(i);

			Object v = null;
			try {
				if (fieldList.size() == 1) {
					if(valueList.size()==1) {
						v = valueList.get(0);
					} else {
						v = values;
					}
				}else{
					v = valueList.get(i);
				}
			} catch (IndexOutOfBoundsException e) {
				v = valueList.get(0);//if can't find the corresponding value, get the first one.
			}

			QueryField qf = QueryFieldImpl.create(p);
			qf.init(this, v);
			
//			SQLSymbolParserContext context = SQLSymbolParserContext.create(qf.getFieldName(), v, paramsValue, ifNull);
			h = getSymbolManager().getHqlSymbolParser(qf.getOperator()).parse(qf);

			if (StringUtils.isBlank(h))
				continue;

			if (index > 0)
				causeScript.append("or ");

			causeScript.append(h);
			index++;
		}

		if (index > 1) {
			causeScript.insert(0, "( ");
			causeScript.insert(causeScript.length() - 1, " )");
		}

		return causeScript.toString();
	}

	
	public String getFieldName(String f) {
		return this.queryNameStrategy.getFieldName(f);
		/*Assert.hasText(f);
		f = appendAlias(translateAt(f));
		checkFieldNameValid(f);
		return f;*/
	}
	
	public SQLSymbolManager getSymbolManager() {
		return symbolManager;
	}
	public Class<?> getEntityClass() {
		return entityClass;
	}
	

	/*
	 * public Map<String, Object> getParamsValue() { return paramsValue; }
	 */

	public ParamValues getParamsValue() {
		return paramsValue;
	}

	/*
	 * public List getValues() { return new
	 * ArrayList(this.paramsValue.values()); }
	 */

	public String getSql() {
		return sql.toString();
	}
	public StringBuilder getWhere() {
		return where;
	}

	public boolean isDebug() {
		return debug;
	}

	public IfNull getIfNull() {
		return ifNull;
	}

	public void setIfNull(IfNull ifNull) {
		this.ifNull = ifNull;
	}
	
	public boolean calmIfNull(){
		return IfNull.Calm == this.ifNull;
	}
	@Override
	public boolean hasBuilt() {
		return hasBuilt;
	}
	/*@Override
	public boolean isSqlQuery() {
		return sqlQuery;
	}
	protected void setSqlQuery(boolean sqlQuery) {
		this.sqlQuery = sqlQuery;
	}*/
	@Override
	public boolean hasParameterField(String fieldName) {
		return getAllParameterFieldNames().contains(fieldName);
	}
	

	@Override
	public Set<String> getAllParameterFieldNames() {
		Set<String> fields = ExtQueryUtils.getAllParameterFieldNames(getParams());
		return fields;
	}
	
	final public void setQueryNameStrategy(QueryNameStrategy queryNameStrategy) {
		this.queryNameStrategy = queryNameStrategy;
	}
	
}
