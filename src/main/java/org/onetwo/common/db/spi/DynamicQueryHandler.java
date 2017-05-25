package org.onetwo.common.db.spi;

import java.lang.reflect.Method;

/**
 * @author wayshall
 * <br/>
 */
public interface DynamicQueryHandler {

	//	@Override
	Object invoke(Object proxy, Method method, Object[] args);

	Object getQueryObject();

}