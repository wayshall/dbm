package org.onetwo.dbm.id;

import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
public class SnowflakeGenerator implements CustomIdGenerator<Long>  {

	private SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1);
	
	@Override
	public Long generate(DbmSessionImplementor session) {
		return idGenerator.nextId();
	}
	
}
