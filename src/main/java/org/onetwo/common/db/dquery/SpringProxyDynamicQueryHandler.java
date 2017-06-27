package org.onetwo.common.db.dquery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.spring.aop.MixinableInterfaceCreator;
import org.onetwo.common.spring.aop.SpringMixinableInterfaceCreator;

import com.google.common.cache.LoadingCache;

public class SpringProxyDynamicQueryHandler extends AbstractDynamicQueryHandler implements MethodInterceptor {

	private Object proxyObject;
	final private MixinableInterfaceCreator mixinableInterfaceCreator;
	final protected List<Class<?>> mixinInterfaces = new ArrayList<Class<?>>();
	
	public SpringProxyDynamicQueryHandler(QueryProvideManager em, LoadingCache<Method, DynamicMethod> methodCache, Class<?>... proxiedInterfaces){
		super(em, methodCache, proxiedInterfaces);
		mixinableInterfaceCreator = SpringMixinableInterfaceCreator.classNamePostfixMixin(proxiedInterfaces);
	}
	
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		return invoke(invocation.getThis(), invocation.getMethod(), invocation.getArguments());
	}

	public Object getQueryObject(){
		Object qb = this.proxyObject;
		if(qb==null){
			qb = mixinableInterfaceCreator.createMixinObject(this);
			this.proxyObject = qb;
		}
		return qb;
	}

}
