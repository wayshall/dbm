package org.onetwo.common.db.filequery;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.onetwo.common.db.AbstractQueryWrapper;
import org.onetwo.common.db.ParsedSqlContext;
import org.onetwo.common.db.filequery.ParsedSqlUtils.ParsedSqlWrapper;
import org.onetwo.common.db.filequery.ParsedSqlUtils.ParsedSqlWrapper.SqlParamterMeta;
import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.common.db.spi.CreateQueryCmd;
import org.onetwo.common.db.spi.FileNamedSqlGenerator;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.db.spi.SqlParamterPostfixFunctionRegistry;
import org.onetwo.common.db.sql.QueryOrderByable;
import org.onetwo.common.db.sqlext.ExtQueryUtils;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.spring.ftl.TemplateParser;
import org.onetwo.common.utils.ArrayUtils;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.RowMapper;

/***
 * 基于文件命名查询的QueryWrapper
 * 对基于文件创建和orm相关QueryWrapper的过程的包装
 * @author wayshall
 *
 */
public class DefaultFileQueryWrapper extends AbstractQueryWrapper implements QueryOrderByable {

//	private DynamicQuery query;
//	private JFishNamedFileQueryInfo info;
	protected QueryProvideManager baseEntityManager;
	private QueryWrapper dataQuery;
	
	protected boolean countQuery;


	private Map<Object, Object> params = LangUtils.newHashMap();
	private int firstRecord = -1;
	private int maxRecords;
	private Class<?> resultClass;
	
	protected NamedQueryInfo info;
	private TemplateParser parser;
	private ParserContext parserContext;
	
	private String[] ascFields;
	private String[] desFields;

	public DefaultFileQueryWrapper(QueryProvideManager baseEntityManager, NamedQueryInfo info, boolean count, TemplateParser parser) {
		Assert.notNull(baseEntityManager);
		this.baseEntityManager = baseEntityManager;
		this.countQuery = count;
		this.parser = parser;
		
		this.info = info;
//		this.resultClass = countQuery?Long.class:info.getMappedEntityClass();
		if(countQuery){
			this.resultClass = LangUtils.isIntegralType(info.getMappedEntityClass())?info.getMappedEntityClass():Long.class;
		}else{
			this.resultClass = info.getMappedEntityClass();
		}
		this.parserContext = ParserContext.create(info);
	}
	
//	abstract protected DataQuery createDataQuery(DynamicQuery query);
//	abstract protected DataQuery createDataQuery(String sql, Class<?> mappedClass);

	
	protected QueryWrapper createDataQuery(CreateQueryCmd createQueryCmd){
		QueryWrapper dataQuery = this.baseEntityManager.createQuery(createQueryCmd);
		return dataQuery;
	}
	
	protected QueryWrapper createDataQueryIfNecessarry(){
		if(dataQuery!=null){
			return dataQuery;
		}
		Optional<SqlFunctionDialet> sqlFunction = baseEntityManager.getSqlFunctionDialet();
		FileNamedSqlGenerator sqlGen = new DefaultFileNamedSqlGenerator(countQuery, parser, parserContext, 
																		resultClass, ascFields, desFields, params, sqlFunction);
		ParsedSqlContext sqlAndValues = sqlGen.generatSql();
		if(sqlAndValues.isListValue()){
			CreateQueryCmd createQueryCmd = new CreateQueryCmd(sqlAndValues.getParsedSql(), resultClass, info.isNativeSql());
			QueryWrapper dataQuery = createDataQuery(createQueryCmd);
			int position = 0;
			for(Object value : sqlAndValues.asList()){
				dataQuery.setParameter(position++, value);
			}
			setLimitResult();
			this.dataQuery = dataQuery;
			return dataQuery;
		}

		String parsedSql = sqlAndValues.getParsedSql();
		CreateQueryCmd createQueryCmd = new CreateQueryCmd(parsedSql, resultClass, info.isNativeSql());
		QueryWrapper dataQuery = createDataQuery(createQueryCmd);
		
		SqlParamterPostfixFunctionRegistry sqlFunc = baseEntityManager.getSqlParamterPostfixFunctionRegistry();
		ParsedSqlWrapper sqlWrapper = ParsedSqlUtils.parseSql(parsedSql, sqlFunc);
		BeanWrapper paramBean = SpringUtils.newBeanMapWrapper(sqlAndValues.asMap());
		for(SqlParamterMeta parameter : sqlWrapper.getParameters()){
			if(!paramBean.isReadableProperty(parameter.getProperty()))
				continue;
			/*Object value = paramBean.getPropertyValue(parameterName);
			if(parameter.hasFunction()){
				value = ReflectUtils.invokeMethod(parameter.getFunction(), SqlParamterFunctions.getInstance(), value);
			}*/
			Object pvalue = parameter.getParamterValue(paramBean);
			if(pvalue!=null && info.getQueryConfig().isLikeQueryField(parameter.getName())){
				pvalue = ExtQueryUtils.getLikeString(pvalue.toString());
			}
			//wrap pvalue
			dataQuery.setParameter(parameter.getName(), pvalue);
		}
		
		setLimitResult();

		this.dataQuery = dataQuery;
		return dataQuery;
	}
	private void setLimitResult(){
		if(firstRecord>0)
			dataQuery.setFirstResult(firstRecord);
		if(maxRecords>0)
			dataQuery.setMaxResults(maxRecords);
	}

