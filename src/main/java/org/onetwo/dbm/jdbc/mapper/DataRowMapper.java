package org.onetwo.dbm.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.onetwo.dbm.exception.DbmException;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author weishao zeng
 * <br/>
 */
public interface DataRowMapper<T> extends RowMapper<T> {
	
	/*default void setColumnValue(ResultSetWrappingSqlRowSet resutSetWrapper, 
			BeanWrapper bw, 
			int rowNumber, 
			int columnIndex, 
			String column) {
		throw new DbmException(this.getClass() + " not supported operation!");
	}
	*/

	final public class NoDataRowMapper implements DataRowMapper<Object> {

		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			throw new DbmException("not supported operation!");
		}
		
	}
}
