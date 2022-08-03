package org.onetwo.dbm.core.internal;

import java.util.Optional;

import org.springframework.core.NamedThreadLocal;

public class DbmThreadLocal {

	private static final ThreadLocal<DbmThreadContext> THREAD_CONTEXT_HOLDER = new NamedThreadLocal<>("dbmThreadContext");
	
	
	public static Optional<DbmThreadContext> get(){
		return Optional.ofNullable(THREAD_CONTEXT_HOLDER.get());
	}

	
	public static DbmThreadContext initContext(boolean autoClean){
		DbmThreadContext ctx = new DbmThreadContext(autoClean);
		ctx.setLogSql(true);
		THREAD_CONTEXT_HOLDER.set(ctx);
		return ctx;
	}
	

	public static DbmThreadContext getOrInitContext(boolean autoClean){
		return get().orElseGet(() -> {
			return initContext(autoClean);
		});
	}
	
	public static DbmThreadContext logSql(boolean autoClean, boolean logSql){
		DbmThreadContext ctx = getOrInitContext(autoClean);
		ctx.setLogSql(logSql);
		return ctx;
	}
	
	public static void reset(){
		THREAD_CONTEXT_HOLDER.remove();
	}

	
	public static  class DbmThreadContext {
		/***
		 * 是否自动清除，避免内存泄漏
		 */
		final private boolean autoClean;
		private boolean logSql;

		public DbmThreadContext(boolean autoClean) {
			super();
			this.autoClean = autoClean;
		}
		public boolean isLogSql() {
			return logSql;
		}
		public void setLogSql(boolean logSql) {
			this.logSql = logSql;
		}
		public boolean isAutoClean() {
			return autoClean;
		}
	}
	

}
