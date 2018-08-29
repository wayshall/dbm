package org.onetwo.dbm.stat;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.onetwo.common.md.Hashs;
import org.onetwo.dbm.annotation.DbmEdgeEventListener;
import org.onetwo.dbm.event.spi.SqlExecutedEvent;
import org.onetwo.dbm.exception.DbmException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;


/**
 * @author wayshall
 * <br/>
 */
@DbmEdgeEventListener
public class SqlExecutedStatis {
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private Cache<String, StatInfo> statInfoCache = CacheBuilder.newBuilder()
																.maximumSize(256)
																.build();
	
	@Subscribe
	public void onSqlExecuted(SqlExecutedEvent event){
		executor.submit(()->{
			statis(event);
		});
	}
	
	protected void statis(SqlExecutedEvent event){
		String key = generateKey(event);
		StatInfo stat = getStatInfo(key, event);
		stat.setExecutedCount(stat.getExecutedCount()+1);
		int maxExecutedTime = Math.max(event.getExecutedTime(), stat.getMaxExecutedTime());
		stat.setMaxExecutedTime(maxExecutedTime);
	}
	
	protected StatInfo getStatInfo(String key, SqlExecutedEvent event){
		try {
			return statInfoCache.get(key, ()->{
				return new StatInfo(event.getSourceShortName(), event.getSql());
			});	
		} catch (ExecutionException e) {
			throw new DbmException("get statinfo error", e);
		}
	}
	
	protected String generateKey(SqlExecutedEvent event){
		return Hashs.MD5.hash(event.getSql().trim());
	}
	
	public Collection<StatInfo> getStatInfos(){
		return this.statInfoCache.asMap().values();
	}
	
	public String toFormatedString() {
		StringBuilder log = new StringBuilder(1024);

		log.append("\n");
		Collection<StatInfo> stats = getStatInfos();
//		int maxLength = stats.stream().mapToInt(s->s.getName().length()).max().orElse(0);
//		String title = String.format("%-"+maxLength+"s|%13s|%15s|%50s\n", "name", "executedCount", "maxExecutedTime", "sql");
		String title = String.format("%13s|%15s|%50s\n", "executedCount", "maxExecutedTime", "sql");
		log.append(title);
		printSepLine(log, title);
		for(StatInfo stat : stats){
//			log.append(String.format("%-"+maxLength+"s", stat.getName())).append("|");
			log.append(String.format("%13d", stat.getExecutedCount())).append("|");
			log.append(String.format("%15d", stat.getMaxExecutedTime())).append("|");
			log.append(String.format("%-50s", stat.getSql()));
			log.append("\n");
		}
		printSepLine(log, title);
		
		return log.toString();
	}

    private void printSepLine(StringBuilder sb, String title) {
        title.chars().forEach((c) -> {
            if (c == '|') {
                sb.append('+');
            } else {
                sb.append('-');
            }
        });
        sb.append('\n');
    }


	public static class StatInfo {
		final private String name;
		final private String sql;
		private long executedCount = 0;
		private int maxExecutedTime = 0;

		public StatInfo(String name, String sql) {
			super();
			this.name = name;
			this.sql = sql;
		}

		public String getName() {
			return name;
		}

		public String getSql() {
			return sql;
		}

		public long getExecutedCount() {
			return executedCount;
		}

		public void setExecutedCount(long executedCount) {
			this.executedCount = executedCount;
		}

		public int getMaxExecutedTime() {
			return maxExecutedTime;
		}

		public void setMaxExecutedTime(int maxExecutedTime) {
			this.maxExecutedTime = maxExecutedTime;
		}
		
	}

}
