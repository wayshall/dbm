package org.onetwo.dbm.mapping;


public interface MappedEntryManager {
	
//	public boolean isSupported(Object entity);
	public boolean isSupportedMappedEntry(Object entity);
	public void scanPackages(String... packagesToScan);
	public DbmMappedEntry findEntry(Object object);
	public DbmMappedEntry getEntry(Object object);
	public void setMappedEntryManagerListener(MappedEntryManagerListener mappedEntryManagerListener);
//	public JFishMappedEntry buildMappedEntry(Class<?> entityClass, boolean byProperty);
	
	/***
	 * 只构建相关字段映射结构，用于非实体查询时映射值；不能用于插入、修改和删除
	 * @see JdbcRowEntryImpl
	 * @author wayshall
	 * @param clazz
	 * @return
	 */
	public DbmMappedEntry getReadOnlyEntry(Class<?> clazz);
	

}