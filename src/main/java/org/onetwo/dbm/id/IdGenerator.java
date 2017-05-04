package org.onetwo.dbm.id;

import java.io.Serializable;

import org.onetwo.dbm.core.spi.DbmSessionImplementor;
import org.springframework.data.util.Pair;

/**
 * @author wayshall
 * <br/>
 */
public interface IdGenerator<T extends Serializable> {

	String getName();
	T generate(DbmSessionImplementor session);
	Pair<T, T> batchGenerate(DbmSessionImplementor session, int batchSize);

}
