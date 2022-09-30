package org.onetwo.dbm.mapping;

import org.onetwo.dbm.utils.Initializable;

public interface MappedEntryBuilder extends Initializable {

	/*public static class TMeta {
		public static final String column_prefix = ":";
		public static final String meta_prefix = "#";
		public static final String table = "#table";
		public static final String entity_meta = "#entity_meta";
		public static final String pk = "#pk";
		public static final String use_keys_as_fields = "#use_keys_as_fields";
		public static final String seq_name = "#seq-name";
	}*/
	
	public boolean isSupported(Object entity);
	
//	public String getCacheKey(Object entity);

	public DbmMappedEntry buildMappedEntry(Object entity);
	
	/***
	 * 只构建相关字段映射结构，用于非实体查询时映射值
	 * @author wayshall
	 * @param entry
	 * @return
	 */
	public DbmMappedEntry buildMappedFields(DbmMappedEntry entry);
	
//	public void afterAllBuildMappedEntry(JFishMappedEntry entry);
	

}