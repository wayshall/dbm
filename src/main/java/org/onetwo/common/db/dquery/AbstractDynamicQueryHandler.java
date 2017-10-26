package org.onetwo.common.db.dquery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.onetwo.common.convert.Types;
import org.onetwo.common.db.ParsedSqlContext;
import org.onetwo.common.db.filequery.ParsedSqlUtils;
import org.onetwo.common.db.filequery.ParsedSqlUtils.ParsedSqlWrapper;
import org.onetwo.common.db.filequery.ParsedSqlUtils.ParsedSqlWrapper.SqlParamterMeta;
import org.onetwo.common.db.spi.DynamicQueryHandler;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.db.spi.SqlParamterPostfixFunctionRegistry;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.profiling.TimeCounter;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.MathUtils;
import org.onetwo.common.utils.Page;
import org.onetwo.dbm.core.internal.AbstractDbmInterceptorChain.RepositoryDbmInterceptorChain;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.exception.FileNamedQueryException;
import org.onetwo.dbm.jdbc.spi.DbmInterceptor;
import org.onetwo.dbm.jdbc.spi.DbmInterceptorChain;
import org.slf4j.Logger;
import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.util.Assert;

import com.google.common.cache.LoadingCache;

abstract public class AbstractDynamicQueryHandler implements DynamicQueryHandler  {
	
	protected Logger logger = JFishLoggerFactory.getLogger(this.getClass());

	private LoadingCache<Method, DynamicMethod> methodCache;
	private QueryProvideManager em;
//	private Object proxyObject;
	private NamedParameterJdbcOperations jdbcOperations;
	final protected List<Class<?>> proxyInterfaces = new ArrayList<Class<?>>();
	
	public AbstractDynamicQueryHandler(QueryProvideManager em, LoadingCache<Method, DynamicMethod> methodCache, Class<?>... proxiedInterfaces){
		this.em = em;
		this.methodCache = methodCache;
		
//		this.dbmJdbcOperations = em.getSessionFactory().getServiceRegistry().getDbmJdbcOperations();
		this.jdbcOperations = em.getJdbcOperations();
		this.proxyInterfaces.addAll(Arrays.asList(proxiedInterfaces));
		Assert.notNull(jdbcOperations);
	}

	/*private Optional<DbmEntityManager> getDbmEntityManager(){
		if(DbmEntityManager.class.isInstance(em)){
			return Optional.of((DbmEntityManager)em);
		}
		return Optional.empty();
	}*/

	final protected NamedQueryInfo getNamedQueryInfo(NamedQueryInvokeContext invokeContex) {
		String qname = invokeContex.getQueryName();
		NamedQueryInfo queryInfo = this.em.getFileNamedQueryManager().getNamedSqlFileManager().getNamedQueryInfo(qname);
		return queryInfo;
	}

//	@Override
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		if(Object.class  == method.getDeclaringClass()) {
			String name = method.getName();
			if("equals".equals(name)) {
				return proxy == args[0];
			} else if("hashCode".equals(name)) {
				return System.identityHashCode(proxy);
			} else if("toString".equals(name)) {
				return proxy.getClass().getName() + "@" +
	               Integer.toHexString(System.identityHashCode(proxy)) + ", InvocationHandler " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}

		DynamicMethod dmethod = getDynamicMethod(method);
		
//		return invoke0(proxy, dmethod, args);
		return invokeWithInterceptor(proxy, dmethod, args);
	}
	
	private Object invokeWithInterceptor(Object proxy, DynamicMethod dmethod, Object[] args){
		MethodDynamicQueryInvokeContext invokeContext = new MethodDynamicQueryInvokeContext(em, dmethod, args);
		NamedQueryInfo namedQueryInfo = getNamedQueryInfo(invokeContext);
		invokeContext.setNamedQueryInfo(namedQueryInfo);
		
		Collection<DbmInterceptor> interceptors = this.em.getRepositoryInterceptors();
		if(interceptors.isEmpty()){
			return invokeMethod(proxy, invokeContext);
		}
		DbmInterceptorChain chain = new RepositoryDbmInterceptorChain(proxy, invokeContext, interceptors, ()->invokeMethod(proxy, invokeContext));
		return chain.invoke();
	}
	
