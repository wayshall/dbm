package org.onetwo.dbm.core.internal;

import java.util.Optional;

import org.springframework.core.NamedThreadLocal;

public class DbmThreadLocal {

	private static final ThreadLocal<DbmThreadContext> THREAD_CONTEXT_HOLDER = new NamedThreadLocal<>("dbmThreadContext");
	
	
	public static Optional<DbmThreadContext> get(){
		return Optional.ofNullable(THREAD_CONTEXT_HOLDER.get());
	}

	
	public static DbmThreadContext initContext(boolean logSql){
		DbmThreadContext ctx = new DbmThreadContext();
		ctx.setLogSql(logSql);
		THREAD_CONTEXT_HOLDER.set(ctx);
		return ctx;
	}
	

	public static DbmThreadContext getOrInitContext(boolean logSql){
		return get().orElseGet(() -> {
			return initContext(logSql);
		});
	}
	


	public static DbmThreadContext logSql(boolean logSql){
		DbmThreadContext ctx = getOrInitContext(logSql);
		ctx.setLogSql(logSql);
		return ctx;
	}
	
	public static void reset(){
		THREAD_CONTEXT_HOLDER.remove();
	}

	
	public static  class DbmThreadContext {
		private boolean logSql;

		public boolean isLogSql() {
			return logSql;
		}
		public void setLogSql(boolean logSql) {
			this.logSql = logSql;
		}
	}
	

}
