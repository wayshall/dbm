package org.onetwo.common.db.sqlext;
/**
 * @author weishao zeng
 * <br/>
 */

import org.onetwo.common.db.sqlext.SQLSymbolManager.FieldOP;

public enum SQLOps {
	LIKE(FieldOP.like),
	LIKE2(FieldOP.like2),
	NOT_LIKE(FieldOP.not_like),
	NOT_LIKE2(FieldOP.not_like2),
	EQUAL(FieldOP.eq),
	GREATER(FieldOP.gt),
	GREATER_EQUAL(FieldOP.ge),
	LESS(FieldOP.lt),
	LESS_EQUAL(FieldOP.le),
	NOT_EQUAL(FieldOP.neq),
	IN(FieldOP.in),
	NOT_IN(FieldOP.not_in),
	DATE_IN(FieldOP.date_in),
	IS_NULL(FieldOP.is_null);
	
	private final String symbol;

	private SQLOps(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}

}
