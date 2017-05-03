package org.onetwo.dbm.id;

import java.io.Serializable;
import java.util.List;

import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
public interface IdGenerator<T extends Serializable> {

	String getName();
	T generate(DbmSessionImplementor session);
	List<T> batchGenerate(DbmSessionImplementor session, int batchSize);

}