	public QueryWrapper setParameter(int index, Object value) {
		this.params.put(index, value);
		return this;
	}

	public QueryWrapper setParameter(String name, Object value) {
		JNamedQueryKey key = JNamedQueryKey.ofKey(name);
		if(key!=null){
			this.processQueryKey(key, value);
		}else{
			this.params.put(name, value);
		}
		return this;
	}

	public <T> T getSingleResult() {
		return createDataQueryIfNecessarry().getSingleResult();
	}

	public int executeUpdate() {
		return this.createDataQueryIfNecessarry().executeUpdate();
	}
	
	public <T> List<T> getResultList() {
		return createDataQueryIfNecessarry().getResultList();
	}

	public QueryWrapper setFirstResult(int firstResult) {
		this.firstRecord = firstResult;
		return this;
	}

	public QueryWrapper setMaxResults(int maxResults) {
		this.maxRecords = maxResults;
		return this;
	}

	public QueryWrapper setResultClass(Class<?> resultClass) {
		this.resultClass = resultClass;
		return this;
	}

	@Override
	public QueryWrapper setParameters(Map<String, Object> params) {
		for(Entry<String, Object> entry : params.entrySet()){
			setParameter(entry.getKey(), entry.getValue());
		}
		return this;
	}

	/****
	 * 根据key类型设置参数、返回结果类型、排序……等等
	 * @param params
	 */
	public void setQueryAttributes(Map<Object, Object> params) {
		Object key;
		for(Entry<Object, Object> entry : params.entrySet()){
			key = entry.getKey();
			if(String.class.isInstance(key)){
				setParameter(key.toString(), entry.getValue());
			}else if(Integer.class.isInstance(key)){
				setParameter((Integer)key, entry.getValue());
			}else if(JNamedQueryKey.class.isInstance(key)){
				this.processQueryKey((JNamedQueryKey)key, entry.getValue());
			}
		}
	}

	private void processQueryKey(JNamedQueryKey qkey, Object value){
		switch (qkey) {
			case ResultClass:
				if(!countQuery)
					setResultClass((Class<?>)value);
				break;
			case ASC:
				String[] ascFields = CUtils.asStringArray(value);
				asc(ascFields);
				break;
			case DESC:
				String[] desFields = CUtils.asStringArray(value);
				desc(desFields);
				break;
			/*case ParserContext:
				this.setParserContext((ParserContext)value);*/
			default:
				break;
		}
		
//		if(JNamedQueryKey.ResultClass.equals(qkey) && !countQuery){//count qyery 忽略
//			setResultClass((Class<?>)value);
//		}
	}
	
	public QueryWrapper setParameters(List<Object> params) {
		int position = 1;
		for(Object value : params){
			setParameter(position, value);
			position++;
		}
		return this;
	}

	@Override
	public void asc(String... fields) {
		this.ascFields = fields;
		/*if(isNeedParseSql() && QueryOrderByable.class.isAssignableFrom(query.getClass())){
			((QueryOrderByable)query).asc(fields);
		}else{
			throw new UnsupportedOperationException("the query can't supported orderby, you need set ignore.null to true.");
		}*/
	}

	@Override
	public void desc(String... fields) {
		this.desFields = fields;
	}
	

	@Override
	public QueryWrapper setParameters(Object[] params) {
		if(ArrayUtils.hasNotElement(params))
			return this;
		int position = 1;
		for(Object value : params){
			setParameter(position++, value);
		}
		return this;
	}

	public QueryWrapper setLimited(final Integer first, final Integer max) {
		this.firstRecord = first;
		this.maxRecords = max;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getRawQuery(Class<T> clazz) {
		this.createDataQueryIfNecessarry();
		return (T)dataQuery;
	}
	@Override
	public QueryWrapper setQueryConfig(Map<Object, Object> configs) {
		setQueryAttributes(params);
		return this;
	}

	/*public void setParserContext(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

	final public ParserContext getParserContext() {
		return parserContext;
	}*/

	@Override
	public void setRowMapper(RowMapper<?> rowMapper) {
		this.dataQuery.setRowMapper(rowMapper);
	}

}
