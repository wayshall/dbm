package org.onetwo.dbm.jdbc.mapper;

import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;

/**
 * @author weishao zeng
 * <br/>
 */
public interface DataColumnMapper {
	void setColumnValue(ResultSetWrappingSqlRowSet resutSetWrapper, 
			BeanWrapper bw, 
			int rowNumber, 
			int columnIndex, 
			String column);
}
