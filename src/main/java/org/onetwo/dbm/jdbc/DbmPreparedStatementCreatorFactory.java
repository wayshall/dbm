package org.onetwo.dbm.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.onetwo.dbm.jdbc.spi.JdbcStatementParameterSetter;
import org.onetwo.dbm.jdbc.spi.SqlParametersProvider;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.util.Assert;

/****
 * 复制自spring PreparedStatementCreatorFactory的实现，主要是增加JdbcStatementParameterSetter接口，统一参数设置入口
 * @author way
 *
 * Helper class that efficiently creates multiple {@link PreparedStatementCreator}
 * objects with different parameters based on a SQL statement and a single
 * set of parameter declarations.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Juergen Hoeller
 */
public class DbmPreparedStatementCreatorFactory {
	/** The SQL, which won't change when the parameters change */
	private final String sql;

	/** List of SqlParameter objects (may not be {@code null}) */
	private final List<SqlParameter> declaredParameters;

	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	private boolean updatableResults = false;

	private boolean returnGeneratedKeys = false;

	private String[] generatedKeysColumnNames = null;

//	private NativeJdbcExtractor nativeJdbcExtractor;
	
	private JdbcStatementParameterSetter parameterSetter;


	/**
	 * Create a new factory. Will need to add parameters via the
	 * {@link #addParameter} method or have no parameters.
	 */
	public DbmPreparedStatementCreatorFactory(String sql) {
		this.sql = sql;
		this.declaredParameters = new LinkedList<SqlParameter>();
	}

	/**
	 * Create a new factory with the given SQL and JDBC types.
	 * @param sql SQL to execute
	 * @param types int array of JDBC types
	 */
	public DbmPreparedStatementCreatorFactory(String sql, int... types) {
		this.sql = sql;
		this.declaredParameters = SqlParameter.sqlTypesToAnonymousParameterList(types);
	}

	/**
	 * Create a new factory with the given SQL and parameters.
	 * @param sql SQL
	 * @param declaredParameters list of {@link SqlParameter} objects
	 * @see SqlParameter
	 */
	public DbmPreparedStatementCreatorFactory(String sql, List<SqlParameter> declaredParameters) {
		this.sql = sql;
		this.declaredParameters = declaredParameters;
	}


	public void setParameterSetter(JdbcStatementParameterSetter parameterSetter) {
		this.parameterSetter = parameterSetter;
	}

	/**
	 * Add a new declared parameter.
	 * <p>Order of parameter addition is significant.
	 * @param param the parameter to add to the list of declared parameters
	 */
	public void addParameter(SqlParameter param) {
		this.declaredParameters.add(param);
	}

