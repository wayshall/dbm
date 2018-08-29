package org.onetwo.dbm.event.spi;

import java.io.Serializable;

/**
 * @author wayshall
 * <br/>
 */

@SuppressWarnings("serial")
public class SqlExecutedEvent implements Serializable {
	private final String sql;
	private final Object args;
	private final long startTime = System.currentTimeMillis();
	private long endTime;
	
	public SqlExecutedEvent(String sql, Object args) {
		super();
		this.sql = sql;
		this.args = args;
	}
	public void finish(){
		this.endTime = System.currentTimeMillis();
	}
	public String getSql() {
		return sql;
	}
	public Object getArgs() {
		return args;
	}
	public long getStartTime() {
		return startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	
}
