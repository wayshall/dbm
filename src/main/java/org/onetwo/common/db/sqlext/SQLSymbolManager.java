package org.onetwo.common.db.sqlext;

import java.util.List;
import java.util.Map;

/***
 * sql操作符管理
 * 
 * @author weishao
 *
 */
public interface SQLSymbolManager {
	
	/***
	 * 用 QueryDSLOperators 代替这些操作符常量
	 * @author way
	 
	@Deprecated
	public static class FieldOP {
		public static final char SPLIT_SYMBOL = QueryField.SPLIT_SYMBOL;
		public static final String like = "like";
		public static final String like2 = "=~";
		public static final String not_like = "not like";
		public static final String not_like2 = "!=~";
		public static final String eq = "=";
		public static final String gt = ">";
		public static final String ge = ">=";
		public static final String lt = "<";
		public static final String le = "<=";
		public static final String neq = "!=";
		public static final String neq2 = "<>";
		public static final String in = "in";
		public static final String between = "between";
		public static final String not_in = "not in";
		public static final String date_in = "date in";
		public static final String is_null = "is null";

		public static final String in(String name){
			return qstr(name, in);
		}
		public static final String notIn(String name){
			return qstr(name, not_in);
		}
		public static final String dateIn(String name){
			return qstr(name, date_in);
		}
		public static final String qstr(String name, String op){
			return name + QueryField.SPLIT_SYMBOL + op;
		}
	}*
	 */

	ExtQueryDialet getSqlDialet();

	SQLSymbolManager register(QueryDSLOps symbol, HqlSymbolParser parser);
	SQLSymbolManager register(HqlSymbolParser parser);
	
	HqlSymbolParser getHqlSymbolParser(String symbol);
	HqlSymbolParser getHqlSymbolParser(QueryDSLOps symbol);
//	public String createHql(Map<Object, Object> properties, List<Object> values) ;
//	public PlaceHolder getPlaceHolder();
	
	ExtQueryInner createDeleteQuery(Class<?> entityClass, Map<Object, Object> properties);
	
	SelectExtQuery createSelectQuery(Class<?> entityClass, Map<Object, Object> properties);
	SelectExtQuery createSelectQuery(Class<?> entityClass, String alias, Map<Object, Object> properties);
	
	void setListeners(List<ExtQueryListener> listeners);
	
}
