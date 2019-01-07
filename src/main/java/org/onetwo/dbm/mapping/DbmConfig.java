package org.onetwo.dbm.mapping;

import org.onetwo.dbm.spring.EnableDbmAttributes;

import lombok.Data;

public interface DbmConfig {

	boolean isEnableSessionCache();
	EnableDbmAttributes getEnableDbmAttributes();
	void onEnableDbmAttributes(EnableDbmAttributes attributes);

	/******
	 * 当保存或者删除接口参数是列表时，size大于临界值userBatchThreshold的将会自动转为调用jdbc batch
	 * 默认50
	 */
	int getUseBatchThreshold();

	/*****
	 * 是否开启batch插入，默认开启
	 * @return
	 */
	boolean isUseBatchOptimize();
	
	/****
	 * 批量处理时，每次提交的数据量
	 * 默认10000
	 * @return
	 */
	int getProcessSizePerBatch();
	
	boolean isLogSql();
	
	boolean isWatchSqlFile();
	
	/****
	 * package to scan only for model
	 * @return
	 */
//	public String[] getModelPackagesToScan();
	
	String getDataSource();
	
	boolean isEnabledDebugContext();
	
	/****
	 * 如果当前线程上下文没发现事务，是否自动为当前session开启事务，以避免出错
	 * @author wayshall
	 * @return
	 */
	boolean isAutoProxySessionTransaction();
	
	/****
	 * snowfalke id算法配置，理论上，每个部署实例的机器id应该都不相同，否则不同都机器生成都id会有冲突都可能
	 * @author weishao zeng
	 * @return
	 */
	SnowflakeIdConfig getSnowflakeId();
	
	@Data
	public class SnowflakeIdConfig {
		/***
		 * 自动根据网络ip都最后两位创建
		 */
		private boolean auto = true;
		private long datacenterId = 1;
		private long machineId = 1;
	}

}