package org.onetwo.dbm.id;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.convert.Types;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.NetUtils;
import org.onetwo.dbm.exception.DbmException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author weishao zeng
 * <br/>
 */
public class DbmIds {
	/***
	 * 
	 * spring单元测试时，一般为-1
	 * 
	 */
	public static final int UNKNOW_TX_ID = -1;
	
	private static final AtomicLong TX_ID_COUNTER = new AtomicLong(1);
	
	public static final String SNOWFLAKE_BEAN_NAME = "dbmSnowflakeIdGenerator";
	public static final SnowflakeIdGenerator DefaultSnowflakeGenerator = new SnowflakeIdGenerator(7L);
	private static final Cache<SnowflakeIdKey, SnowflakeIdGenerator> IDCACHES = CacheBuilder.newBuilder()
																							.build();

	public static AtomicLong getTxIdCounter() {
		return TX_ID_COUNTER;
	}
	
	/****
	 * 根据内网ip的后两位创建生成器，其中最后一位为机器id，倒数第二位为数据中心id
	 * @author weishao zeng
	 * @return
	 */
	public static SnowflakeIdGenerator createIdGeneratorByAddress() {
		//根据ip地址来创建生成器
//		String[] strs = StringUtils.split(NetUtils.getHostAddress(), ".");
		String[] strs = StringUtils.split(NetUtils.getLocalHostLANIp(), ".");
		int datacenterId = 0;
		int machineId = 0;
		if (strs!=null && strs.length>=2) {
			datacenterId = Types.asValue(strs[strs.length-2], int.class, 1)%32;
			machineId = Types.asValue(strs[strs.length-1], int.class, 1)%32;
			JFishLoggerFactory.getCommonLogger().info("[dbm] createIdGeneratorByAddress, datacenterId: {}, machineId: {}", datacenterId, machineId);
		} else {
			JFishLoggerFactory.getCommonLogger().info("[dbm] localhost lanip not found, createIdGeneratorByAddress user default value , datacenterId: {}, machineId: {}", datacenterId, machineId);
		}
		SnowflakeIdGenerator idGenerator = createIdGenerator(new SnowflakeIdKey(datacenterId, machineId));
		return idGenerator;
	}

	public static SnowflakeIdGenerator createIdGenerator(long datacenterId, long machineId) {
		return createIdGenerator(new SnowflakeIdKey(datacenterId, machineId));
	}
	
	public static SnowflakeIdGenerator createIdGenerator(SnowflakeIdKey key) {
		try {
			SnowflakeIdGenerator idGenerator = IDCACHES.get(key, () -> {
				return new SnowflakeIdGenerator(key.getDatacenterId(), key.getMachineId());
			});
			return idGenerator;
		} catch (ExecutionException e) {
			throw new DbmException("create snowflakeIdGenerator error", e);
		}
	}
	
	@Data
	@AllArgsConstructor
	public static class SnowflakeIdKey {
		final private long datacenterId;
		final private long machineId;
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SnowflakeIdKey other = (SnowflakeIdKey) obj;
			if (datacenterId != other.datacenterId)
				return false;
			if (machineId != other.machineId)
				return false;
			return true;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (datacenterId ^ (datacenterId >>> 32));
			result = prime * result + (int) (machineId ^ (machineId >>> 32));
			return result;
		}
		
	}
	
	private DbmIds() {
	}

}

