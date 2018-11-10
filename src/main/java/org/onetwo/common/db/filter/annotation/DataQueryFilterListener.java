package org.onetwo.common.db.filter.annotation;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.onetwo.common.convert.Types;
import org.onetwo.common.db.filter.DataQueryParamaterEnhancer;
import org.onetwo.common.db.filter.IDataQueryParamterEnhancer;
import org.onetwo.common.db.sqlext.ExtQuery;
import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.db.sqlext.ExtQueryInner;
import org.onetwo.common.db.sqlext.ExtQueryListenerAdapter;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.Springs;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

//@Component
public class DataQueryFilterListener extends ExtQueryListenerAdapter{
	static final IDataQueryParamterEnhancer NULL_ENHANCER = new IDataQueryParamterEnhancer(){
		@Override
		public Map<Object, Object> enhanceParameters(ExtQuery query) {
			return Collections.emptyMap();
		}
	};
	
	private LoadingCache<Class<?>, IDataQueryParamterEnhancer> queryParamaterEnhancerCaches = CacheBuilder.newBuilder()
																									.build(new CacheLoader<Class<?>, IDataQueryParamterEnhancer>(){

																										@Override
																										public IDataQueryParamterEnhancer load(Class<?> entityClass) throws Exception {
//																											DataQueryParamaterEnhancer dqpe = entityClass.getAnnotation(DataQueryParamaterEnhancer.class);
//																											DataQueryParamaterEnhancer dqpe = AnnotationUtils.findAnnotation(entityClass, DataQueryParamaterEnhancer.class);
																											DataQueryParamaterEnhancer dqpe = AnnotatedElementUtils.findMergedAnnotation(entityClass, DataQueryParamaterEnhancer.class);
																											if(dqpe==null){
																												return NULL_ENHANCER;
																											}
																											Class<? extends IDataQueryParamterEnhancer> enhancerCls = dqpe.value();
																											boolean useSpringContext = Springs.getInstance().isInitialized();
																											IDataQueryParamterEnhancer enhancer = useSpringContext?Springs.getInstance().getBean(enhancerCls):null;
																											if(enhancer==null){
																												enhancer = ReflectUtils.newInstance(enhancerCls);
																											}
																											return enhancer;
																										}
																										
																									});

	@Override
	public void onInit(ExtQuery q) {
		ExtQueryInner query = (ExtQueryInner) q;
		Boolean hasDataFilter = (Boolean)query.getParams().remove(K.DATA_FILTER);
		
		Object dataFilterValue = LangUtils.firstNotNull(query.getParams().remove(DataQueryFilter.class), hasDataFilter);
		boolean isEnabledDataFilter = dataFilterValue!=null?Types.convertValue(dataFilterValue, Boolean.class):true;
		if(isEnabledDataFilter){
			DataQueryFilter qdf = (DataQueryFilter)query.getEntityClass().getAnnotation(DataQueryFilter.class);
			if(qdf!=null)
				addParameterByDataQueryFilter(query, qdf);
		}

		Object hasDataQueryParamaterEnhancer = query.getParams().remove(DataQueryParamaterEnhancer.class);
		dataFilterValue = LangUtils.firstNotNull(hasDataQueryParamaterEnhancer, hasDataFilter);
		isEnabledDataFilter = dataFilterValue!=null?Types.convertValue(dataFilterValue, Boolean.class):true;
		if(isEnabledDataFilter){
			/*DataQueryParamaterEnhancer dqpe = query.getEntityClass().getAnnotation(DataQueryParamaterEnhancer.class);
			if(dqpe!=null)
				addParameterByIDataQueryParamterEnhancer(query, dqpe);*/
			addParameterByIDataQueryParamterEnhancer(query);
		}
		
	}
	
	private void addParameterByDataQueryFilter(ExtQueryInner query, DataQueryFilter dataQueryFilter){
		if(dataQueryFilter == null) {
			throw new IllegalArgumentException("dataQueryFilter can not be null");
		}
		String[] fields = dataQueryFilter.fields();
		String[] values = dataQueryFilter.values();
		if(fields.length!=values.length)
			throw new BaseException("the length is not equals of QueryDataFilter");
		int index = 0;
		Map<?, ?> sourceParams = query.getSourceParams();
		for(String field : fields){
			if(sourceParams.containsKey(field)){
				Object val = query.getParams().get(field);
				if(val!=null && StringUtils.isNotBlank(val.toString()))
					return ;
				query.getParams().put(field, values[index++]);
			}else{
				query.getParams().put(field, values[index++]);
			}
		}
	}
	
	private void addParameterByIDataQueryParamterEnhancer(ExtQueryInner query){
//		Assert.notNull(enhancer);
		/*Class<? extends IDataQueryParamterEnhancer> enhancerCls = dqpe.value();

		boolean useSpringContext = Springs.getInstance().isInitialized();
		IDataQueryParamterEnhancer enhancer = useSpringContext?Springs.getInstance().getBean(enhancerCls):null;
		if(enhancer==null){
			enhancer = ReflectUtils.newInstance(enhancerCls);
		}*/
		
		IDataQueryParamterEnhancer enhancer;
		try {
			enhancer = this.queryParamaterEnhancerCaches.get(query.getEntityClass());
		} catch (ExecutionException e) {
			JFishLoggerFactory.getCommonLogger().debug("find IDataQueryParamterEnhancer error on class: " + query.getEntityClass());
			enhancer = NULL_ENHANCER;
		}
		if(enhancer==NULL_ENHANCER){
			return ;
		}
		Map<Object, Object> params = enhancer.enhanceParameters(query);
		if(LangUtils.isEmpty(params))
			return;

		for(Entry<Object, Object> field : params.entrySet()){
			if(query.getParams().containsKey(field.getKey())){
				Object val = query.getParams().get(field.getKey());
				if(val!=null && StringUtils.isNotBlank(val.toString()))
					return ;
				query.getParams().put(field.getKey(), field.getValue());
			}else{
				query.getParams().put(field.getKey(), field.getValue());
			}
		}
		/*Set<String> queryFields = query.getAllParameterFieldNames();
		for(Entry<String, Object> field : params.entrySet()){
			if(queryFields.contains(field.getKey()))
				continue;
			query.getParams().put(field.getKey(), field.getValue());
		}*/
	}

}
