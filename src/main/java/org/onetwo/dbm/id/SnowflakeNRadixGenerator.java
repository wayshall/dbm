package org.onetwo.dbm.id;

import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
public class SnowflakeNRadixGenerator implements CustomIdGenerator<String> {
	SnowflakeIdGenerator idGenerator;
	private int workerId = 1;
	private int radix = 36;
	
	public SnowflakeNRadixGenerator initGenerator(){
		idGenerator = new SnowflakeIdGenerator(workerId);
		return this;
	}
	
	@Override
	public String generate(DbmSessionImplementor session) {
		Long id = idGenerator.nextId();
		String sid = Long.toString(id, radix);
		return sid;
	}

	public void setWorkerId(int workerId) {
		this.workerId = workerId;
	}

	public void setRadix(int radix) {
		this.radix = radix;
	}
	
}
