package org.onetwo.dbm.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.onetwo.dbm.annotation.DbmResultMapping;
import org.onetwo.dbm.jdbc.internal.ResultSetColumnValueGetter;
import org.onetwo.dbm.jdbc.spi.ColumnValueGetter;
import org.onetwo.dbm.utils.DbmUtils;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.util.Assert;

public class DbmNestedBeanRowMapper<T> extends AbstractNestedBeanMapper<T> implements DataRowMapper<T> {

//	protected JdbcResultSetGetter jdbcResultSetGetter;
	
	public DbmNestedBeanRowMapper(DbmRowMapperFactory rowMapperFactory, Class<T> mappedClass, DbmResultMapping dbmResultMapping) {
		super(rowMapperFactory, mappedClass, dbmResultMapping);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		Assert.state(this.mappedClass != null, "Mapped class was not specified");
		ResultSetWrappingSqlRowSet resutSetWrapper = new ResultSetWrappingSqlRowSet(rs);
		SqlRowSetMetaData rsmd = resutSetWrapper.getMetaData();
		Map<String, Integer> names = DbmUtils.lookupColumnNames(rsmd);
		
		ColumnValueGetter columnValueGetter = new ResultSetColumnValueGetter(resutSetWrapper, getRowMapperFactory().getJdbcResultSetGetter());

		RowResultContext rowContext = new RowResultContext(resutSetWrapper, null, null);
		T mappedObject = (T)this.resultClassMapper.mapResult(rowContext, names, columnValueGetter, rowNum);
		return mappedObject;
	}

}
