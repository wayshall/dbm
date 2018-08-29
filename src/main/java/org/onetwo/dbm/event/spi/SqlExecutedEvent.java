package org.onetwo.dbm.event.spi;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author wayshall
 * <br/>
 */

@SuppressWarnings("serial")
public class SqlExecutedEvent implements Serializable {
	private Method source;
	private final String sql;
	private final Object args;
	private final long startTime = System.currentTimeMillis();
	private long endTime;
	
	public SqlExecutedEvent(Method source, String sql, Object args) {
		super();
		this.source = source;
		this.sql = sql;
		this.args = args;
	}
	public void finish(){
		this.endTime = System.currentTimeMillis();
	}
	public String getSourceShortName(){
		return source.getDeclaringClass().getSimpleName()+"."+source.getName();
	}
	public int getExecutedTime(){
		return (int)(endTime - startTime);
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
	public Method getSource() {
		return source;
	}
	
}
