package org.onetwo.dbm.core.internal;

import org.springframework.core.Ordered;
import org.onetwo.common.utils.Assert;

/**
 * @author wayshall
 * <br/>
 */
public class DbmInterceptorOrder {

	private static final int FIRST = Ordered.HIGHEST_PRECEDENCE;
	private static final int LAST = Ordered.LOWEST_PRECEDENCE;//Integer.MAX_VALUE
	private static final int INCREMENTAL = 10;
	

	public static final int DEBUG = FIRST;
	public static final int SESSION_CACHE = after(DEBUG);
	public static final int LOG_SQL = after(SESSION_CACHE);
	public static final int JDBC_EVENT = after(LOG_SQL);
	

	public static int after(int order){
		int limit = LAST - INCREMENTAL;//防止溢出
		Assert.isTrue(order < limit, "error order: " + order);
		return order + INCREMENTAL;
	}
	
	public static int before(int order){
		int limit = FIRST+INCREMENTAL;
		Assert.isTrue(order > limit, "error order: " + order);
		return order - INCREMENTAL;
	}

}
