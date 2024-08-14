package org.onetwo.common.db.sqlext;

import java.util.Optional;
import java.util.stream.Stream;

import org.onetwo.common.db.builder.QueryField;
import org.onetwo.dbm.exception.DbmException;

/**
 * @author weishao zeng
 * <br/>
 */
public enum QueryDSLOps {
	EQ("="),
	GT(">"),
	GE(">="),
	LT("<"),
	LE("<="),
	NEQ("!="),
	NEQ2("<>"),
	IN("in"),
	NOT_IN("not in"),
	BETWEEN("between"),
	LIKE("like"),
	NOT_LIKE("not like"),
	LIKE2("=~", "like"),
	NOT_LIKE2("!=~", "not like"),
	IS_NULL("is null"),
	DATE_IN("date in")
	;
	
	private String operator;
	/***
	 * 实际操作符
	 */
	private String actualOperator;
	
	private QueryDSLOps(String operator) {
		this.operator = operator;
		this.actualOperator = operator;
	}
	
	private QueryDSLOps(String operator, String actualOperator) {
		this.operator = operator;
		this.actualOperator = actualOperator;
	}

	public final String qstr(String name){
		return name + QueryField.SPLIT_SYMBOL + operator;
	}

	public String getOperator() {
		return operator;
	}

	public String getActualOperator() {
		return actualOperator;
	}

	static public final QueryDSLOps operatorOf(String operator){
		Optional<QueryDSLOps> ops = Stream.of(values()).filter(op -> op.getOperator().equalsIgnoreCase(operator)).findAny();
		if (!ops.isPresent()) {
			throw new DbmException("error query operator: " + operator);
		}
		return ops.get();
	}
	
}
