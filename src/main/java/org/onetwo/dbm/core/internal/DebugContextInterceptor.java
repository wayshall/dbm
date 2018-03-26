package org.onetwo.dbm.core.internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.jdbc.spi.DbmInterceptor;
import org.onetwo.dbm.jdbc.spi.DbmInterceptorChain;
import org.onetwo.dbm.jdbc.spi.DbmJdbcOperationType.DatabaseOperationType;
import org.slf4j.Logger;
import org.springframework.core.NamedThreadLocal;

import com.google.common.collect.Lists;

public class DebugContextInterceptor implements DbmInterceptor {
	
	static private NamedThreadLocal<DebugContextData> DebugContext = new NamedThreadLocal<>("DBM-Debuger");
	static private Logger logger = JFishLoggerFactory.getLogger(DebugContextInterceptor.class);
	
	public static NamedThreadLocal<DebugContextData> getDebugContext() {
		return DebugContext;
	}
	
	public static Optional<DebugContextData> getCurrentDebugContextData() {
		return Optional.ofNullable(DebugContext.get());
	}

	private DbmSessionFactory sessionFactory;

	public DebugContextInterceptor(DbmSessionFactory sessionFactory) {
		super();
	}

	@Override
	public Object intercept(DbmInterceptorChain chain) {
		DebugContextData data = DebugContext.get();
		if(data==null){
			data = new DebugContextData();
			DebugContext.set(data);
			if(logger.isInfoEnabled()){
				logger.info("create and set DebugContext.");
			}
		}
		
		Optional<DatabaseOperationType> operationOpt = chain.getDatabaseOperationType();
		InvokeData invoke = new InvokeData(chain.getTargetMethod(), chain.getTargetArgs(), operationOpt);
		data.getInvokeList().add(invoke);
		
		return chain.invoke();
	}
	
	public class DebugContextData {
		private List<Pair<String, Object>> sqlAndParamList = Lists.newArrayList();
		private List<InvokeData> invokeList = Lists.newArrayList();

		public DbmSessionFactory getSessionFactory() {
			return sessionFactory;
		}
		public DebugContextData addSqlAndParams(Pair<String, Object> sqlParams){
			this.sqlAndParamList.add(sqlParams);
			return this;
		}
		public List<Pair<String, Object>> getSqlAndParamList() {
			return sqlAndParamList;
		}

		public Logger getLogger() {
			return logger;
		}
		public List<InvokeData> getInvokeList() {
			return invokeList;
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
	}

}
