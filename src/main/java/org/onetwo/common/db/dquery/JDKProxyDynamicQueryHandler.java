package org.onetwo.common.db.dquery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.utils.ClassUtils;

import com.google.common.cache.LoadingCache;

public class JDKProxyDynamicQueryHandler extends AbstractDynamicQueryHandler implements InvocationHandler {
	
	private Object proxyObject;
	
	public JDKProxyDynamicQueryHandler(QueryProvideManager em, LoadingCache<Method, DynamicMethod> methodCache, Class<?>... proxiedInterfaces){
		super(em, methodCache, proxiedInterfaces);
	}
	
	public Object getQueryObject(){
		Object qb = this.proxyObject;
		if(qb==null){
			qb = Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), proxiedInterfaces, this);
			this.proxyObject = qb;
		}
		return qb;
	}

}
