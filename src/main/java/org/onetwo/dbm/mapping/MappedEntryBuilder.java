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
	 * 只构建相关字段映射结构，不构建与jpa相关的任何注解信息
	 * @author wayshall
	 * @param entry
	 * @return
	 */
	public DbmMappedEntry buildMappedFields(DbmMappedEntry entry);
	
//	public void afterAllBuildMappedEntry(JFishMappedEntry entry);
	

}