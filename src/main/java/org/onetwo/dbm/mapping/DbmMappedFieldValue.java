package org.onetwo.dbm.mapping;

import org.onetwo.common.db.sqlext.QueryDSLOps;
import org.onetwo.dbm.dialet.DBDialect;

/**
 * @author weishao zeng
 * <br/>
 */
public class DbmMappedFieldValue {
	public static final String QMARK = "?";
	
	public static DbmMappedFieldValue create(DbmMappedField field, DBDialect dialet) {
		return new DbmMappedFieldValue(field, dialet);
	}
	
	final private DbmMappedField field;
	final private Object value;
	final private QueryDSLOps operator;
	private DBDialect dialet;
	
	private DbmMappedFieldValue(DbmMappedField field, DBDialect dialet) {
		this(field, QueryDSLOps.EQ, null, dialet);
	}
	
	public DbmMappedFieldValue(DbmMappedField field, QueryDSLOps operator, Object value, DBDialect dialet) {
		super();
		this.field = field;
		this.value = value;
		this.operator = operator;
		this.dialet = dialet;
	}
	
	public String toWhereString(boolean namedPlaceHoder, boolean alias) {
		String fieldString = alias?field.getColumn().getNameWithAlias():field.getColumn().getName();
		fieldString = dialet.wrapKeywordColumnName(fieldString);
		
		if (operator == QueryDSLOps.IS_NULL) {
			return fieldString + " " + operator.getOperator();
		}
		
		String namedStr = QMARK;
		String where = null;
		if(namedPlaceHoder){
			namedStr = alias?field.getColumn().getNamedPlaceHolderWithAlias():field.getColumn().getNamedPlaceHolder();
			where = fieldString + " " + operator.getOperator() + " " + namedStr;
		}else{
			where = fieldString + " " + operator.getOperator() + " " + namedStr;
		}
		return where;
	}

	public DbmMappedField getField() {
		return field;
	}

	public Object getValue() {
		return value;
	}

	public QueryDSLOps getOperator() {
		return operator;
	}

	public DBDialect getDialet() {
		return dialet;
	}

	public void setDialet(DBDialect dialet) {
		this.dialet = dialet;
	}

}
