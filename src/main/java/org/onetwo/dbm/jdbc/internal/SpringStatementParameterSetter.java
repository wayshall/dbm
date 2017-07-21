package org.onetwo.dbm.jdbc.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.onetwo.dbm.jdbc.spi.JdbcStatementParameterSetter;
import org.onetwo.dbm.utils.DbmUtils;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

/***
 * TODO: 设置参数时，如果是枚举类型会有问题，是否做枚举类型自动转？
 * @author wayshall
 *
 */
public class SpringStatementParameterSetter implements JdbcStatementParameterSetter {

	@Override
	public void setParameterValue(PreparedStatement psToUse, int paramIndex, SqlParameter declaredParameter, Object inValue) throws SQLException {
		StatementCreatorUtils.setParameterValue(psToUse, paramIndex, declaredParameter, inValue);
	}

	@Override
	public void setParameterValue(PreparedStatement ps, int parameterPosition, Object argValue) throws SQLException {
		if (argValue instanceof SqlParameterValue) {
			SqlParameterValue paramValue = (SqlParameterValue) argValue;
			StatementCreatorUtils.setParameterValue(ps, parameterPosition, paramValue, DbmUtils.getActualValue(paramValue.getValue()));
		}
		else {
			StatementCreatorUtils.setParameterValue(ps, parameterPosition, SqlTypeValue.TYPE_UNKNOWN, DbmUtils.getActualValue(argValue));
		}
	}

	@Override
	public void setParameterValue(PreparedStatement ps, int parameterPosition, int argType, Object argValue) throws SQLException {
		/*JDBCType jdbcType = JDBCType.valueOf(argType);
		TypeHandler<Object> typeHandler = (TypeHandler<Object>)dialect.getTypeMapping().getTypeHander(argValue.getClass(), jdbcType);
		typeHandler.setParameter(ps, parameterPosition, argValue, jdbcType);*/
		StatementCreatorUtils.setParameterValue(ps, parameterPosition, argType, argValue);
	}
	
	

}
