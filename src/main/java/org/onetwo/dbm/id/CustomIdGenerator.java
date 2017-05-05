package org.onetwo.dbm.id;

import java.io.Serializable;

import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
public interface CustomIdGenerator<T extends Serializable> {

	T generate(DbmSessionImplementor session);

}
