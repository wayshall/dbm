package org.onetwo.dbm.core.internal;

import java.lang.reflect.Method;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.jdbc.spi.DbmJdbcOperationType.DatabaseOperationType;
import org.slf4j.Logger;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.Ordered;

import com.google.common.collect.EvictingQueue;

public class DebugContextInterceptor implements DbmInterceptor, Ordered {
	
	static private NamedThreadLocal<DebugContextData> DebugContext = new NamedThreadLocal<>("DBM-Debuger");
	static private Logger logger = JFishLoggerFactory.getLogger(DebugContextInterceptor.class);
	
	public static NamedThreadLocal<DebugContextData> getDebugContext() {
		return DebugContext;
	}
	
	public static Optional<DebugContextData> getCurrentDebugContextData() {
		return Optional.ofNullable(DebugContext.get());
	}
	
	public static DebugContextData init() {
		DebugContextData data = new DebugContextData();
//		data.setLogSql(isLogSql);
		DebugContext.set(data);
		return data;
	}

//	private DbmSessionFactory sessionFactory;

	public DebugContextInterceptor(DbmSessionFactory sessionFactory) {
		super();
//		this.sessionFactory = sessionFactory;
	}

	@Override
	public Object intercept(DbmInterceptorChain chain) {
		DebugContextData data = DebugContext.get();
		if(data==null){
			data = init();
			if(logger.isInfoEnabled()){
				logger.info("create and set DebugContext.");
			}
		}
		
		Optional<DatabaseOperationType> operationOpt = chain.getDatabaseOperationType();
		InvokeData invoke = new InvokeData(chain.getTargetMethod(), chain.getTargetArgs(), operationOpt);
		data.getInvokeList().add(invoke);
		
		return chain.invoke();
	}
	

	@Override
	public int getOrder() {
		return DbmInterceptorOrder.DEBUG;
	}
	
	static public class DebugContextData {
		private EvictingQueue<Pair<String, Object>> sqlAndParamList = EvictingQueue.create(256);
		private EvictingQueue<InvokeData> invokeList = EvictingQueue.create(256);
		private boolean logSql = true;

//		public DbmSessionFactory getSessionFactory() {
//			return sessionFactory;
//		}
		public DebugContextData addSqlAndParams(Pair<String, Object> sqlParams){
			this.sqlAndParamList.add(sqlParams);
			return this;
		}
		public EvictingQueue<Pair<String, Object>> getSqlAndParamList() {
			return sqlAndParamList;
		}

		public Logger getLogger() {
			return logger;
		}
		public EvictingQueue<InvokeData> getInvokeList() {
			return invokeList;
		}
		public boolean isLogSql() {
			return logSql;
		}
		public void setLogSql(boolean logSql) {
			this.logSql = logSql;
		}
		
	}
	
	public class InvokeData {
		private final Method method;
		private final Object[] args;
		private final Optional<DatabaseOperationType> dbOperation;
		public InvokeData(Method method, Object[] args,
				Optional<DatabaseOperationType> dbOperation) {
			super();
			this.method = method;
			this.args = args;
			this.dbOperation = dbOperation;
		}
		public Method getMethod() {
			return method;
		}
		public Object[] getArgs() {
			return args;
		}
		public Optional<DatabaseOperationType> getDbOperation() {
			return dbOperation;
		}
		public String toString() {
			return dbOperation.map(op -> op.name()).orElse("no db Operation") + 
						": " + method.toGenericString() + ", args: " + StringUtils.join(args, ",");
		}
	}

}
