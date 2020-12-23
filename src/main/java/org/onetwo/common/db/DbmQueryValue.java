package org.onetwo.common.db;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.onetwo.common.db.sqlext.ExtQueryUtils;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.utils.JdbcParamValueConvers;


public class DbmQueryValue {

	public static DbmQueryValue create(String sql){
		return new DbmQueryValue(sql);
	}
	
	private Map<String, Object> values;
	private Class<?> resultClass;
	private String sql;
	private String countSql;
	
//	private Integer firstResult = 0; 
//	private Integer maxResults = -1;
	
	private DbmQueryValue(String sql){
		this.values = new LinkedHashMap<>();
		this.sql = sql;
	}

//	public boolean isLimitQuery(){
//		return SelectExtQueryImpl.isLimitQuery(this.firstResult, maxResults);
//	}

	public DbmQueryValue setValue(int index, Object value){
		Map<String, Object> map = this.values;
		map.put(String.valueOf(index), convertValue(value));
		return this;
	}
	

	private Object convertValue(Object value){
		return JdbcParamValueConvers.getActualValue(value);
//		return ExtQueryUtils.getNameIfEnum(value, value);
	}
	
	public DbmQueryValue setValue(Map<String, Object> parameters){
		parameters.forEach((k,v)-> setValue(k, v));
		return this;
	}
	
	public DbmQueryValue setValue(List<?> parametsrs){
		Map<String, Object> parameters = CUtils.toMap(parametsrs, (e, index)->String.valueOf(index));
		this.setValue(parameters);
		return this;
	}
	
	public DbmQueryValue setValue(String field, Object value){
		values.put(field, convertValue(value));
		return this;
	}
	
	public Map<String, Object> asMap() {
		return getValues();
	}
	
	/*********
	 * @return
	 */
	public Map<String, Object> getValues(){
		return Collections.unmodifiableMap(values);
	}

	/*public PlaceHolder getHolder() {
		return holder;
	}*/

	public Class<?> getResultClass() {
		return resultClass;
	}

	public void setResultClass(Class<?> resultClass) {
		this.resultClass = resultClass;
	}

//	public Integer getFirstResult() {
//		return firstResult;
//	}
//
//	public void setLimit(Integer firstResult, Integer maxResult) {
//		this.firstResult = firstResult;
//		this.maxResults = maxResult;
//	}
//
//	public Integer getMaxResults() {
//		return maxResults;
//	}


	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public String getCountSql() {
		if(StringUtils.isBlank(countSql)){
			this.countSql = ExtQueryUtils.buildCountSql(this.sql, "");
		}
		return countSql;
	}
	public void setCountSql(String countSql) {
		this.countSql = countSql;
	}
}