	/**
	 * Set whether to use prepared statements that return a specific type of ResultSet.
	 * @param resultSetType the ResultSet type
	 * @see java.sql.ResultSet#TYPE_FORWARD_ONLY
	 * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE
	 * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE
	 */
	public void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}

	/**
	 * Set whether to use prepared statements capable of returning updatable ResultSets.
	 */
	public void setUpdatableResults(boolean updatableResults) {
		this.updatableResults = updatableResults;
	}

	/**
	 * Set whether prepared statements should be capable of returning auto-generated keys.
	 */
	public void setReturnGeneratedKeys(boolean returnGeneratedKeys) {
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	/**
	 * Set the column names of the auto-generated keys.
	 */
	public void setGeneratedKeysColumnNames(String... names) {
		this.generatedKeysColumnNames = names;
	}

	/**
	 * Specify the NativeJdbcExtractor to use for unwrapping PreparedStatements, if any.
	 
	public void setNativeJdbcExtractor(NativeJdbcExtractor nativeJdbcExtractor) {
		this.nativeJdbcExtractor = nativeJdbcExtractor;
	}*/


	/**
	 * Return a new PreparedStatementSetter for the given parameters.
	 * @param params list of parameters (may be {@code null})
	 */
	public PreparedStatementSetter newPreparedStatementSetter(List<?> params) {
		return new PreparedStatementCreatorImpl(params != null ? params : Collections.emptyList());
	}

	/**
	 * Return a new PreparedStatementSetter for the given parameters.
	 * @param params the parameter array (may be {@code null})
	 */
	public PreparedStatementSetter newPreparedStatementSetter(Object[] params) {
		return new PreparedStatementCreatorImpl(params != null ? Arrays.asList(params) : Collections.emptyList());
	}

	/**
	 * Return a new PreparedStatementCreator for the given parameters.
	 * @param params list of parameters (may be {@code null})
	 */
	public PreparedStatementCreator newPreparedStatementCreator(List<?> params) {
		return new PreparedStatementCreatorImpl(params != null ? params : Collections.emptyList());
	}

	/**
	 * Return a new PreparedStatementCreator for the given parameters.
	 * @param params the parameter array (may be {@code null})
	 */
	public PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
		return new PreparedStatementCreatorImpl(params != null ? Arrays.asList(params) : Collections.emptyList());
	}

	/**
	 * Return a new PreparedStatementCreator for the given parameters.
	 * @param sqlToUse the actual SQL statement to use (if different from
	 * the factory's, for example because of named parameter expanding)
	 * @param params the parameter array (may be {@code null})
	 */
	public PreparedStatementCreator newPreparedStatementCreator(String sqlToUse, Object[] params) {
		return new PreparedStatementCreatorImpl(
				sqlToUse, params != null ? Arrays.asList(params) : Collections.emptyList());
	}


	/**
	 * PreparedStatementCreator implementation returned by this class.
	 */
	class PreparedStatementCreatorImpl
			implements PreparedStatementCreator, PreparedStatementSetter, SqlProvider, ParameterDisposer, SqlParametersProvider {

		private final String actualSql;

		private final List<?> parameters;

		public PreparedStatementCreatorImpl(List<?> parameters) {
			this(sql, parameters);
		}

		public PreparedStatementCreatorImpl(String actualSql, List<?> parameters) {
			this.actualSql = actualSql;
			Assert.notNull(parameters, "Parameters List must not be null");
			this.parameters = parameters;
			if (this.parameters.size() != declaredParameters.size()) {
				// account for named parameters being used multiple times
				Set<String> names = new HashSet<String>();
				for (int i = 0; i < parameters.size(); i++) {
					Object param = parameters.get(i);
					if (param instanceof SqlParameterValue) {
						names.add(((SqlParameterValue) param).getName());
					}
					else {
						names.add("Parameter #" + i);
					}
				}
				if (names.size() != declaredParameters.size()) {
					throw new InvalidDataAccessApiUsageException(
							"SQL [" + sql + "]: given " + names.size() +
							" parameters but expected " + declaredParameters.size());
				}
			}
		}

		@Override
		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			PreparedStatement ps;
			if (generatedKeysColumnNames != null || returnGeneratedKeys) {
				if (generatedKeysColumnNames != null) {
					ps = con.prepareStatement(this.actualSql, generatedKeysColumnNames);
				}
				else {
					ps = con.prepareStatement(this.actualSql, PreparedStatement.RETURN_GENERATED_KEYS);
				}
			}
			else if (resultSetType == ResultSet.TYPE_FORWARD_ONLY && !updatableResults) {
				ps = con.prepareStatement(this.actualSql);
			}
			else {
				ps = con.prepareStatement(this.actualSql, resultSetType,
					updatableResults ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY);
			}
			setValues(ps);
			return ps;
		}

		@Override
		public void setValues(PreparedStatement ps) throws SQLException {
			// Determine PreparedStatement to pass to custom types.
			PreparedStatement psToUse = ps;
			/*if (nativeJdbcExtractor != null) {
				psToUse = nativeJdbcExtractor.getNativePreparedStatement(ps);
			}*/

			// Set arguments: Does nothing if there are no parameters.
			int sqlColIndx = 1;
			for (int i = 0; i < this.parameters.size(); i++) {
				Object in = this.parameters.get(i);
				SqlParameter declaredParameter;
				// SqlParameterValue overrides declared parameter metadata, in particular for
				// independence from the declared parameter position in case of named parameters.
				if (in instanceof SqlParameterValue) {
					SqlParameterValue paramValue = (SqlParameterValue) in;
					in = paramValue.getValue();
					declaredParameter = paramValue;
				}
				else {
					if (declaredParameters.size() <= i) {
						throw new InvalidDataAccessApiUsageException(
								"SQL [" + sql + "]: unable to access parameter number " + (i + 1) +
								" given only " + declaredParameters.size() + " parameters");

					}
					declaredParameter = declaredParameters.get(i);
				}
				if (in instanceof Collection && declaredParameter.getSqlType() != Types.ARRAY) {
					Collection<?> entries = (Collection<?>) in;
					for (Object entry : entries) {
						if (entry instanceof Object[]) {
							Object[] valueArray = ((Object[])entry);
							for (Object argValue : valueArray) {
								setParameterValue(psToUse, sqlColIndx++, declaredParameter, argValue);
							}
						}
						else {
							setParameterValue(psToUse, sqlColIndx++, declaredParameter, entry);
						}
					}
				}
				else {
					setParameterValue(psToUse, sqlColIndx++, declaredParameter, in);
				}
			}
		}

		protected void setParameterValue(PreparedStatement psToUse, int paramIndex, SqlParameter declaredParameter, Object inValue) throws SQLException {
//			StatementCreatorUtils.setParameterValue(psToUse, paramIndex, declaredParameter, inValue);
			parameterSetter.setParameterValue(psToUse, paramIndex, declaredParameter, inValue);
		}

		@Override
		public String getSql() {
			return sql;
		}

		@Override
		public void cleanupParameters() {
			StatementCreatorUtils.cleanupParameters(this.parameters);
		}

		public String getActualSql() {
			return actualSql;
		}

		@Override
		public Object[] getSqlParameters() {
			return parameters.toArray();
		}

		@Override
		public List<?> getSqlParameterList() {
			return parameters;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("DbmPreparedStatementCreatorFactory.PreparedStatementCreatorImpl: sql=[");
			sb.append(sql).append("]; parameters=").append(this.parameters);
			return sb.toString();
		}
	}
}
