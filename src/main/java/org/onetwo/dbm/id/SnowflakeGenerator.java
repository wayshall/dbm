package org.onetwo.dbm.id;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
public class SnowflakeGenerator implements CustomIdGenerator<Serializable>  {

	private SnowflakeIdGenerator idGenerator;
	private String prefix;
	
	public SnowflakeGenerator() {
		super();
		this.idGenerator = DbmIds.DefaultSnowflakeGenerator;
	}

	@Override
	public Serializable generate(DbmSessionImplementor session) {
		Serializable id = getIdGenerator().nextId();
		if(StringUtils.isNotBlank(prefix)){
			return prefix + id;
		}
		return id;
	}

	public SnowflakeIdGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
}
