package org.onetwo.dbm.mapping;

import java.util.Collections;

import org.onetwo.common.annotation.AnnotationInfo;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;

/**
 * @author wayshall
 * <br/>
 */
public class JdbcRowEntryImpl extends AbstractDbmMappedEntryImpl {

	public JdbcRowEntryImpl(AnnotationInfo annotationInfo, DbmInnerServiceRegistry serviceRegistry) {
		super(annotationInfo, null, serviceRegistry);
	}
	

	@Override
	public void buildEntry() {
		for(DbmMappedField field : this.mappedFields.values()){
			if(field.getColumn()!=null)
				this.mappedColumns.put(field.getColumn().getName().toLowerCase(), field);
		}

		this.mappedFields = Collections.unmodifiableMap(this.mappedFields);
		this.mappedColumns = Collections.unmodifiableMap(this.mappedColumns);
		
//		freezing();
	}

	@Override
	protected EntrySQLBuilderImpl getStaticInsertSqlBuilder() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected EntrySQLBuilderImpl getStaticUpdateSqlBuilder() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected EntrySQLBuilderImpl getStaticDeleteSqlBuilder() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected EntrySQLBuilderImpl getStaticFetchSqlBuilder() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected EntrySQLBuilder getStaticFetchAllSqlBuilder() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected EntrySQLBuilder getStaticSelectVersionSqlBuilder() {
		throw new UnsupportedOperationException();
	}

}
