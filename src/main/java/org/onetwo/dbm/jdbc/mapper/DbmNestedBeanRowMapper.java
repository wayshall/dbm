package org.onetwo.dbm.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.onetwo.dbm.annotation.DbmResultMapping;
import org.onetwo.dbm.jdbc.internal.ResultSetColumnValueGetter;
import org.onetwo.dbm.jdbc.spi.ColumnValueGetter;
import org.onetwo.dbm.jdbc.spi.JdbcResultSetGetter;
import org.onetwo.dbm.utils.DbmUtils;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.util.Assert;

public class DbmNestedBeanRowMapper<T> extends AbstractNestedBeanMapper<T> implements DataRowMapper<T> {

	protected JdbcResultSetGetter jdbcResultSetGetter;
	
	public DbmNestedBeanRowMapper(JdbcResultSetGetter jdbcResultSetGetter, Class<T> mappedClass, DbmResultMapping dbmResultMapping) {
		super(mappedClass, dbmResultMapping);
		this.jdbcResultSetGetter = jdbcResultSetGetter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		Assert.state(this.mappedClass != null, "Mapped class was not specified");
		ResultSetWrappingSqlRowSet resutSetWrapper = new ResultSetWrappingSqlRowSet(rs);
		SqlRowSetMetaData rsmd = resutSetWrapper.getMetaData();
		Map<String, Integer> names = DbmUtils.lookupColumnNames(rsmd);
		
		ColumnValueGetter columnValueGetter = new ResultSetColumnValueGetter(resutSetWrapper, jdbcResultSetGetter);
		T mappedObject = (T)this.resultClassMapper.mapResult(names, columnValueGetter, rowNum);
		return mappedObject;
	}

}
