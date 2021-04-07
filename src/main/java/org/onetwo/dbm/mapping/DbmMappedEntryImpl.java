package org.onetwo.dbm.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onetwo.common.annotation.AnnotationInfo;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.mapping.SQLBuilderFactory.SqlBuilderType;

public class DbmMappedEntryImpl extends AbstractDbmMappedEntryImpl implements DbmMappedEntry {
	

	private EntrySQLBuilderImpl staticInsertOrUpdateSqlBuilder;
	private EntrySQLBuilderImpl staticInsertOrIgnoreSqlBuilder;
	private EntrySQLBuilderImpl staticInsertSqlBuilder;
	private EntrySQLBuilderImpl staticUpdateSqlBuilder;
	private EntrySQLBuilderImpl staticFetchAllSqlBuilder;
	private EntrySQLBuilderImpl staticFetchSqlBuilder;
	private EntrySQLBuilderImpl staticDeleteSqlBuilder;
	private EntrySQLBuilderImpl staticDeleteAllSqlBuilder;
	/*private EntrySQLBuilderImpl staticSeqSqlBuilder;
	private EntrySQLBuilderImpl staticCreateSeqSqlBuilder;*/
	private EntrySQLBuilderImpl staticSelectVersionSqlBuilder;
//	private EntrySQLBuilderImpl staticSelectLockSqlBuilder;
	
	public DbmMappedEntryImpl(String entityName, AnnotationInfo annotationInfo, TableInfo tableInfo, DbmInnerServiceRegistry serviceRegistry) {
		super(entityName, annotationInfo, tableInfo, serviceRegistry);
	}


	protected Collection<DbmMappedField> getInsertableFields(){
		List<DbmMappedField> insertables = new ArrayList<DbmMappedField>();
		for (DbmMappedField field : getMappedColumns().values()) {
			if (field.getColumn()!=null && field.getColumn().isInsertable()) {
				insertables.add(field);
			}
		}
		return insertables;
	}
	
	public List<DbmMappedField> getUpdateableFields(){
		List<DbmMappedField> updatables = new ArrayList<DbmMappedField>();
		for (DbmMappedField field : getMappedColumns().values()){
			if (field.getColumn()!=null && field.getColumn().isUpdatable()) {
				updatables.add(field);
			}
		}
		return updatables;
	}
	

	public Collection<DbmMappedField> getSelectableField() {
		List<DbmMappedField> cols = LangUtils.newArrayList();
		for(DbmMappedField field : this.getMappedColumns().values()){
			if (field.getColumn()==null) {
				continue;
			}
			if (field.getColumn().isLazy()) {
				continue;
			}
			if (field.getColumn().isSelectable()) {
				cols.add(field);
			}
		}
		return cols;
	}
	
