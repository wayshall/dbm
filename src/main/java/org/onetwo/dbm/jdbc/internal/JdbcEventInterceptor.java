package org.onetwo.dbm.jdbc.internal;

import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.dbm.annotation.DbmInterceptorFilter;
import org.onetwo.dbm.annotation.DbmInterceptorFilter.InterceptorType;
import org.onetwo.dbm.core.internal.DbmInterceptorOrder;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.event.internal.EdgeEventBus;
import org.onetwo.dbm.event.spi.SqlExecutedEvent;
import org.onetwo.dbm.jdbc.spi.DbmJdbcOperations;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.core.Ordered;

/**
 * @see DbmJdbcOperations
 * @author wayshall
 * <br/>
 */
@DbmInterceptorFilter(type=InterceptorType.JDBC)
public class JdbcEventInterceptor implements DbmInterceptor, Ordered {
	
	final private static Logger logger = JFishLoggerFactory.getLogger(JdbcEventInterceptor.class);
	final private EdgeEventBus edgeEventBus;
	
	public JdbcEventInterceptor(EdgeEventBus edgeEventBus) {
		this.edgeEventBus = edgeEventBus;
	}

	@Override
	public Object intercept(DbmInterceptorChain chain) {
		Object[] args = chain.getTargetArgs();
		Pair<String, Object> sqlParams = DbmUtils.findSqlAndParams(args);
		if(sqlParams==null){
			if(logger.isWarnEnabled()){
				logger.warn("this operation can not found sql and args, method: {}", chain.getTargetMethod().getName());
			}
			return chain.invoke();
		}
		
		SqlExecutedEvent event = new SqlExecutedEvent(sqlParams.getKey(), sqlParams.getValue());
		try {
			return chain.invoke();
		}finally{
			event.finish();
			edgeEventBus.post(event);
		}
	}

	@Override
	public int getOrder() {
		return DbmInterceptorOrder.JDBC_EVENT;
	}
	
}
