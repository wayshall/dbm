package org.onetwo.dbm.jdbc.method;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.onetwo.common.proxy.AbstractMethodResolver;
import org.onetwo.common.proxy.BaseMethodParameter;
import org.onetwo.dbm.jdbc.annotation.DbmJdbcArgsMark;
import org.onetwo.dbm.jdbc.annotation.DbmJdbcOperationMark;
import org.onetwo.dbm.jdbc.annotation.DbmJdbcSqlMark;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jdbc.core.SqlProvider;

/**
 * @author weishao zeng
 * <br/>
 */
public class JdbcOperationMethod extends AbstractMethodResolver<BaseMethodParameter> {
	
	private BaseMethodParameter sqlParameter;
	private BaseMethodParameter sqlArgsParameter;
	private BaseMethodParameter sqlProviderParameter;
	private DbmJdbcOperationMark jdbcOperationMark;

	public JdbcOperationMethod(Method method) {
		super(method);
		this.jdbcOperationMark = AnnotationUtils.findAnnotation(method, DbmJdbcOperationMark.class);
		for (BaseMethodParameter p : this.parameters) {
			if (p.hasParameterAnnotation(DbmJdbcSqlMark.class)) {
				this.sqlParameter = p;
			} else if (p.hasParameterAnnotation(DbmJdbcArgsMark.class)) {
				this.sqlArgsParameter = p;
			} else if (SqlProvider.class.isAssignableFrom(p.getParameterType())) {
				this.sqlProviderParameter = p;
			}
		}
	}

	@Override
	protected BaseMethodParameter createMethodParameter(Method method, int parameterIndex, Parameter parameter) {
		return new BaseMethodParameter(method, parameter, parameterIndex);
	}

	public BaseMethodParameter getSqlParameter() {
		return sqlParameter;
	}

	public BaseMethodParameter getSqlArgsParameter() {
		return sqlArgsParameter;
	}

	public BaseMethodParameter getSqlProviderParameter() {
		return sqlProviderParameter;
	}

	public DbmJdbcOperationMark getJdbcOperationMark() {
		return jdbcOperationMark;
	}

}
