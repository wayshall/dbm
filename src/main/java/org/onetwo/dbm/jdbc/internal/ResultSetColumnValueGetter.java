package org.onetwo.dbm.jdbc.internal;

import java.sql.ResultSet;

import org.onetwo.dbm.jdbc.spi.ColumnValueGetter;
import org.onetwo.dbm.jdbc.spi.JdbcResultSetGetter;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;

/**
 * @author wayshall
 * <br/>
 */
public class ResultSetColumnValueGetter implements ColumnValueGetter {

	private ResultSetWrappingSqlRowSet rowSet;
	private JdbcResultSetGetter jdbcResultSetGetter;
	
	
	public ResultSetColumnValueGetter(ResultSet resultSet, JdbcResultSetGetter jdbcResultSetGetter) {
		super();
		this.rowSet = new ResultSetWrappingSqlRowSet(resultSet);
		this.jdbcResultSetGetter = jdbcResultSetGetter;
	}

	public ResultSetColumnValueGetter(ResultSetWrappingSqlRowSet rowSet, JdbcResultSetGetter jdbcResultSetGetter) {
		super();
		this.rowSet = rowSet;
		this.jdbcResultSetGetter = jdbcResultSetGetter;
	}

	@Override
	public Object getColumnValue(int index, Class<?> requiredType) {
		return jdbcResultSetGetter.getColumnValue(rowSet, index, requiredType);
	}

	@Override
	public Object getColumnValue(int index, DbmMappedField field) {
		return jdbcResultSetGetter.getColumnValue(rowSet, index, field);
	}
	
	

}
