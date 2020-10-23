package org.onetwo.dbm.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public interface DbmMapRowMapperResultSetExtractor<K, V> extends ResultSetExtractor<Map<K, V>> {

	@Override
	default public Map<K, V> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<K, V> results = new HashMap<>();
		int rowNum = 0;
		while (rs.next()) {
			putToMap(results, rs, rowNum);
			rowNum++;
		}
		return results;
	}
	
	void putToMap(Map<K, V> results, ResultSet rs, int rowNum) throws SQLException, DataAccessException;

}
