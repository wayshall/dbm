package org.onetwo.common.db.dquery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EnumType;

import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.common.db.dquery.DynamicMethod.DynamicMethodParameter;
import org.onetwo.common.db.dquery.annotation.AsCountQuery;
import org.onetwo.common.db.dquery.annotation.BatchObject;
import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.dquery.annotation.ExecuteUpdate;
import org.onetwo.common.db.dquery.annotation.Param;
import org.onetwo.common.db.dquery.annotation.QueryDispatcher;
import org.onetwo.common.db.dquery.annotation.QueryName;
import org.onetwo.common.db.dquery.annotation.QueryParseContext;
import org.onetwo.common.db.dquery.annotation.QueryResultType;
import org.onetwo.common.db.dquery.annotation.QuerySqlTemplateParser;
import org.onetwo.common.db.dquery.annotation.Sql;
import org.onetwo.common.db.filequery.TemplateNameIsSqlTemplateParser;
import org.onetwo.common.db.spi.QueryConfigData;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.db.spi.SqlTemplateParser;
import org.onetwo.common.db.sqlext.ExtQueryUtils;
import org.onetwo.common.proxy.AbstractMethodResolver;
import org.onetwo.common.proxy.BaseMethodParameter;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.Page;
import org.onetwo.common.utils.PageRequest;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.FileNamedQueryException;
import org.onetwo.dbm.mapping.DbmEnumValueMapping;
import org.onetwo.dbm.utils.DbmUtils;
import org.springframework.core.MethodParameter;

import com.google.common.collect.Sets;


public class DynamicMethod extends AbstractMethodResolver<DynamicMethodParameter>{

	public static DynamicMethod newDynamicMethod(Method method){
		return new DynamicMethod(method);
	}

	public static final List<String> EXECUTE_UPDATE_PREFIX = LangUtils.newArrayList("save", "update", "remove", "delete", "insert", "create");
	public static final List<String> BATCH_PREFIX = LangUtils.newArrayList("batch", "batchUpdate", "batchInsert", "batchSave");
//	public static final String FIELD_NAME_SPERATOR = "By";
	
//	private final Method method;
//	private final List<DynamicMethodParameter> parameters;
	private final Class<?> resultClass;
	//mapping type
	private final Class<?> componentClass;
	private String queryName;
//	private final ExecuteUpdate executeUpdate;
	private boolean update;
	private boolean batchUpdate;
	private int batchSize = -1;
	private AsCountQuery asCountQuery;
//	private List<String> parameterNames;

	private DynamicMethodParameter pageParamter;
	private DynamicMethodParameter pageRequestParamter;
	private DynamicMethodParameter dispatcherParamter;
	
	private QueryConfigData queryConfig;
	
	
	/****
	 * 新增支持自定义解释器
	 * 用于自定义从其它地方（非sql文件，比如数据之类）加载sql模板？
	 * 在注解QueryName指定自定义的TemplateParser？
	 */
	private SqlTemplateParser dynamicSqlTemplateParser;
	private DynamicMethodParameter queryNameParameter;
	private DynamicMethodParameter sqlParameter;
//	private DynamicMethodParameter dynamicQueryMetaProviderParameter;
	
	/***
	 * 动态传入返回类型
	 */
	private DynamicMethodParameter resultTypeParameter;
	/****
	 * 标识解释上下文的参数
	 * QueryParseContext
	 */
	private DynamicMethodParameter parseContextParameter;
	
	private Set<DynamicMethodParameter> specialParameters = Sets.newHashSet();
	
