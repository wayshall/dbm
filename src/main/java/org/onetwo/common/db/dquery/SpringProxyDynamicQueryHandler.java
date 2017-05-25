package org.onetwo.common.db.dquery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.spring.aop.ClassNamePostfixMixinAdvisorFactory;
import org.onetwo.common.spring.aop.MixinFactory;
import org.onetwo.common.spring.aop.Proxys;

import com.google.common.cache.LoadingCache;

public class SpringProxyDynamicQueryHandler extends AbstractDynamicQueryHandler implements MethodInterceptor {

	final private MixinFactory mixinFactory;
	private Object proxyObject;
	final protected List<Class<?>> mixinInterfaces = new ArrayList<Class<?>>();
	
	public SpringProxyDynamicQueryHandler(QueryProvideManager em, LoadingCache<Method, DynamicMethod> methodCache, Class<?>... proxiedInterfaces){
		super(em, methodCache, proxiedInterfaces);
		mixinFactory = new MixinFactory();
		mixinFactory.setAdvisorFactory(new ClassNamePostfixMixinAdvisorFactory());
		analyseProxyInterfaces();
	}
	
	private void analyseProxyInterfaces(){
		for(Class<?> inter : this.proxyInterfaces){
			analyseInterface(inter);
		}
	}
	private void analyseInterface(Class<?> interfaceClass){
		Class<?>[] interfaces = interfaceClass.getInterfaces();
		for(Class<?> inter : interfaces){
			if(mixinFactory.isMixinInterface(inter)){
				this.mixinInterfaces.add(inter);
			}else{
				this.proxyInterfaces.add(inter);
			}
			analyseInterface(inter);
		}
	}
	
	final public void addMixinInterfaces(Class<?>...mixinInterfaces){
		for(Class<?> inter : mixinInterfaces){
			this.mixinInterfaces.add(inter);
		}
	}
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		return invoke(invocation.getThis(), invocation.getMethod(), invocation.getArguments());
	}

	public Object getQueryObject(){
		Object qb = this.proxyObject;
		if(qb==null){
			qb = Proxys.ofInterfaces(proxyInterfaces, this);
			
			if(!mixinInterfaces.isEmpty()){
				qb = mixinFactory.of(qb, mixinInterfaces.toArray(new Class<?>[0]));
			}
			this.proxyObject = qb;
		}
		return qb;
	}

}
