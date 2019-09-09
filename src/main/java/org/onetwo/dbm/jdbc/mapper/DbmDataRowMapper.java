package org.onetwo.dbm.jdbc.mapper;

import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.dbm.jdbc.spi.JdbcResultSetGetter;
import org.slf4j.Logger;

/**
 * @author weishao zeng
 * <br/>
 */
abstract public class DbmDataRowMapper<T> implements DataRowMapper<T> {
	final protected Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
//	protected ConversionService conversionService = DbmUtils.CONVERSION_SERVICE;
	protected JdbcResultSetGetter jdbcResultSetGetter;
	

	public DbmDataRowMapper(JdbcResultSetGetter jdbcResultSetGetter) {
		super();
		this.jdbcResultSetGetter = jdbcResultSetGetter;
	}



	public JdbcResultSetGetter getJdbcResultSetGetter() {
		return jdbcResultSetGetter;
	}
	
}