	public DynamicMethod(Method method){
		super(method);
		
		this.checkAndSetExecuteType();
		this.findAndConfigSqlTemplateParser();

		//check query swither
//		checkAndFindQuerySwitch(parameters);
		// check queryName paramter
//		checkAndFindQueryNameParameter(parameters);
		checkAndFindSpecialParameters(parameters);
		
//		Class<?> returnClass = method.getReturnType();
		// example: List
		Class<?> returnClass = getActualReturnType();
		// example: List<User> => User.class
		Class<?> compClass = getActualComponentType();
		
		if(returnClass==void.class){
//			DynamicMethodParameter firstParamter = parameters.get(0);
//			pageParamter = dispatcherParamter!=null?parameters.get(1):parameters.get(0);
//			this.pageParamter = this.findPagePrarameter();
			if(findPagePrarameter()){
				returnClass = pageParamter.getParameterType();
				//获取page泛型类
				Type ptype = pageParamter.getGenericParameterType();
				if(ptype instanceof ParameterizedType){
					compClass = ReflectUtils.getGenricType(ptype, 0);
				}
			}
		}else if(Page.class==returnClass){
			/*this.pageParamter = pageParameterOpt.orElseThrow(()->{
				return new FileNamedQueryException("Page type parameter not found for paginaton method: " + method.toGenericString());
			});*/
			if (!findPagePrarameter() && !findPageRequestPrarameter()) {
				throw new FileNamedQueryException("Page type parameter not found for paginaton method: " + method.toGenericString());
			}
			/*if(Page.class==rClass){
//				throw new FileNamedQueryException("define Page Type at the first parameter and return void if you want to pagination: " + method.toGenericString());
				this.pageParamter = pageParameterOpt.orElseThrow(()->{
					return new FileNamedQueryException("no Page type parameter found for paginaton method: " + method.toGenericString());
				});
			}else if(QueryWrapper.class==rClass){
				compClass = null;
			}*/
		}else if(QueryWrapper.class==returnClass){
			compClass = null;
		}
		
		
		resultClass = returnClass;
		if(compClass==Object.class)
			compClass = resultClass;
		this.componentClass = compClass;

		checkAndFindAsCountQuery(componentClass);
		findAndSetQueryName(this.asCountQuery);
		
		LangUtils.println("resultClass: ${0}, componentClass:${1}", resultClass, compClass);
	}
	
	private void findAndConfigSqlTemplateParser() {
		DbmRepository dbmRepository = method.getDeclaringClass().getAnnotation(DbmRepository.class);
		if (dbmRepository.sqlTemplateParser()!=SqlTemplateParser.class) {
			this.dynamicSqlTemplateParser = DbmUtils.createDbmBean(dbmRepository.sqlTemplateParser());
		}
		QuerySqlTemplateParser parserAnno = method.getAnnotation(QuerySqlTemplateParser.class);
		if (parserAnno!=null && parserAnno.value()!=SqlTemplateParser.class) {
			this.dynamicSqlTemplateParser = DbmUtils.createDbmBean(parserAnno.value());
		}
	}
	
