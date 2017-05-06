package org.onetwo.dbm.id;

import javax.persistence.GenerationType;

/**
 * @author wayshall
 * <br/>
 */
public enum StrategyType {
	IDENTITY(GenerationType.IDENTITY),
	SEQ(GenerationType.SEQUENCE),
	TABLE(GenerationType.TABLE),
	DBM(GenerationType.AUTO);
	
	final private GenerationType generationType;

	private StrategyType(GenerationType generationType) {
		this.generationType = generationType;
	}

	public GenerationType getGenerationType() {
		return generationType;
	}
	
}
