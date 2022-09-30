package org.onetwo.dbm.jdbc.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.dbm.jdbc.AroundPreparedStatementExecute;
import org.onetwo.dbm.jdbc.DbmListRowMapperResultSetExtractor;
import org.onetwo.dbm.jdbc.DbmNamedJdbcTemplate;
import org.onetwo.dbm.jdbc.spi.DbmJdbcOperations;
import org.onetwo.dbm.jdbc.spi.JdbcStatementParameterSetter;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.Assert;

public class DbmJdbcTemplate extends JdbcTemplate implements DbmJdbcOperations {
	
	private final Logger logger = JFishLoggerFactory.getLogger(this.getClass());

	private final DbmListRowMapperResultSetExtractor<Map<String, Object>> generatedKeysExtractor = new DbmListRowMapperResultSetExtractor<Map<String, Object>>(getColumnMapRowMapper(), 1);
	protected DbmNamedJdbcTemplate dbmNamedJdbcOperations;
	private JdbcStatementParameterSetter jdbcParameterSetter;

	/*public DbmJdbcTemplate() {
	}
*/
	public DbmJdbcTemplate(DataSource dataSource) {
		this(dataSource, new SpringStatementParameterSetter());
	}
	
	public DbmJdbcTemplate(DataSource dataSource, JdbcStatementParameterSetter jdbcParameterSetter) {
		super(dataSource);
		this.jdbcParameterSetter = jdbcParameterSetter;
	}
	
