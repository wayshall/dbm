package org.onetwo.common.db.sqlext;

import java.util.List;

import org.onetwo.common.db.builder.QueryField;
import org.onetwo.common.db.sqlext.ExtQuery.K.IfNull;
import org.onetwo.dbm.exception.DbmException;

/****
 * 对between操作符的解释
 * 解释为sql的between start and end
 * 是否包含边界值需要根据数据库来确定
 * @author weishao
 *
 */
public class BetweenSymbolParser extends CommonSQLSymbolParser implements HqlSymbolParser {
	
//	private DateRangeSymbolParser dateIn;
	
	public BetweenSymbolParser(SQLSymbolManager sqlSymbolManager){
		super(sqlSymbolManager, QueryDSLOps.BETWEEN);
//		this.dateIn = new DateRangeSymbolParser(sqlSymbolManager, FieldOP.date_in);
	}
	
	@SuppressWarnings("rawtypes")
	public String parse(String symbol, QueryField context){

		String field = context.getActualFieldName();
		Object value = context.getValue();
		ParamValues paramValues = context.getExtQuery().getParamsValue();
		IfNull ifNull = getIfNull(context);
		
		List paramlist = convertValues(field, value, ifNull);

		if(paramlist.size()!=2)
			throw new DbmException("the operator [" + symbol + "] excepted 2 parameters, acutal: " + paramlist.size());
		
		Object startValue = paramlist.get(0);
		Object endValue = paramlist.get(1);
		
		if (startValue==null || endValue==null) {
			throw new DbmException("between parameter value can not be null");
		}
		
//		if (!startValue.getClass().equals(endValue.getClass())) {
//			throw new DbmException("the type of between parameter value can not be difference");
//		}
		
//		if (startValue instanceof Date) {
//			return this.dateIn.parse(symbol, context);
//		}
		
		field = this.getFieldName(field);
		StringBuilder hql = new StringBuilder();

//		hql.append("( ");
		
		// startValue
		hql.append(field).append(" between ");
		paramValues.addValue(field, startValue, hql);
		hql.append(" and ");
		
		// endValue
		paramValues.addValue(field, endValue, hql);
		
		hql.append(" ");
//		hql.append(" ) ");
		
		return hql.toString();
	}
	
}
