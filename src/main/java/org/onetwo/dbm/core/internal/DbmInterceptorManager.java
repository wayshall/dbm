package org.onetwo.dbm.core.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.onetwo.common.annotation.AnnotationUtils;
import org.onetwo.common.utils.map.CollectionMap;
import org.onetwo.dbm.annotation.DbmInterceptorFilter;
import org.onetwo.dbm.annotation.DbmInterceptorFilter.InterceptorType;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;

import com.google.common.collect.ImmutableList;

@Order(value=Ordered.HIGHEST_PRECEDENCE)
public class DbmInterceptorManager implements InitializingBean {
	
//	@Autowired(required=false)
	private List<DbmInterceptor> interceptors;
	private CollectionMap<InterceptorType, DbmInterceptor> typeInterceptors = CollectionMap.newLinkedListMap();

	@Override
	public void afterPropertiesSet() {
//		this.dbmInterceptors = SpringUtils.getBeans(applicationContext, DbmInterceptor.class);
		List<DbmInterceptor> interceptors = this.interceptors;
		if(interceptors==null){
			this.interceptors = Collections.emptyList();
			return ;
		}
		
		AnnotationAwareOrderComparator.sort(interceptors);
		this.interceptors = ImmutableList.copyOf(interceptors);
		
		for(DbmInterceptor interceptor : this.interceptors){
			DbmInterceptorFilter filter = AnnotationUtils.findAnnotationWithSupers(interceptor.getClass(), DbmInterceptorFilter.class);
			InterceptorType[] types = null;
			if(filter==null){
				types = InterceptorType.values();
			}else{
				types = filter.type();
			}
			Stream.of(types).forEach(type->typeInterceptors.putElement(type, interceptor));
		}
	}
	
	public Collection<DbmInterceptor> getInterceptors(InterceptorType type){
		Collection<DbmInterceptor> inters = typeInterceptors.get(type);
		if(inters==null){
			inters = ImmutableList.of();
		}
		return inters;
	}

	public void setInterceptors(List<DbmInterceptor> interceptors) {
		this.interceptors = interceptors;
	}
	
}
