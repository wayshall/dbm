package org.onetwo.dbm.mapping;

import java.util.Collection;

import org.onetwo.common.annotation.AnnotationInfo;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.mapping.SQLBuilderFactory.SqlBuilderType;

/****
 * 连接表映射？
 * 暂没有用到
 * @author way
 *
 */
public class DbmJoinedMappedEntryImpl extends AbstractDbmMappedEntryImpl implements DbmMappedEntry {

	private EntrySQLBuilderImpl staticInsertSqlBuilder;
	private EntrySQLBuilderImpl staticFetchSqlBuilder;
	private EntrySQLBuilderImpl staticDeleteSqlBuilder;
	private EntrySQLBuilderImpl staticFetchAllSqlBuilder;
	
	public DbmJoinedMappedEntryImpl(AnnotationInfo annotationInfo, TableInfo tableInfo, DbmInnerServiceRegistry serviceRegistry) {
		super(annotationInfo, tableInfo, serviceRegistry);
	}
	
	protected void buildStaticSQL(TableInfo tableInfo){
		Collection<? extends DbmMappedField> columns = getFields();
//		List<ColumnInfo> idColumns = tableInfo.getPrimaryKey().getColumns();

		staticInsertSqlBuilder = createSQLBuilder(SqlBuilderType.insert);
		staticInsertSqlBuilder.append(getFields());
		staticInsertSqlBuilder.build();
		
		staticDeleteSqlBuilder = createSQLBuilder(SqlBuilderType.delete);
		staticDeleteSqlBuilder.setNamedPlaceHoder(false);
		staticDeleteSqlBuilder.appendWhere(getFields());
		staticDeleteSqlBuilder.build();
		
		staticFetchSqlBuilder = createSQLBuilder(SqlBuilderType.query);
		staticFetchSqlBuilder.setNamedPlaceHoder(false);
		staticFetchSqlBuilder.append(columns);
		staticFetchSqlBuilder.appendWhere(columns);
		staticFetchSqlBuilder.build();

		staticFetchAllSqlBuilder = createSQLBuilder(SqlBuilderType.query);
		staticFetchAllSqlBuilder.setNamedPlaceHoder(false);
		staticFetchAllSqlBuilder.append(columns);
		staticFetchAllSqlBuilder.build();
	}

	/*@Override
	public String getStaticSeqSql() {
		throw new UnsupportedOperationException("the joined entity unsupported this operation!");
	}*/

	@Override
	protected EntrySQLBuilderImpl getStaticInsertSqlBuilder() {
		return staticInsertSqlBuilder;
	}

	@Override
	protected EntrySQLBuilderImpl getStaticUpdateSqlBuilder() {
		throw new UnsupportedOperationException("the joined entity unsupported this operation!");
	}

	@Override
	protected EntrySQLBuilderImpl getStaticDeleteSqlBuilder() {
		return staticDeleteSqlBuilder;
	}

	@Override
	public MappedType getMappedType() {
//		return MappedType.JOINED;
		throw new UnsupportedOperationException();
	}

	@Override
	protected EntrySQLBuilderImpl getStaticFetchSqlBuilder() {
		return staticFetchSqlBuilder;
	}

	public EntrySQLBuilder getStaticFetchAllSqlBuilder() {
		return staticFetchAllSqlBuilder;
	}

	@Override
	protected EntrySQLBuilder getStaticSelectVersionSqlBuilder() {
		throw new UnsupportedOperationException("the joined entity unsupported this operation!");
	}
	
}
