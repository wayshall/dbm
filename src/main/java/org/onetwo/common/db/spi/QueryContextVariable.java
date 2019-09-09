package org.onetwo.common.db.spi;

public interface QueryContextVariable {
	
	String varName();

	public interface QueryGlobalVariable extends QueryContextVariable {
	}
}
