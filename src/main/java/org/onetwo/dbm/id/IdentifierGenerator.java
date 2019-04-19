package org.onetwo.dbm.id;

import java.io.Serializable;
import java.util.List;

import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
public interface IdentifierGenerator<T extends Serializable> {

	String getName();
	
	StrategyType getStrategyType();
	
	T generate(DbmSessionImplementor session);
	/***
	 * 
	 * @author wayshall
	 * @param session
	 * @param batchSize
	 * @return change Pair<T, T> to List<T>, because not all id is increase 1
	 */
	List<T> batchGenerate(DbmSessionImplementor session, int batchSize);

}
