package org.onetwo.common.db.dquery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EnumType;

import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.common.db.dquery.DynamicMethod.DynamicMethodParameter;
import org.onetwo.common.db.dquery.annotation.AsCountQuery;
import org.onetwo.common.db.dquery.annotation.BatchObject;
import org.onetwo.common.db.dquery.annotation.ExecuteUpdate;
import org.onetwo.common.db.dquery.annotation.Param;
import org.onetwo.common.db.dquery.annotation.QueryDispatcher;
import org.onetwo.common.db.filequery.JNamedQueryKey;
import org.onetwo.common.db.spi.QueryConfigData;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.db.sqlext.ExtQueryUtils;
import org.onetwo.common.proxy.AbstractMethodResolver;
import org.onetwo.common.proxy.BaseMethodParameter;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.Page;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.FileNamedQueryException;
import org.onetwo.dbm.mapping.DbmEnumValueMapping;
import org.springframework.core.MethodParameter;


public class DynamicMethod extends AbstractMethodResolver<DynamicMethodParameter>{

	public static DynamicMethod newDynamicMethod(Method method){
		return new DynamicMethod(method);
	}

	public static final List<String> EXECUTE_UPDATE_PREFIX = LangUtils.newArrayList("save", "update", "remove", "delete", "insert", "create");
	public static final List<String> BATCH_PREFIX = LangUtils.newArrayList("batch");
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
	private AsCountQuery asCountQuery;
//	private List<String> parameterNames;
	
	private DynamicMethodParameter pageParamter;
	private DynamicMethodParameter dispatcherParamter;
	
	private QueryConfigData queryConfig;
	
	public DynamicMethod(Method method){
		super(method);
		
		this.checkAndSetExecuteType();

		//check query swither
		checkAndFindQuerySwitch(parameters);
		
//		Class<?> returnClass = method.getReturnType();
		Class<?> returnClass = getActualReturnType();
//		Class<?> compClass = ReflectUtils.getGenricType(method.getGenericReturnType(), 0);
		Class<?> compClass = getActualComponentType();
		Optional<DynamicMethodParameter> pageParameterOpt = this.findPagePrarameter();
		if(returnClass==void.class){
//			DynamicMethodParameter firstParamter = parameters.get(0);
//			pageParamter = dispatcherParamter!=null?parameters.get(1):parameters.get(0);
//			this.pageParamter = this.findPagePrarameter();
			if(pageParameterOpt.isPresent()){
				this.pageParamter = pageParameterOpt.get();
				returnClass = pageParamter.getParameterType();
				//获取page泛型类
				Type ptype = pageParamter.getGenericParameterType();
				if(ptype instanceof ParameterizedType){
					compClass = ReflectUtils.getGenricType(ptype, 0);
				}
			}
		}else if(Page.class==returnClass){
			this.pageParamter = pageParameterOpt.orElseThrow(()->{
				return new FileNamedQueryException("no Page type parameter found for paginaton method: " + method.toGenericString());
			});
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
	
	final public Class<?> getActualReturnType(){
		Class<?> returnClass = method.getReturnType();
		if(isReturnOptional()){
			returnClass = ReflectUtils.getGenricType(method.getGenericReturnType(), 0);
		}
		return returnClass;
	}
	
	@SuppressWarnings("rawtypes")
	final public Class<?> getActualComponentType(){
		Class compClass = ReflectUtils.getGenricType(method.getGenericReturnType(), 0);
		if(isReturnOptional()){
			Optional<ParameterizedType> parameterizedType = ReflectUtils.getParameterizedType(method.getGenericReturnType(), 0);
			compClass = parameterizedType.map(ptype->{
				return ReflectUtils.getGenricType(ptype, 0);
			})
			.orElse(compClass);
		}
		return compClass;
	}
	
	public boolean hasPageParamter() {
		return pageParamter!=null;
	}

	public final boolean isAnnotationPresent(Class<? extends Annotation> annoClass){
		return this.method.getAnnotation(annoClass)!=null;
	}
	private Optional<DynamicMethodParameter> findPagePrarameter(){
		return parameters.stream()
				.filter(p->p.getParameterType()==Page.class)
				.findAny()
				/*.orElseThrow(()->{
					return new FileNamedQueryException("no Page type parameter found for paginaton method: " + method.toGenericString());
				})*/;
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
	
	private void checkAndFindQuerySwitch(List<DynamicMethodParameter> parameters){
		this.dispatcherParamter = parameters.stream().filter(p->{
								if(p.hasParameterAnnotation(QueryDispatcher.class)){
									if(p.getParameterIndex()!=0){
										throw new FileNamedQueryException("Dispatcher must be first parameter but actual index is " + (p.getParameterIndex()+1));
									}/*else if(p.getParameterType()!=String.class){
										throw new FileNamedQueryException("Dispatcher must be must be a String!");
									}*/
									return true;
								}
								return false;
							})
						.findFirst()
						.orElse(null);
	}
	
	public boolean isAsCountQuery(){
		return asCountQuery!=null;
	}
	
	@Override
	protected DynamicMethodParameter createMethodParameter(Method method, int parameterIndex, Parameter parameter) {
		return new DynamicMethodParameter(method, parameterIndex, parameter);
	}
	
	public Page<?> getPageParamter(Object[] args) {
		return (Page<?>)args[pageParamter.getParameterIndex()];
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
		}else{
			this.update = EXECUTE_UPDATE_PREFIX.contains(StringUtils.getFirstWord(this.method.getName()));
			this.batchUpdate = BATCH_PREFIX.contains(StringUtils.getFirstWord(this.method.getName()));
		}
		if(!batchUpdate){
			this.batchUpdate = judgeBatchUpdateFromParameterObjects(parameters);
		}
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
				val = ((DbmEnumValueMapping)val).getMappingValue();
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
			pvalue = args[mp.getParameterIndex()];
//			handleArg(values, parserContext, mp, pvalue);
			handleArg(values, mp, pvalue);
		}
		
//		buildQueryConfig(parserContext);

//		values.put(JNamedQueryKey.ParserContext, parserContext);
		if(componentClass!=null){
			values.put(JNamedQueryKey.ResultClass, componentClass);
		}
		return values;
	}
	
	public Method getMethod() {
		return method;
	}

	public List<DynamicMethodParameter> getParameters() {
		return parameters;
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public Class<?> getComponentClass() {
		return componentClass;
	}

	public String getQueryName() {
		return queryName;
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
