package org.onetwo.common.db.sqlext;

import java.util.HashSet;
import java.util.Set;

import org.onetwo.common.exception.ServiceException;



/***
 * 
 * @author weishao
 *
 */
public class LikeSQLSymbolParser extends CommonSQLSymbolParser {
	
	@SuppressWarnings("serial")
	private static Set<QueryDSLOps> SYMBOL_SET = new HashSet<QueryDSLOps>(){
		{
			add(QueryDSLOps.LIKE);
			add(QueryDSLOps.NOT_LIKE);
			add(QueryDSLOps.LIKE2);
			add(QueryDSLOps.NOT_LIKE2);
		}
	};
	

	LikeSQLSymbolParser(SQLSymbolManager sqlSymbolManager, QueryDSLOps mappedOperator) {
		super(sqlSymbolManager, mappedOperator);
		if(!SYMBOL_SET.contains(mappedOperator)){
			throw new IllegalArgumentException("only support : " + SYMBOL_SET.toString());
		}
	}

	protected void process(String field, String symbol, int index, Object value, StringBuilder sqlScript, ParamValues paramValues){
		if(value!=null){
			if(!(value instanceof String))
				throw new ServiceException("the symbol is [like], the value must a string type, but " + value);
			value = ExtQueryUtils.getLikeString(value.toString());
		}
		super.process(field, symbol, index, value, sqlScript, paramValues);
	}

}
