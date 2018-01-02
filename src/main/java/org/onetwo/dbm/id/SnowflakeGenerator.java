package org.onetwo.dbm.id;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.convert.Types;
import org.onetwo.common.utils.NetUtils;
import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
public class SnowflakeGenerator implements CustomIdGenerator<Serializable>  {

	private SnowflakeIdGenerator idGenerator;
	private String prefix;
	
	@Override
	public Serializable generate(DbmSessionImplementor session) {
		Serializable id = getIdGenerator().nextId();
		if(StringUtils.isNotBlank(prefix)){
			return prefix + id;
		}
		return id;
	}

	public SnowflakeIdGenerator getIdGenerator() {
		SnowflakeIdGenerator idGenerator = this.idGenerator;
		if(idGenerator==null){
			//根据ip地址的最后一位数字来创建生成器
			String[] strs = StringUtils.split(NetUtils.getHostAddress());
			int last = Types.asValue(strs[strs.length-1], int.class, 1);
			idGenerator = new SnowflakeIdGenerator(last/32, last%32);
			this.idGenerator = idGenerator;
		}
		return idGenerator;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
}
