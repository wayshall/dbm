package org.onetwo.common.db.sqlext;

import org.onetwo.dbm.exception.DbmException;


/***
 * 可用于解释一般的操作符，如=,<,> ……
 * @author weishao
 *
 */
public class CommonSQLSymbolParser extends AbstractSupportedSubQuerySQLSymbolParser {
	
//	public static final String LIKE = "like";
	
//	protected SQLSymbolManager sqlSymbolManager;
//	protected String symbolAlias;
//	private String mappedSymbol;
	
//	protected boolean like;

	CommonSQLSymbolParser(SQLSymbolManager sqlSymbolManager, QueryDSLOps symbol){
		super(sqlSymbolManager, symbol);
	}

	/*CommonSQLSymbolParser(SQLSymbolManager sqlSymbolManager, String symbol, boolean like){
		this(sqlSymbolManager, symbol, symbol);
		this.like = like;
	}*/
	
	/*CommonSQLSymbolParser(SQLSymbolManager sqlSymbolManager, String symbol, String symbolDesc){
		super(sqlSymbolManager, symbol);
		this.symbolAlias = symbolDesc;
	}*/
	
	
	protected void processKey(String field, String op, SQLKeys key, StringBuilder hql){
		if (SQLKeys.Null==key) {
			QueryDSLOps symbol = QueryDSLOps.operatorOf(op);
			if (QueryDSLOps.EQ.equals(symbol)) {
				hql.append(field).append(" is null ");
			} else if (QueryDSLOps.NEQ.equals(symbol) || QueryDSLOps.NEQ2.equals(symbol)) {
				hql.append(field).append(" is not null ");
			} else {
				throw new DbmException("unsupported symbol: " + symbol);
			}
		}
	}
	
	protected void process(String field, String symbol, int index, Object value, StringBuilder sqlScript, ParamValues paramValues){
		if(value instanceof SQLKeys){
			SQLKeys key = (SQLKeys) value;
			this.processKey(field, symbol, key, sqlScript);
		}else{
			super.process(field, symbol, index, value, sqlScript, paramValues);
		}
	}

	/*protected void processKey(String field, SQLKeys key, StringBuilder hql){
		hql.append(field).append(" ").append(symbolAlias).append(" ").append(key.getValue()).append(" ");
	}*/

}
