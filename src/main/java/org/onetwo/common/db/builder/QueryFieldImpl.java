package org.onetwo.common.db.builder;

import java.util.List;
import java.util.stream.Collectors;

import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.db.sqlext.ExtQueryInner;
import org.onetwo.common.db.sqlext.QueryDSLOps;
import org.onetwo.common.db.sqlext.QueryNameStrategy;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.onetwo.dbm.query.DbmQueryNameStrategy;

public class QueryFieldImpl implements QueryField {
	
	
	public static QueryField create(Object p, QueryNameStrategy queryNameStrategy){
		QueryField qf = null;
		if(p instanceof String){
			qf = new QueryFieldImpl(p.toString(), queryNameStrategy);
		}else if(p instanceof QueryField){
			qf = (QueryField) p;
		}else{
			throw new DbmException("error field expression : " + p);
		}
		return qf;
	}
	
	private ExtQueryInner extQuery;
	private String fieldExpr;
	private Object value;
	
	private String fieldName;
	private String operator;
	private QueryNameStrategy queryNameStrategy;
	
	QueryFieldImpl(String fieldExpr, QueryNameStrategy queryNameStrategy) {
		super();
		this.fieldExpr = fieldExpr;

		String[] sp = StringUtils.split(fieldExpr, QueryField.SPLIT_SYMBOL);
		this.fieldName = sp[0];
		if(sp.length==2) {
			this.operator = sp[1];
		} else {
			this.operator = QueryDSLOps.EQ.getActualOperator();
		}
		this.queryNameStrategy = queryNameStrategy;
	}
	
	public void init(ExtQueryInner extQuery, Object value){
		this.extQuery = extQuery;
		this.value = value;
		
	}

	public String getActualFieldName() {
		String newf = this.fieldName;
		if(newf.startsWith(K.FUNC)){
			newf = processFunction(newf);
		}else{
//			newf = extQuery.getFieldName(newf);
			newf = this.queryNameStrategy.getFieldName(newf);
		}
		return newf;
	}

	public Object getValue() {
		if (isDbmQueryField()) {
			DbmQueryNameStrategy dbmStrategy = (DbmQueryNameStrategy)this.queryNameStrategy;
			DbmMappedField field = dbmStrategy.getDbmMappedField(this.fieldName);
			if (field.isEnumeratedOrdinal()) {
				if (LangUtils.isMultiple(field)) {
					List<Object> values = LangUtils.asList(value).stream().map(e -> ordinalIfPossible(e)).collect(Collectors.toList());
					return values;
				} else {
					Object val = ordinalIfPossible(value);
					return val;
				}
			}
		}
		return value;
	}
	
	private Object ordinalIfPossible(Object value) {
		if (value instanceof Enum) {
			Object val = ((Enum<?>)value).ordinal();
			return val;
		}
		return value;
	}
	
	private boolean isDbmQueryField() {
		return this.queryNameStrategy instanceof DbmQueryNameStrategy;
	}

	public String processFunction(String f) {
		String result = f;
		
		int leftParentheses = f.indexOf('(');
		int rightParentheses = f.indexOf(')');
		if(leftParentheses==-1 || rightParentheses==-1)
			throw new DbmException("the function must with parentheses : " + f);
		
		String fname = f.substring(K.FUNC.length(), leftParentheses);
		String paramString = f.substring(leftParentheses+1, rightParentheses);
		if(StringUtils.isBlank(paramString))
			throw new DbmException("the function's parameter can not be emtpy : " + paramString);
		
		String[] args = StringUtils.split(paramString, ",");
//		args[0] = extQuery.getFieldName(args[0]);
		args[0] = this.queryNameStrategy.getFieldName(args[0]);
		result = extQuery.getSqlFunctionManager().exec(fname, (Object[])args);
		
		return result;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryFieldImpl other = (QueryFieldImpl) obj;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (operator == null) {
			if (other.operator != null)
				return false;
		} else if (!operator.equals(other.operator))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public String getOperator() {
		return operator;
	}

	public String getFieldExpr() {
		return fieldExpr;
	}

	public ExtQueryInner getExtQuery() {
		return extQuery;
	}

	public String getFieldName() {
		return fieldName;
	}
}
