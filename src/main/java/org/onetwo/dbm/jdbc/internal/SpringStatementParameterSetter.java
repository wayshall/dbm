package org.onetwo.dbm.jdbc.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.onetwo.dbm.jdbc.spi.JdbcStatementParameterSetter;
import org.onetwo.dbm.utils.JdbcParamValueConvers;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

/***
 * @author wayshall
 *
 */
public class SpringStatementParameterSetter implements JdbcStatementParameterSetter {

	@Override
	public void setParameterValue(PreparedStatement psToUse, int paramIndex, SqlParameter declaredParameter, final Object argValue) throws SQLException {
		Object inValue = JdbcParamValueConvers.getActualValue(argValue);
		StatementCreatorUtils.setParameterValue(psToUse, paramIndex, declaredParameter, inValue);
	}

	@Override
	public void setParameterValue(PreparedStatement ps, int parameterPosition, Object argValue) throws SQLException {
		if (argValue instanceof SqlParameterValue) {
			SqlParameterValue paramValue = (SqlParameterValue) argValue;
			StatementCreatorUtils.setParameterValue(ps, parameterPosition, paramValue, JdbcParamValueConvers.getActualValue(paramValue.getValue()));
		}
		else {
			StatementCreatorUtils.setParameterValue(ps, parameterPosition, SqlTypeValue.TYPE_UNKNOWN, JdbcParamValueConvers.getActualValue(argValue));
		}
	}

	@Override
	public void setParameterValue(PreparedStatement ps, int parameterPosition, int argType, final Object argValue) throws SQLException {
		/*JDBCType jdbcType = JDBCType.valueOf(argType);
		TypeHandler<Object> typeHandler = (TypeHandler<Object>)dialect.getTypeMapping().getTypeHander(argValue.getClass(), jdbcType);
		typeHandler.setParameter(ps, parameterPosition, argValue, jdbcType);*/
		Object inValue = JdbcParamValueConvers.getActualValue(argValue);
		StatementCreatorUtils.setParameterValue(ps, parameterPosition, argType, inValue);
	}
	
	

}
