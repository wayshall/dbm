package org.onetwo.common.db.sqlext;

import java.util.List;

import org.onetwo.common.db.builder.QueryField;
import org.onetwo.common.db.sqlext.ExtQuery.K.IfNull;
import org.onetwo.common.utils.LangUtils;

/****
 * 对in操作符的解释
 * 
 * @author weishao
 *
 */
public class InSymbolParser extends CommonSQLSymbolParser implements HqlSymbolParser {
	
	public InSymbolParser(SQLSymbolManager sqlSymbolManager, String symbol){
		super(sqlSymbolManager, symbol);
	}
	
	@SuppressWarnings("rawtypes")
	public String parse(String symbol, QueryField context){

		String field = context.getActualFieldName();
		Object value = context.getValue();
		ParamValues paramValues = context.getExtQuery().getParamsValue();
		IfNull ifNull = getIfNull(context);
		
		/*if(value==null || (value instanceof String && StringUtils.isBlank(value.toString())))
			return null;
		
		List paramlist = MyUtils.asList(value);
		if(ExtQueryUtils.isContinueByCauseValue(paramlist, ifNull)){
			return null;
		}*/

		List paramlist = convertValues(field, value, ifNull);
		if (LangUtils.isEmpty(paramlist)) {
			return null;
		}

		field = this.getFieldName(field);
		StringBuilder hql = new StringBuilder();

		if(!this.subQuery(field, symbol, paramlist, paramValues, hql)){
			hql.append(field).append(" ").append(symbol).append(" ( ");
			for(int i=0; i<paramlist.size(); i++){
				paramValues.addValue(field, paramlist.get(i), hql);//第二个参数value改成paramlist.get(i) mrs
				if(i!=paramlist.size()-1)
					hql.append(", ");
			}
			hql.append(") ");
		}
		
		return hql.toString();
	}
	
}