	/***
	 * 获取方法的返回类型，如果optional类型，则获取optional的泛型类型
	 * @author weishao zeng
	 * @return
	 */
	final public Class<?> getActualReturnType(){
		Class<?> returnClass = method.getReturnType();
		if(isReturnOptional()){
			returnClass = ReflectUtils.getGenricType(method.getGenericReturnType(), 0);
		}
		return returnClass;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	final public Class<?> getActualComponentType(){
		// 获取第一个泛型参数
		Class compClass = ReflectUtils.getGenricType(method.getGenericReturnType(), 0);
		if(isReturnOptional()){
			// 如果是Optional类型，则获取Optional的实际类型
			Optional<ParameterizedType> parameterizedType = ReflectUtils.getParameterizedType(method.getGenericReturnType(), 0);
			// 获取该实际类型的第一个泛型参数
			compClass = parameterizedType.map(ptype->{
				return ReflectUtils.getGenricType(ptype, 0);
			})
			.orElse(compClass);
		}
		return compClass;
	}
	
	public boolean hasPageParamter() {
		return pageParamter!=null || pageRequestParamter!=null;
	}

	public final boolean isAnnotationPresent(Class<? extends Annotation> annoClass){
		return this.method.getAnnotation(annoClass)!=null;
	}
	
	private boolean findPagePrarameter(){
//		this.pageParamter = parameters.stream()
//				.filter(p->p.getParameterType()==Page.class)
//				.findAny()
//				.orElse(null);
		return this.pageParamter!=null;
				/*.orElseThrow(()->{
					return new FileNamedQueryException("no Page type parameter found for paginaton method: " + method.toGenericString());
				})*/
	}
	
	private boolean findPageRequestPrarameter(){
		return this.pageRequestParamter!=null;
				/*.orElseThrow(()->{
					return new FileNamedQueryException("no Page type parameter found for paginaton method: " + method.toGenericString());
				})*/
	}
	
	/***
	 * dependency AsCountQuery
	 * @param asCountQuery
	 */
	private void findAndSetQueryName(AsCountQuery asCountQuery){
		if(asCountQuery!=null){
			queryName = method.getDeclaringClass().getName()+"."+asCountQuery.value();
		}else{
			queryName = method.getDeclaringClass().getName()+"."+method.getName();
		}
	}
	
	/***
	 * dependency componentClass
	 * @param componentClass
	 */
	private void checkAndFindAsCountQuery(Class<?> componentClass){
		this.asCountQuery = method.getAnnotation(AsCountQuery.class);
		if(asCountQuery!=null){
			if(update || batchUpdate){
				update = batchUpdate = false;
			}
			if(!LangUtils.isNumberType(componentClass)){
				throw new FileNamedQueryException("countquery's return type must be a number, but " + componentClass);
			}
		}
	}
	

	private void checkAndFindSpecialParameters(List<DynamicMethodParameter> parameters){
		for (DynamicMethodParameter parameter : parameters) {
			if (dispatcherParamter==null && parameter.hasParameterAnnotation(QueryDispatcher.class)){
				if(parameter.getParameterIndex()!=0){
					throw new FileNamedQueryException("Dispatcher must be first parameter but actual index is " + (parameter.getParameterIndex()+1));
				}
				dispatcherParamter = parameter;
				specialParameters.add(parameter);
				
			} else if (queryNameParameter==null && parameter.hasParameterAnnotation(QueryName.class)) {
				if(parameter.getParameterType()!=String.class){
					throw new FileNamedQueryException("@" + QueryName.class.getSimpleName() + " parameter type must be String.");
				}
//				QueryName queryNameAnno = parameter.getParameterAnnotation(QueryName.class);
//				if (queryNameAnno.templateParser()!=SqlTemplateParser.class) {
//					this.dynamicSqlTemplateParser = DbmUtils.createDbmBean(queryNameAnno.templateParser());
//				}
				queryNameParameter = parameter;
				specialParameters.add(parameter);
				
			} else if (sqlParameter==null && parameter.hasParameterAnnotation(Sql.class)) {
				if(parameter.getParameterType()!=String.class){
					throw new FileNamedQueryException("@" + Sql.class.getSimpleName() + " parameter type must be String.");
				}
//				Sql queryNameAnno = parameter.getParameterAnnotation(Sql.class);
//				this.dynamicSqlTemplateParser = DbmUtils.createDbmBean(TemplateNameIsSqlTemplateParser.class);
				this.dynamicSqlTemplateParser = TemplateNameIsSqlTemplateParser.INSTANCE;
				this.sqlParameter = parameter;
				this.queryNameParameter = parameter;
				specialParameters.add(parameter);
				
			} else if (resultTypeParameter==null && parameter.hasParameterAnnotation(QueryResultType.class)) {
				if(!(parameter.getParameterType() instanceof Class) && !parameter.getParameterType().isArray()){
					throw new FileNamedQueryException("@" + QueryResultType.class.getSimpleName() + " parameter type must be Class or Array.");
				}
				resultTypeParameter = parameter;
				specialParameters.add(parameter);
				
			} else if (parseContextParameter==null && parameter.hasParameterAnnotation(QueryParseContext.class)) {
				if(!Map.class.isAssignableFrom(parameter.getParameterType())){
					throw new FileNamedQueryException("@" + QueryParseContext.class.getSimpleName() + " parameter type must be Map.");
				}
				parseContextParameter = parameter;
				
			} else if (PageRequest.class.isAssignableFrom(parameter.getParameterType())) {
				this.pageRequestParamter = parameter;
//				specialParameters.add(parameter);
				
			} else if (Page.class.isAssignableFrom(parameter.getParameterType())) {
				this.pageParamter = parameter;
//				specialParameters.add(parameter);
			} 
//			else if (DynamicQueryMetaProvider.class.isAssignableFrom(parameter.getParameterType())) {
//				this.dynamicQueryMetaProviderParameter = parameter;
//			}
			
		}
	}
	
//	private void checkAndFindQuerySwitch(List<DynamicMethodParameter> parameters){
//		this.dispatcherParamter = parameters.stream().filter(p->{
//								if(p.hasParameterAnnotation(QueryDispatcher.class)){
//									if(p.getParameterIndex()!=0){
//										throw new FileNamedQueryException("Dispatcher must be first parameter but actual index is " + (p.getParameterIndex()+1));
//									}/*else if(p.getParameterType()!=String.class){
//										throw new FileNamedQueryException("Dispatcher must be must be a String!");
//									}*/
//									return true;
//								}
//								return false;
//							})
//						.findFirst()
//						.orElse(null);
//	}
//	
//	private void checkAndFindQueryNameParameter(List<DynamicMethodParameter> parameters){
//		this.queryNameParameter = parameters.stream().filter(p->{
//								if(p.hasParameterAnnotation(QueryName.class)){
//									if(p.getParameterType()!=String.class){
//										throw new FileNamedQueryException("@" + QueryName.class.getSimpleName() + " parameter type must be String.");
//									}
//									return true;
//								}
//								return false;
//							})
//						.findFirst()
//						.orElse(null);
//		if (this.queryNameParameter!=null) {
//			QueryName queryNameAnno = this.queryNameParameter.getParameterAnnotation(QueryName.class);
//			if (queryNameAnno.templateParser()!=SqlTemplateParser.class) {
//				this.dynamicSqlTemplateParser = DbmUtils.createDbmBean(queryNameAnno.templateParser());
//			}
//		}
//	}
	
	
	public boolean isAsCountQuery(){
		return asCountQuery!=null;
	}
	
	
	public SqlTemplateParser getDynamicSqlTemplateParser() {
		return dynamicSqlTemplateParser;
	}

	@Override
	protected DynamicMethodParameter createMethodParameter(Method method, int parameterIndex, Parameter parameter) {
		return new DynamicMethodParameter(method, parameterIndex, parameter);
	}
	
	public Page<?> getPageParamter(Object[] args) {
		if (this.pageParamter!=null) {
			return (Page<?>)args[pageParamter.getParameterIndex()];
		} else {
			PageRequest pageRequest = (PageRequest)args[pageRequestParamter.getParameterIndex()];
			return pageRequest.toPageObject();
		}
	}

	public String getQueryName(Object[] args) {
		if (this.queryNameParameter!=null) {
			String qname = (String)args[queryNameParameter.getParameterIndex()];
			return qname;
		}
		return queryName;
	}

	public Map<?, ?> getQueryParseContext(Object[] args) {
		if (this.parseContextParameter!=null) {
			Map<?, ?> ctx = (Map<?, ?>)args[parseContextParameter.getParameterIndex()];
			return ctx;
		}
		return null;
	}

	public Class<?> getResultClass(Object[] args) {
		if (this.resultTypeParameter!=null) {
			Object types = args[resultTypeParameter.getParameterIndex()];
			if (types.getClass().isArray()) {
				return (Class<?>)Array.get(types, 0);
			} else {
				return (Class<?>)types;
			}
		}
		return resultClass;
	}

	public Class<?> getComponentClass(Object[] args) {
		if (this.resultTypeParameter!=null) {
			Object types = args[resultTypeParameter.getParameterIndex()];
			if (types.getClass().isArray()) {
				return (Class<?>)Array.get(types, 1);
			} else {
				return (Class<?>)types;
			}
		}
		return componentClass;
	}
	
	protected boolean judgeBatchUpdateFromParameterObjects(List<DynamicMethodParameter> mparameters){
		for(DynamicMethodParameter mp : mparameters){
			if(mp.hasParameterAnnotation(BatchObject.class)){
				return true;
			}
		}
		return false;
	}
	
	final private void checkAndSetExecuteType(){
		ExecuteUpdate executeUpdate = method.getAnnotation(ExecuteUpdate.class);
		if(executeUpdate!=null){
			this.update = true;
			this.batchUpdate = executeUpdate.isBatch();
			this.batchSize = executeUpdate.batchSize();
		}else{
			this.update = EXECUTE_UPDATE_PREFIX.contains(StringUtils.getFirstWord(this.method.getName()));
			this.batchUpdate = BATCH_PREFIX.contains(StringUtils.getFirstWord(this.method.getName()));
		}
		if(!batchUpdate){
			this.batchUpdate = judgeBatchUpdateFromParameterObjects(parameters);
		}
	}
	
	public int getBatchSize() {
		return batchSize;
	}

	public MethodParameter remove(int index){
		return this.parameters.remove(index);
	}
	
	private Pair<String, Object> addAndCheckParamValue(Param name, String pname, final Object pvalue){
//		Object val = convertEnumValue(name, pvalue);
		Object val = pvalue;
		if (String.class.isInstance(val) && name.isLikeQuery()) {
//			values.add(ExtQueryUtils.getLikeString(pvalue.toString()));
			val = ExtQueryUtils.getLikeString(val.toString());
		} /*else if (pvalue.getClass().isArray()) {
			val = LangUtils.asList(pvalue);
		}*/ else {
			val = pvalue;
		}
		return Pair.of(pname, val);
	}
	
	private Object convertQueryValue(Param name, Object val){
		if (name!=null && val instanceof Enum) {
			if (val instanceof DbmEnumValueMapping) {
				val = ((DbmEnumValueMapping<?>)val).getEnumMappingValue();
			} else {
				Enum<?> enumValue = (Enum<?>)val;
				val = name.enumType()==EnumType.ORDINAL?enumValue.ordinal():enumValue.name();
			}
		} if (val!=null && val.getClass().isArray()) {
			val = LangUtils.asList(val);
		}
		return val;
	}
	
	protected void handleArg(Map<Object, Object> values, DynamicMethodParameter mp, Object pvalue){
		/*if(pvalue instanceof ParserContext){
//			parserContext.putAll((ParserContext) pvalue);
		}else */
		if(mp.hasParameterAnnotation(Param.class)){
			Param paramMeta = mp.getParameterAnnotation(Param.class);
			if(paramMeta.renamedUseIndex()){
				List<?> listValue = LangUtils.asList(pvalue);
				int index = 0;
				//parem0, value0, param1, value1, ...
				for(Object obj : listValue){
					Pair<String, Object> pair = addAndCheckParamValue(paramMeta, mp.getParameterName()+index, obj);
					if(pair!=null){
						putArg2Map(values, paramMeta, pair.getLeft(), pair.getRight());
//						if(addAndCheckParamValue(name, values, mp.getParameterName()+index, obj)){
						index++;
					}
				}
				/*values.add(mp.getParameterName());
				values.add(listValue);*/
				putArg2Map(values, paramMeta, mp.getParameterName(), listValue);
				
			}else{
				Pair<String, Object> pair = addAndCheckParamValue(paramMeta, mp.getParameterName(), pvalue);
				if(pair!=null){
					putArg2Map(values, paramMeta, pair.getLeft(), pair.getRight());
				}
			}
				
		}else if(mp.hasParameterAnnotation(BatchObject.class)){
			putArg2Map(values, null, BatchObject.class, pvalue);
			
		}else{
			/*values.add(mp.getParameterName());
			values.add(pvalue);*/
			putArg2Map(values, null, mp.getParameterName(), pvalue);
		}
		
	}
	
	private void putArg2Map(Map<Object, Object> values, Param paramMeta, Object key, Object value){
		if(values.containsKey(key)){
			throw new IllegalArgumentException("parameter has exist: " + key);
		}
		//see DbmUtils#getActualValue
		/*if(value instanceof Enum){
			values.put(key, ((Enum<?>)value).name());
		}else{
			values.put(key, value);
		}*/
		value = convertQueryValue(paramMeta, value);
		values.put(key, value);
	}
	
	/*protected void buildQueryConfig(ParserContext parserContext){
//		QueryConfig queryConfig = AnnotationUtils.findAnnotation(method, QueryConfig.class, true);//method.getAnnotation(QueryConfig.class);
		QueryConfig queryConfig = AnnotationUtils.findAnnotation(method, QueryConfig.class);
		if(queryConfig!=null){
//			QueryConfigData config = new QueryConfigData(queryConfig.value(), queryConfig.countQuery());
			QueryConfigData config = new QueryConfigData();
			config.setLikeQueryFields(Arrays.asList(queryConfig.likeQueryFields()));
			if(queryConfig.funcClass()==ParserContextFunctionSet.class){
				config.setVariables(ParserContextFunctionSet.getInstance());
			}else{
				QueryContextVariable func = (QueryContextVariable)ReflectUtils.newInstance(queryConfig.funcClass());
				config.setVariables(ParserContextFunctionSet.getInstance(), func);
			}
			parserContext.setQueryConfig(config);
			this.queryConfig = config;
			
		}else{
			parserContext.setQueryConfig(ParsedSqlUtils.EMPTY_CONFIG);
		}
	}*/
	
	public Object getMatcherValue(Object[] args){
		if(!hasDispatcher())
			return null;
		return args[dispatcherParamter.getParameterIndex()];
	}
	
	public boolean hasDispatcher(){
		return this.dispatcherParamter!=null;
	}

	public Map<Object, Object> toMapByArgs(Object[] args){
		Map<Object, Object> values = LangUtils.newHashMap(parameters.size());
		
		Object pvalue = null;
//		ParserContext parserContext = ParserContext.create();
		for(DynamicMethodParameter mp : parameters){
			if (this.specialParameters.contains(mp)) { // 过滤特殊参数
				continue;
			}
			pvalue = args[mp.getParameterIndex()];
//			handleArg(values, parserContext, mp, pvalue);
			handleArg(values, mp, pvalue);
		}
		
//		buildQueryConfig(parserContext);

//		values.put(JNamedQueryKey.ParserContext, parserContext);
//		if(componentClass!=null){
//			values.put(JNamedQueryKey.ResultClass, componentClass);
//		}
		return values;
	}
	
	public Method getMethod() {
		return method;
	}

	public List<DynamicMethodParameter> getParameters() {
		return parameters;
	}
	
	public boolean isExecuteUpdate(){
		/*String name = StringUtils.getFirstWord(this.method.getName());
		return EXECUTE_UPDATE_PREFIX.contains(name);*/
		return update && !batchUpdate; //(executeUpdate!=null && !executeUpdate.isBatch()) || );
	}
	
	public boolean isBatch(){
		return batchUpdate;//(executeUpdate!=null && executeUpdate.isBatch()) || );
	}
	
	public QueryConfigData getQueryConfig() {
		return queryConfig;
	}

	protected static class DynamicMethodParameter extends BaseMethodParameter {

		final protected String[] condidateParameterNames;
		final protected Param nameAnnotation;
		final protected String parameterName;
		
		public DynamicMethodParameter(Method method, int parameterIndex, Parameter parameter) {
			this(method, parameterIndex, parameter, LangUtils.EMPTY_STRING_ARRAY);
		}
		public DynamicMethodParameter(Method method, int parameterIndex, Parameter parameter, String[] parameterNamesByMethodName) {
			super(method, parameterIndex);
			this.condidateParameterNames = parameterNamesByMethodName;
			nameAnnotation = getParameterAnnotation(Param.class);

			//参数查找
			String pname = null;
			if(nameAnnotation!=null){
				pname = nameAnnotation.value();
			}else if(parameter!=null && parameter.isNamePresent()){
				pname = parameter.getName();
			}else if(condidateParameterNames.length>getParameterIndex()){
				pname = StringUtils.uncapitalize(condidateParameterNames[getParameterIndex()]);
			}else{
				pname = String.valueOf(getParameterIndex());
			}
			this.parameterName = pname;
		}

		/****
		 * 查询参数策略
		 * 如果有注解优先
		 * 其次是by分割符
		 * 以上皆否，则通过参数位置作为名称
		 */
		public String getParameterName() {
			return parameterName;
		}
		
	}
	
}
