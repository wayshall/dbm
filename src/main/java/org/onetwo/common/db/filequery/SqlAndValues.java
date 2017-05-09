package org.onetwo.common.db.filequery;

import java.util.List;
import java.util.Map;

import org.onetwo.common.db.ParsedSqlContext;

public class SqlAndValues implements ParsedSqlContext {
	private String parsedSql;
	final private Object values;
	final private boolean namedValue;
	
	public SqlAndValues(boolean namedValue, String parsedSql, Object values) {
		super();
		this.namedValue = namedValue;
		this.parsedSql = parsedSql;
		this.values = values;
	}
	

	@Override
	public Map<String, Object> asMap() {
		return getValues();
	}

	@Override
	public List<Object> asList() {
		return getValues();
	}
	
	@Override
	public boolean isListValue(){
		return !this.namedValue;
	}
	
	@Override
	public boolean isMapValue(){
		return this.namedValue;
	}
	
	/*********
	 * List Or Map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValues(){
		return (T) values;
	}

	@Override
	public String getParsedSql() {
		return parsedSql;
	}

	public void setParsedSql(String parsedSql) {
		this.parsedSql = parsedSql;
	}

	/*@Override
	public QueryConfigData getQueryConfig() {
		return queryConfig==null?ParsedSqlUtils.EMPTY_CONFIG:queryConfig;
	}
	*/
}