	/*public void setJdbcParameterSetter(JdbcStatementParameterSetter jdbcParameterSetter) {
		this.jdbcParameterSetter = jdbcParameterSetter;
	}*/

	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		this.initTemplateConfig();
	}

	protected void initTemplateConfig() {
		if(dbmNamedJdbcOperations==null){
			DbmNamedJdbcTemplate t = new DbmNamedJdbcTemplate(this);
			this.dbmNamedJdbcOperations = t;
		}
	}
	
	@Override
	public DbmNamedJdbcTemplate getDbmNamedJdbcOperations() {
		return dbmNamedJdbcOperations;
	}
	
	@Override
	public int updateWith(final SimpleArgsPreparedStatementCreator spsc, final KeyHolder generatedKeyHolder) throws DataAccessException {
		return updateWith(spsc, new AroundPreparedStatementExecute() {
			
			@Override
			public void afterExecute(PreparedStatement ps, int rows) throws SQLException {
				if(generatedKeyHolder==null)
					return ;
				List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
				generatedKeys.clear();
				ResultSet keys = ps.getGeneratedKeys();
				if (keys != null) {
					try {
//						DbmListRowMapperResultSetExtractor<Map<String, Object>> rse = new DbmListRowMapperResultSetExtractor<Map<String, Object>>(getColumnMapRowMapper(), 1);
						generatedKeys.addAll(generatedKeysExtractor.extractData(keys));
					}
					finally {
						JdbcUtils.closeResultSet(keys);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("SQL update affected " + rows + " rows and returned " + generatedKeys.size() + " keys");
				}
				
			}
		});
	}

	@Override
	public int updateWith(final SimpleArgsPreparedStatementCreator spsc) throws DataAccessException {
		final PreparedStatementSetter pss = this.newArgPreparedStatementSetter(spsc.getSqlParameters());
		return update(spsc, pss);
	}
	
	@Override
	public int updateWith(final SimpleArgsPreparedStatementCreator spsc, final AroundPreparedStatementExecute action) throws DataAccessException {
		final PreparedStatementSetter pss = this.newArgPreparedStatementSetter(spsc.getSqlParameters());
		return execute(spsc, new PreparedStatementCallback<Integer>() {
			public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException {
				try {
					if(action!=null)
						action.beforeExecute(pss, ps);
					else
						pss.setValues(ps);
					
					int rows = ps.executeUpdate();
					if (logger.isDebugEnabled()) {
						logger.debug("SQL update affected " + rows + " rows");
					}
					
					if(action!=null)
						action.afterExecute(ps, rows);
					
					return rows;
				}
				finally {
					if (pss instanceof ParameterDisposer) {
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		});
	}
	
	
	@Override
	public int updateWith(String sql, Object[] args, final AroundPreparedStatementExecute action) throws DataAccessException {
		SimpleArgsPreparedStatementCreator psc = new SimpleArgsPreparedStatementCreator(sql, args);
		return updateWith(psc, action);
	}
	
	public <T> int[][] batchUpdateWith(String sql, Collection<T[]> batchArgs, int batchSize) throws DataAccessException {
		int[][] ups = super.batchUpdate(sql, batchArgs, batchSize, new ParameterizedPreparedStatementSetter<T[]>(){

			@Override
			public void setValues(PreparedStatement ps, T[] args) throws SQLException {
				if (args == null) {
					return ;
				}
				newArgPreparedStatementSetter(args).setValues(ps);
			}
			
		});
		return ups;
	}


	/****
	 * for update*
	 * to execute
	 * 
	 * psc SqlProvider
	 * pss SqlParametersProvider
	 */
	@Override
	protected int update(final PreparedStatementCreator psc, final PreparedStatementSetter pss) throws DataAccessException {
		return super.update(psc, pss);
	}
	
	/*****
	 * for query*
	 * to execute
	 */
	public <T> T query(PreparedStatementCreator psc, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) throws DataAccessException {

		Assert.notNull(rse, "ResultSetExtractor must not be null");
		logger.debug("Executing prepared SQL query");

		return execute(psc, new PreparedStatementCallback<T>() {
			public T doInPreparedStatement(PreparedStatement ps) throws SQLException {
				ResultSet rs = null;
				try {
					if (pss != null) {
						pss.setValues(ps);
					}
					long queryStart = System.currentTimeMillis();
					rs = ps.executeQuery();
					long quyeryEnd = System.currentTimeMillis();
					
					ResultSet rsToUse = rs;
					if (getNativeJdbcExtractor() != null) {
						rsToUse = getNativeJdbcExtractor().getNativeResultSet(rs);
					}
					
					T result = rse.extractData(rsToUse);
					
					if(logger.isDebugEnabled()){
						logger.debug("===>>> executeQuery cost time (milliseconds): " + (quyeryEnd-queryStart));
						long costTime = System.currentTimeMillis()-quyeryEnd;
						logger.debug("===>>> extractData cost time (milliseconds): " + costTime);
					}
					
					return result;
				}
				finally {
					JdbcUtils.closeResultSet(rs);
					if (pss instanceof ParameterDisposer) {
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		});
	}
	

	//-------------------------------------------------------------------------
	// final execute call
	//-------------------------------------------------------------------------
	
	@Override
	public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException {
		return super.execute(psc, action);
	}
	
	@Override
	public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException {
		return super.execute(csc, action);
	}
	@Override
	public <T> T execute(StatementCallback<T> action) throws DataAccessException {
		return super.execute(action);
	}
	

	@Override
	public <T> T execute(ConnectionCallback<T> action) throws DataAccessException {
		return super.execute(action);
	}

	@Override
	protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
		return new DbmArgumentPreparedStatementSetter(jdbcParameterSetter, args);
	}
	
	protected PreparedStatementSetter newArgTypePreparedStatementSetter(Object[] args, int[] argTypes) {
		return new ArgumentTypePreparedStatementSetter(args, argTypes){
			protected void doSetValue(PreparedStatement ps, int parameterPosition, int argType, Object argValue) throws SQLException {
				jdbcParameterSetter.setParameterValue(ps, parameterPosition, argType, argValue);
			}
		};
	}

	
	
	
	//------------------------------not dbmJdbcOperations----------------------------------//
	
	/****
	 * use DbmListRowMapperResultSetExtractor instead of RowMapperResultSetExtractor 
	 */
	@Override
	public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		return query(sql, new DbmListRowMapperResultSetExtractor<T>(rowMapper));
	}

	@Override
	public <T> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException {
		return query(psc, new DbmListRowMapperResultSetExtractor<T>(rowMapper));
	}

	@Override
	public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
		return query(sql, args, new DbmListRowMapperResultSetExtractor<T>(rowMapper));
	}
	@Override
	public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
		return query(sql, args, new DbmListRowMapperResultSetExtractor<T>(rowMapper));
	}

	@Override
	public int[] batchUpdate(String sql, Map<String, ?>[] batchValues) {
		return this.dbmNamedJdbcOperations.batchUpdate(sql, batchValues);
	}
	
	private int[] batchUpdateList(String sql, List<Map<String, ?>> batchValues) {
		return this.dbmNamedJdbcOperations.batchUpdate(sql, DbmUtils.createBatch(batchValues));
	}
	
	public int[] batchUpdate(String sql, List<Map<String, ?>> batchValues, int processSizePerBatch) {
		int insertSize = batchValues.size();
		if (processSizePerBatch==-1 || insertSize<=processSizePerBatch) {
			return batchUpdateList(sql, batchValues);
		}
		int startIndexInclusive = 0;
		int endIndex = 0;
		int[] res = null;
		while(endIndex<insertSize) {
			endIndex = startIndexInclusive + processSizePerBatch;
			if (endIndex>insertSize) {
				endIndex = insertSize;
			}
			List<Map<String, ?>> values = batchValues.subList(startIndexInclusive, endIndex);
			int[] inserted = this.batchUpdateList(sql, values);
			res = ArrayUtils.addAll(res, inserted);
			startIndexInclusive = endIndex;
		}
		return res;
	}

	@Override
	public int update(String sql, Map<String, ?> paramMap) throws DataAccessException {
		return this.dbmNamedJdbcOperations.update(sql, paramMap);
	}

	@Override
	public <T> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse) throws DataAccessException {
		return this.dbmNamedJdbcOperations.query(sql, paramMap, rse);
	}

	@Override
	public <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException {
		return this.dbmNamedJdbcOperations.query(sql, paramMap, rowMapper);
	}

	/****
	 * 返回结果会调用  DataAccessUtils.requiredSingleResult 过滤
	 */
	@Override
	public <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException {
		return this.dbmNamedJdbcOperations.queryForObject(sql, paramMap, rowMapper);
	}

	@Override
	public Object execute(String sql, Map<String, ?> paramMap) throws DataAccessException {
		return this.dbmNamedJdbcOperations.execute(sql, paramMap);
	}
	
	protected Connection getConnection(){
		Connection con = DataSourceUtils.getConnection(getDataSource());
		return con;
	}

	protected void closeConnection(Connection con){
		DataSourceUtils.releaseConnection(con, getDataSource());
	}
	

}
