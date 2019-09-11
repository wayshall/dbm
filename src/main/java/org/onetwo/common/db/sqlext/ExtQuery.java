package org.onetwo.common.db.sqlext;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.onetwo.common.db.sqlext.ExtQuery.K.IfNull;

import lombok.Builder;
import lombok.Data;

public interface ExtQuery {
	
	public static class Msg {
		public static final String THROW_IF_NULL_MSG = "the case value can not be null!";
	}
	@Data
	public class KeyObject {
		final private String key;
		@Builder
		private KeyObject(String key) {
			super();
			this.key = key;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			KeyObject other = (KeyObject) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}
	}
	
	/****
	 * key object set
	 * @author way
	 *
	 */
	final public static class K {
		public static enum IfNull {
			Calm,//not throw , not ignore
			Throw,
			Ignore
		}
		static final Map<Object, Object> ORDER_BY_MAP;
		static final Map<Object, String> JOIN_MAP;

//		public static final String SQL_QUERY = ":sql-query";

		public static final String NO_PREFIX = ".";//不会加 "ent."

		public static final String FUNC = "&";
//		public static final String RAW_FUNC = "#";// raw sql function
		public static final String PREFIX_REF = "@";//加上 "ent."

		
		//FIRST_RESULT，ASC等key因为之前比较常用，先不重构为KeyObject，保持为字符串类型
		/***
		 *  from 0
		 */
		public static final Object FIRST_RESULT = KeyObject.builder().key(":firstResult").build(); // ":firstResult";
		public static final Object MAX_RESULTS = KeyObject.builder().key(":maxResults").build(); // ":maxResults";
		public static final Object OR = KeyObject.builder().key(":or").build(); // ":or";
		public static final Object AND = KeyObject.builder().key(":and").build(); //":and";
		public static final Object ASC = KeyObject.builder().key(":asc").build(); // ":asc";
		public static final Object DESC = KeyObject.builder().key(":desc").build(); // ":desc";
//		public static final String RAW_QL = ":raw-ql";

		public static final Object QUERY_CONFIG = KeyObject.builder().key(":query_tips").build();//":query_config";
		public static final Object DEBUG = KeyObject.builder().key(":debug").build(); //"_EXTQUERY_DEBUG_NAME_KEY";
		public static final Object IF_NULL = KeyObject.builder().key(":if-null").build(); //":if-null";
		public static final Object SELECT = KeyObject.builder().key(":select").build();//":select";
		public static final Object FOR_UPDATE = KeyObject.builder().key(":for_update").build(); //":for_update";
		public static final Object UNSELECT = KeyObject.builder().key(":unselect").build(); //":unselect";
		public static final Object SQL_SELECT = KeyObject.builder().key(":sql-select").build(); //":sql-select";//value is RawSqlWrapper
		public static final Object DISTINCT = KeyObject.builder().key(":distinct").build(); //":distinct";
		public static final Object COUNT = KeyObject.builder().key(":count").build(); //":count";
//		public static final String CACHEABLE = ":cacheable";//是否缓存查询对象，避免重复解释，暂时没实现
		
		public static final Object ORDERBY = KeyObject.builder().key(":orderBy").build();//":orderBy";

		public static final Object DATA_FILTER = KeyObject.builder().key(":dataFilter").build();//":dataFilter";
//		public static final String INCLUDE = ":include";
		
//		public static final String SQL_JOIN = ":sql-join";//value is RawSqlWrapper
		public static final Object SQL_JOIN = KeyObject.builder().key(":sql-join").build(); //value is RawSqlWrapper
		public static final Object JOIN_IN = KeyObject.builder().key(":join-in").build(); // ":join-in"; //hql的join in写法
		public static final Object FETCH = KeyObject.builder().key(":fetch").build(); //":fetch";//put(":fetch", "obj1") hql的left join fetch obj1写法 
		public static final Object LEFT_JOIN_FETCH = KeyObject.builder().key(":left-join-fetch").build(); //":left-join-fetch";// put(":left-join-fetch", "obj1") hql的left join fetch obj1写法 
		public static final Object JOIN_FETCH = KeyObject.builder().key(":join-fetch").build(); //":join-fetch";//i hql join fetch 写法
		public static final Object JOIN = KeyObject.builder().key(":join").build(); //":join"; // hql join写法
		public static final Object LEFT_JOIN = KeyObject.builder().key(":left-join").build(); //":left-join";//o
		
		/****
		 * 是否触发监听器
		 */
		public static final Object LISTENERS = KeyObject.builder().key(":listeners").build(); //":listeners";
		
		static{
			Map<Object, Object> temp = new HashMap<>();
			temp.put(ASC, " asc");
			temp.put(DESC, " desc");
			temp.put(ORDERBY, "");
			ORDER_BY_MAP = Collections.unmodifiableMap(temp);

			Map<Object, String> joinTemp = new LinkedHashMap<>();
			joinTemp.put(FETCH, "left join fetch");
			joinTemp.put(LEFT_JOIN_FETCH, "left join fetch");
			joinTemp.put(JOIN_FETCH, "join fetch");
			joinTemp.put(JOIN_IN, "in");
			joinTemp.put(JOIN, "join");
			joinTemp.put(LEFT_JOIN, "left join");
			joinTemp.put(SQL_JOIN, "");
			JOIN_MAP = Collections.unmodifiableMap(joinTemp);
		}
		
		public static Object getMappedValue(Object key){
			return getMappedValue(key, null);
		}
		
		public static Object getMappedValue(Object key, Object def){
			Object val = ORDER_BY_MAP.get(key);
			if(val==null)
				val = def;
			return val;
		}
		
		private K(){}
		
	}
	
	
	public boolean hasBuilt();



//	public void setParams(Map params);
	
	

	public ParamValues getParamsValue();
	
	public IfNull getIfNull();

//	public List getValues();

	public String getSql();

//	public StringBuilder getWhere();

	public Class<?> getEntityClass();
//	public boolean isSqlQuery();
	

	public Map<?, ?> getSourceParams();
	public Map<Object, Object> getParams();

}