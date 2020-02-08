package org.onetwo.dbm.query;

import java.util.Collection;

import org.onetwo.common.annotation.AnnotationInfo;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.mapping.AbstractDbmMappedEntryImpl;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.onetwo.dbm.mapping.EntrySQLBuilder;
import org.onetwo.dbm.mapping.EntrySQLBuilderImpl;
import org.onetwo.dbm.mapping.MappedType;
import org.onetwo.dbm.mapping.SQLBuilderFactory.SqlBuilderType;
import org.onetwo.dbm.mapping.TableInfo;

public class DbmQueryableMappedEntryImpl extends AbstractDbmMappedEntryImpl implements DbmMappedEntry {

	private EntrySQLBuilderImpl staticFetchSqlBuilder;
	

	public DbmQueryableMappedEntryImpl(String entityName, AnnotationInfo annotationInfo, TableInfo tableInfo, DbmInnerServiceRegistry serviceRegistry) {
		super(entityName, annotationInfo, tableInfo, serviceRegistry);
	}
	
	protected void buildStaticSQL(TableInfo taboleInfo){
		Collection<? extends DbmMappedField> columns = getFields();
		
		staticFetchSqlBuilder = createSQLBuilder(SqlBuilderType.query);
		staticFetchSqlBuilder.setNamedPlaceHoder(false);
		staticFetchSqlBuilder.append(columns);
		if(taboleInfo.getPrimaryKey()!=null){
			staticFetchSqlBuilder.appendWhere(getIdentifyFields());
		}
		staticFetchSqlBuilder.build();
	}
	/*
	@Override
	public String getStaticInsertSql() {
		return staticInsertSqlBuilder.getSql();
	}

	@Override
	public String getStaticUpdateSql() {
		return staticUpdateSqlBuilder.getSql();
	}*/

	/*@Override
	public String getStaticFetchSql() {
		return staticFetchSqlBuilder.getSql();
	}*/

	/*@Override
	public String getStaticSeqSql() {
		throw new UnsupportedOperationException("the queryable entity unsupported this operation!");
	}*/

	@Override
	protected EntrySQLBuilderImpl getStaticInsertSqlBuilder() {
		throw new UnsupportedOperationException("the queryable entity unsupported this operation!");
	}

	@Override
	protected EntrySQLBuilderImpl getStaticUpdateSqlBuilder() {
		throw new UnsupportedOperationException("the queryable entity unsupported this operation!");
	}

	@Override
	protected EntrySQLBuilderImpl getStaticDeleteSqlBuilder() {
		throw new UnsupportedOperationException("the queryable entity unsupported this operation!");
	}

	public boolean isQueryableOnly() {
		return true;
	}

	public MappedType getMappedType() {
		return MappedType.QUERYABLE_ONLY;
	}

	@Override
	protected EntrySQLBuilderImpl getStaticFetchSqlBuilder() {
		return staticFetchSqlBuilder;
	}

	@Override
	protected EntrySQLBuilder getStaticFetchAllSqlBuilder() {
		throw new UnsupportedOperationException("the queryable entity unsupported this operation!");
	}

	@Override
	protected EntrySQLBuilder getStaticSelectVersionSqlBuilder() {
		throw new UnsupportedOperationException("the queryable entity unsupported this operation!");
	}
}
