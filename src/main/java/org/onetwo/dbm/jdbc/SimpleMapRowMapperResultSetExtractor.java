package org.onetwo.dbm.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.onetwo.common.utils.Tuple;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public interface SimpleMapRowMapperResultSetExtractor<K, V> extends ResultSetExtractor<Map<K, V>> {

	@Override
	default public Map<K, V> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<K, V> results = new HashMap<>();
		while (rs.next()) {
			Tuple<K, V> rowTuple = createRow(rs);
			results.put(rowTuple.getKey(), rowTuple.getValue());
		}
		return results;
	}
	
	Tuple<K, V> createRow(ResultSet res) throws SQLException, DataAccessException;

}