	private Object invokeMethod(Object proxy, MethodDynamicQueryInvokeContext invokeContext) {
		try {
			return this.dispatchInvoke(proxy, invokeContext);
		}/* catch (HibernateException e) {
			throw (HibernateException) e;
		}*/
		catch (FileNamedQueryException e) {
			throw e;
		}
		catch (DbmException e) {
			throw e;
		}catch (Throwable e) {
//			throw new FileNamedQueryException("invoke query["+invokeContext.getQueryName()+"] error : " + e.getMessage(), e);
			throw new FileNamedQueryException("execute dbm query error : " + e.getMessage(), e).put("queryName", invokeContext.getQueryName());
		}
		
	}
	
	protected DynamicMethod getDynamicMethod(Method method){
		try {
			return methodCache.get(method);
		} catch (ExecutionException e) {
			throw new FileNamedQueryException("get dynamic method error: " + method.getName(), e);
//			return newDynamicMethod(method);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Object dispatchInvoke(Object proxy, MethodDynamicQueryInvokeContext invokeContext) throws Throwable {
		DynamicMethod dmethod = invokeContext.getDynamicMethod();
		Object[] args = invokeContext.getParameterValues();
		Class<?> resultClass = dmethod.getResultClass();
//		JFishNamedFileQueryInfo parsedQueryName = (JFishNamedFileQueryInfo) em.getFileNamedQueryManager().getNamedQueryInfo(invokeContext);

		if(logger.isDebugEnabled()){
			logger.debug("{}: {}", dmethod.getQueryName(), LangUtils.toString(args));
		}
		
		Object result = null;
//		Object[] methodArgs = null;
		
		if(dmethod.isBatch()){
			//先特殊处理
//			result = handleBatch(dmethod, args, parsedQueryName, parsedParams);
			result = handleBatch(invokeContext);
		}else{
//			methodArgs = Langs.toArray(invokeContext.getParsedParams());
			
			if(dmethod.isExecuteUpdate()){
				QueryWrapper dq = em.getFileNamedQueryManager().createQuery(invokeContext);
				result = dq.executeUpdate();
				
			}/*else if(Page.class.isAssignableFrom(resultClass)){
				Page<?> page = dmethod.getPageParamter(args);
				result = em.getFileNamedQueryManager().findPage(page, invokeContext);
				
			}*/else if(dmethod.hasPageParamter()){
				Page<?> page = dmethod.getPageParamter(args);
				result = em.getFileNamedQueryManager().findPage(page, invokeContext);
			}else if(Collection.class.isAssignableFrom(resultClass)){
				List<?> datalist = em.getFileNamedQueryManager().findList(invokeContext);
				if(resultClass.isAssignableFrom(datalist.getClass())){
					result = datalist;
				}else{
					Collection<Object> collections = CUtils.newCollections((Class<Collection<Object>>)resultClass, datalist.size());
					collections.addAll(datalist);
					result = collections;
				}
				
			}else if(QueryWrapper.class.isAssignableFrom(resultClass)){
				QueryWrapper dq = em.getFileNamedQueryManager().createQuery(invokeContext);
				return dq;
				
			}else if(dmethod.isAsCountQuery()){
//				parsedQueryName.setMappedEntity(dmethod.getResultClass());
				QueryWrapper dq = em.getFileNamedQueryManager().createCountQuery(invokeContext);
				result = dq.getSingleResult();
				result = Types.convertValue(result, resultClass);
			}else{
				result = em.getFileNamedQueryManager().findOne(invokeContext);
			}
		}
		
		if(dmethod.isReturnVoid()){
			return null;
		}
		
		return result;
	}
	
	
	protected Object handleBatch(MethodDynamicQueryInvokeContext invokeContext){
		DynamicMethod dmethod = invokeContext.getDynamicMethod();
		Collection<?> batchParameter = invokeContext.getBatchParameter();
		/*if(batchParameter==null){
			if(LangUtils.size(invokeContext.getParameterValues())!=1 || !Collection.class.isInstance(invokeContext.getParameterValues()[0])){
				throw new BaseException("BatchObject not found, the batch method parameter only supported one parameter and must a Collection : " + invokeContext.getDynamicMethod().getMethod().toGenericString());
			}
			
			//default is first arg
			batchParameter = (Collection<?>)args[0];
		}*/
		
		/*FileNamedSqlGenerator sqlGen = em.getFileNamedQueryManager().createFileNamedSqlGenerator(invokeContext);
		ParsedSqlContext sv = sqlGen.generatSql();*/
		ParsedSqlContext sv = em.getFileNamedQueryManager().parseNamedQuery(invokeContext);
//		JdbcDao jdao = this.jdao;
		/*DbmJdbcOperations dbmJdbcTemplate = this.dbmJdbcOperations;
		if(dbmJdbcTemplate==null){
			DataSource ds = Springs.getInstance().getBean(DataSource.class, false);
			dbmJdbcTemplate = new DbmJdbcTemplate(ds);
		}*/
		
		if(logger.isDebugEnabled()){
			logger.debug("===>>> batch insert start ...");
		}
		TimeCounter t = new TimeCounter("prepare insert", logger);
		t.start();
		
		BeanWrapper paramsContextBean = SpringUtils.newBeanMapWrapper(invokeContext.getParsedParams());
		List<Map<String, Object>> batchValues = LangUtils.newArrayList(batchParameter.size());
//		SqlParamterPostfixFunctionRegistry sqlFunc = em.getSessionFactory().getServiceRegistry().getSqlParamterPostfixFunctionRegistry();
		SqlParamterPostfixFunctionRegistry sqlFunc = em.getSqlParamterPostfixFunctionRegistry();
		ParsedSqlWrapper sqlWrapper = ParsedSqlUtils.parseSql(sv.getParsedSql(), sqlFunc);
		for(Object val : batchParameter){
			Map<String, Object> paramValueMap = new HashMap<String, Object>();
			BeanWrapper paramBean = SpringUtils.newBeanWrapper(val);
			
			for(SqlParamterMeta parameter : sqlWrapper.getParameters()){
				Object value = null;
				if(paramBean.isReadableProperty(parameter.getProperty())){
					value = parameter.getParamterValue(paramBean);
//					value = DbmUtils.convertSqlParameterValue(paramBean.getPropertyDescriptor(parameter.getProperty()), value, em.getSqlTypeMapping());
				}else{
					if(!paramsContextBean.isReadableProperty(parameter.getProperty()))
						throw new BaseException("batch execute parameter["+parameter.getProperty()+"] not found in bean["+val+"]'s properties or params");
				}
				
				if(value==null && paramsContextBean.isReadableProperty(parameter.getProperty()))
					value = parameter.getParamterValue(paramsContextBean);
				
				paramValueMap.put(parameter.getName(), value);
			}
			batchValues.add(paramValueMap);
		}

		if(logger.isDebugEnabled()){
			logger.debug("prepare insert finish!");
			logger.debug("batch sql : {}", sv.getParsedSql() );
		}
		t.stop();
		t.restart("insert to db");

		@SuppressWarnings("unchecked")
//		int[] counts = jdao.getNamedParameterJdbcTemplate().batchUpdate(sv.getParsedSql(), batchValues.toArray(new HashMap[0]));
		int[] counts = jdbcOperations.batchUpdate(sv.getParsedSql(), batchValues.toArray(new HashMap[0]));

		t.stop();
		
		if(logger.isDebugEnabled()){
			logger.debug("===>>> batch insert stop: {}", t.getMessage());
		}
		
		if(dmethod.isReturnVoid()){
			return null;
		}else if(dmethod.getResultClass()==int[].class || dmethod.getResultClass()==Integer[].class){
			return counts;
		}else{
			int count = MathUtils.sum(counts);
			return Types.convertValue(count, dmethod.getResultClass());
		}
	}

	@Override
	abstract public Object getQueryObject();

}
