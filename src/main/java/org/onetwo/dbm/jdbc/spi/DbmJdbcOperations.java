package org.onetwo.dbm.jdbc.spi;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.onetwo.dbm.jdbc.DbmNamedJdbcTemplate;
import org.onetwo.dbm.jdbc.annotation.DbmJdbcArgsMark;
import org.onetwo.dbm.jdbc.annotation.DbmJdbcOperationMark;
import org.onetwo.dbm.jdbc.annotation.DbmJdbcSqlMark;
import org.onetwo.dbm.jdbc.internal.SimpleArgsPreparedStatementCreator;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

public interface DbmJdbcOperations /*extends JdbcOperations*/ {
	
	DbmNamedJdbcTemplate getDbmNamedJdbcOperations();
	
	@DbmJdbcOperationMark(type=DbmJdbcOperationType.QUERY)
	<T> T query(@DbmJdbcSqlMark String sql, @DbmJdbcArgsMark Map<String, ?> paramMap, ResultSetExtractor<T> rse) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.QUERY)
	<T> List<T> query(@DbmJdbcSqlMark String sql, @DbmJdbcArgsMark Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.QUERY)
	<T> T queryForObject(@DbmJdbcSqlMark String sql, @DbmJdbcArgsMark Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException;
	
//	<T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException;
	/***
	 * 
	 * @param sql
	 * @param elementType SingleColumnRowMapper
	 * @param args
	 * @return
	 * @throws DataAccessException
	 */
	@DbmJdbcOperationMark(type=DbmJdbcOperationType.QUERY)
	<T> List<T> queryForList(@DbmJdbcSqlMark String sql, Class<T> elementType, @DbmJdbcArgsMark Object... args) throws DataAccessException;

	/***
	 * 
	 * @param sql
	 * @param requiredType SingleColumnRowMapper
	 * @param args
	 * @return
	 * @throws DataAccessException
	 */
	@DbmJdbcOperationMark(type=DbmJdbcOperationType.QUERY)
	<T> T queryForObject(@DbmJdbcSqlMark String sql, Class<T> requiredType, @DbmJdbcArgsMark Object... args) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.QUERY)
	<T> T queryForObject(@DbmJdbcSqlMark String sql, @DbmJdbcArgsMark Object[] args, RowMapper<T> rowMapper) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.QUERY)
	<T> List<T> query(@DbmJdbcSqlMark String sql, @DbmJdbcArgsMark Object[] args, RowMapper<T> rowMapper) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.EXECUTE)
	void execute(@DbmJdbcSqlMark String sql) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.EXECUTE)
	Object execute(@DbmJdbcSqlMark String sql, @DbmJdbcArgsMark Map<String, ?> paramMap) throws DataAccessException ;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.UPDATE)
	int updateWith(final SimpleArgsPreparedStatementCreator spsc, final KeyHolder generatedKeyHolder) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.UPDATE)
	int updateWith(final SimpleArgsPreparedStatementCreator spsc) throws DataAccessException;

//	@DbmJdbcOperationMark(type=DbmJdbcOperationType.UPDATE)
//	int updateWith(final SimpleArgsPreparedStatementCreator spsc, final AroundPreparedStatementExecute action) throws DataAccessException;

//	@DbmJdbcOperationMark(type=DbmJdbcOperationType.UPDATE)
//	int updateWith(String sql, Object[] args, final AroundPreparedStatementExecute action) throws DataAccessException;

	
	@DbmJdbcOperationMark(type=DbmJdbcOperationType.BATCH_UPDATE)
	int[] batchUpdate(@DbmJdbcSqlMark String sql, List<Map<String, ?>> batchValues, int processSizePerBatch) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.UPDATE)
	int update(@DbmJdbcSqlMark String sql, @DbmJdbcArgsMark Object... args) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.UPDATE)
	int update(@DbmJdbcSqlMark String sql, @DbmJdbcArgsMark Map<String, ?> paramMap) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.BATCH_UPDATE)
	<T> int[][] batchUpdateWith(@DbmJdbcSqlMark String sql, @DbmJdbcArgsMark Collection<T[]> batchArgs, int batchSize) throws DataAccessException;

	@DbmJdbcOperationMark(type=DbmJdbcOperationType.BATCH_UPDATE)
	int[] batchUpdate(@DbmJdbcSqlMark String sql, @DbmJdbcArgsMark Map<String, ?>[] batchValues) throws DataAccessException;
	
//	void setDataSource(DataSource dataSource);

	DataSource getDataSource();
	
//	DbmNamedJdbcOperations getDbmNamedJdbcOperations();

}