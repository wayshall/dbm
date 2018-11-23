package org.onetwo.dbm.id;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.convert.Types;
import org.onetwo.common.utils.NetUtils;

/**
 * @author weishao zeng
 * <br/>
 */
public class DbmIds {
	public static final SnowflakeIdGenerator DefaultSnowflakeGenerator = new SnowflakeIdGenerator(7L);
	

	public static SnowflakeIdGenerator createIdGeneratorByAddress() {
		//根据ip地址的最后一位数字来创建生成器
		String[] strs = StringUtils.split(NetUtils.getHostAddress(), ".");
		int last = Types.asValue(strs[strs.length-1], int.class, 1);
		SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(last/32, last%32);
		return idGenerator;
	}
	
	private DbmIds() {
	}

}