	protected void buildStaticSQL(TableInfo taboleInfo){
//		List<ColumnInfo> idColumns = taboleInfo.getPrimaryKey().getColumns();

		staticInsertOrUpdateSqlBuilder = createSQLBuilder(SqlBuilderType.insertOrUpdate);
		staticInsertOrUpdateSqlBuilder.append(getInsertableFields());
		staticInsertOrUpdateSqlBuilder.build();

		staticInsertOrIgnoreSqlBuilder = createSQLBuilder(SqlBuilderType.insertOrIgnore);
		staticInsertOrIgnoreSqlBuilder.append(getInsertableFields());
		staticInsertOrIgnoreSqlBuilder.build();
		
		staticInsertSqlBuilder = createSQLBuilder(SqlBuilderType.insert);
		staticInsertSqlBuilder.append(getInsertableFields());
		staticInsertSqlBuilder.build();
		
		staticUpdateSqlBuilder = createSQLBuilder(SqlBuilderType.update);
		staticUpdateSqlBuilder.append(getUpdateableFields());
		staticUpdateSqlBuilder.appendWhere(getIdentifyFields());
		staticUpdateSqlBuilder.appendWhere(getVersionField());
		staticUpdateSqlBuilder.build();
		
		staticDeleteSqlBuilder = createSQLBuilder(SqlBuilderType.delete);
		staticDeleteSqlBuilder.setNamedPlaceHoder(false);
		staticDeleteSqlBuilder.appendWhere(getIdentifyFields());
		staticDeleteSqlBuilder.appendWhere(getVersionField());
		staticDeleteSqlBuilder.build();
		
		/*staticSeqSqlBuilder = createSQLBuilder(SqlBuilderType.seq);
		staticSeqSqlBuilder.setNamedPlaceHoder(false);
		staticSeqSqlBuilder.build();
		
		staticCreateSeqSqlBuilder = createSQLBuilder(SqlBuilderType.createSeq);
		staticCreateSeqSqlBuilder.setNamedPlaceHoder(false);
		staticCreateSeqSqlBuilder.build();*/

		Collection<DbmMappedField> columns = getSelectableField();
		staticFetchSqlBuilder = createSQLBuilder(SqlBuilderType.query);
		staticFetchSqlBuilder.setNamedPlaceHoder(false);
		staticFetchSqlBuilder.append(columns);
		staticFetchSqlBuilder.appendWhere(getIdentifyFields());
		staticFetchSqlBuilder.build();

		staticFetchAllSqlBuilder = createSQLBuilder(SqlBuilderType.query);
		staticFetchAllSqlBuilder.setNamedPlaceHoder(false);
		staticFetchAllSqlBuilder.append(columns);
		staticFetchAllSqlBuilder.build();

		staticDeleteAllSqlBuilder = createSQLBuilder(SqlBuilderType.delete);
		staticDeleteAllSqlBuilder.setNamedPlaceHoder(false);
		staticDeleteAllSqlBuilder.build();
		
		staticSelectVersionSqlBuilder = createSQLBuilder(SqlBuilderType.query);
		staticSelectVersionSqlBuilder.append(getVersionField());
		staticSelectVersionSqlBuilder.appendWhere(getIdentifyFields());
		staticSelectVersionSqlBuilder.build();
		

		/*staticSelectLockSqlBuilder = createSQLBuilder(SqlBuilderType.query);
		staticSelectLockSqlBuilder.setNamedPlaceHoder(false);
		staticSelectLockSqlBuilder.append(columns);
		staticFetchSqlBuilder.appendWhere(getIdentifyField());
		staticSelectLockSqlBuilder.build();*/
	}
	
	/*public EntrySQLBuilderImpl getStaticSelectLockSqlBuilder() {
		return staticSelectLockSqlBuilder;
	}*/



	@Override
	protected EntrySQLBuilderImpl getStaticInsertOrIgnoreSqlBuilder() {
		return staticInsertOrIgnoreSqlBuilder;
	}

	@Override
	protected EntrySQLBuilderImpl getStaticInsertOrUpdateSqlBuilder() {
		return staticInsertOrUpdateSqlBuilder;
	}

	@Override
	protected EntrySQLBuilderImpl getStaticInsertSqlBuilder() {
		return staticInsertSqlBuilder;
	}

	@Override
	protected EntrySQLBuilderImpl getStaticUpdateSqlBuilder() {
		return staticUpdateSqlBuilder;
	}

	@Override
	protected EntrySQLBuilderImpl getStaticDeleteSqlBuilder() {
		return staticDeleteSqlBuilder;
	}


	@Override
	protected EntrySQLBuilderImpl getStaticFetchSqlBuilder() {
		return staticFetchSqlBuilder;
	}

	public EntrySQLBuilder getStaticFetchAllSqlBuilder() {
		return staticFetchAllSqlBuilder;
	}


	@Override
	protected EntrySQLBuilder getStaticDeleteAllSqlBuilder() {
		return staticDeleteAllSqlBuilder;
	}

	@Override
	protected EntrySQLBuilder getStaticSelectVersionSqlBuilder() {
		return staticSelectVersionSqlBuilder;
	}

	/*public EntrySQLBuilderImpl getStaticCreateSeqSqlBuilder() {
		return staticCreateSeqSqlBuilder;
	}*/
	
}
