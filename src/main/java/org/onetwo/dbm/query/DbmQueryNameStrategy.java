package org.onetwo.dbm.query;

import java.util.Map;

import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.db.sqlext.SelectQueryNameStrategy;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.DbmMappedField;

/**
 * @author wayshall
 * <br/>
 */
public class DbmQueryNameStrategy extends SelectQueryNameStrategy {
	private DbmMappedEntry entry;

	public DbmQueryNameStrategy(DbmMappedEntry entry, String alias, Map<String, String> joinMapped, boolean aliasMainTableName) {
		super(alias, joinMapped, aliasMainTableName);
		this.entry = entry;
	}


	public String getFromName(Class<?> entityClass){
		String tableName = null;
		if(entry!=null){
			tableName = entry.getTableInfo().getName();
		}else{
			tableName = StringUtils.convert2UnderLineName(entityClass.getSimpleName());
		}
		return tableName;
	}

	@Override
	public String getFieldName(String f) {
		String fieldName = f;
		if(entry!=null && entry.contains(f)){
			fieldName = entry.getColumnName(f);
		}
		return super.getFieldName(fieldName);
	}
	

	public DbmMappedField getDbmMappedField(String f) {
		String fieldName = getFieldName(f);
		if (entry==null) {
			return null;
		}
		Map<String, DbmMappedField> mappedFields = entry.getMappedFields();
		DbmMappedField field = mappedFields.get(fieldName);
		if (field==null && entry.containsColumn(f)) {
			field = entry.getFieldByColumnName(f);
		}
		return field;
	}
	
	public DbmMappedEntry getDbmMappedEntry() {
		return entry;
	}
	
	public String appendAlias(String f){
		if(StringUtils.isBlank(this.alias)){
			return f;
		}
		String newf = f;
		if(f.startsWith(K.NO_PREFIX)){
			newf = f.substring(K.NO_PREFIX.length());
		}else{
			if(isAliasMainTableName()){
				if(!f.contains(".")){
					newf = this.alias + "." + f;
				}
			}
		}
		return newf;
	}
}
