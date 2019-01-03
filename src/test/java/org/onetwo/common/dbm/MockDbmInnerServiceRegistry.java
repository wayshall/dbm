package org.onetwo.common.dbm;

import java.util.Arrays;

import org.onetwo.common.db.spi.SqlParamterPostfixFunctionRegistry;
import org.onetwo.common.db.sql.SequenceNameManager;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.dbm.core.internal.DbmInterceptorManager;
import org.onetwo.dbm.core.internal.SimpleDbmInnerServiceRegistry.DbmServiceRegistryCreateContext;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.dialet.DefaultDatabaseDialetManager;
import org.onetwo.dbm.dialet.MySQLDialect;
import org.onetwo.dbm.event.internal.EdgeEventBus;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;
import org.onetwo.dbm.jdbc.spi.DbmJdbcOperations;
import org.onetwo.dbm.jdbc.spi.JdbcStatementParameterSetter;
import org.onetwo.dbm.jpa.JPAMappedEntryBuilder;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.mapping.DbmTypeMapping;
import org.onetwo.dbm.mapping.EntityValidator;
import org.onetwo.dbm.mapping.MappedEntryManager;
import org.onetwo.dbm.mapping.MutilMappedEntryManager;

/**
 * @author weishao zeng <br/>
 */
public class MockDbmInnerServiceRegistry implements DbmInnerServiceRegistry {

	private DBDialect dialect = new MySQLDialect();
	private MutilMappedEntryManager mappedEntryManager;

	@Override
	public void initialize(DbmServiceRegistryCreateContext context) {
		this.dialect.initialize();
		
		mappedEntryManager = new MutilMappedEntryManager(this);
		JPAMappedEntryBuilder jeb = new JPAMappedEntryBuilder(this);
		jeb.initialize();
		mappedEntryManager.setMappedEntryBuilder(Arrays.asList(jeb));
	}

	@Override
	public DBDialect getDialect() {
		return dialect;
	}

	@Override
	public DbmJdbcOperations getDbmJdbcOperations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JdbcStatementParameterSetter getJdbcParameterSetter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbmInterceptorManager getInterceptorManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MappedEntryManager getMappedEntryManager() {
		return mappedEntryManager;
	}

	@Override
	public SQLSymbolManager getSqlSymbolManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SequenceNameManager getSequenceNameManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DefaultDatabaseDialetManager getDatabaseDialetManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbmConfig getDataBaseConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RowMapperFactory getRowMapperFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getService(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getService(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> DbmInnerServiceRegistry register(T service) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> DbmInnerServiceRegistry register(String name, T service) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityValidator getEntityValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbmTypeMapping getTypeMapping() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SqlParamterPostfixFunctionRegistry getSqlParamterPostfixFunctionRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EdgeEventBus getEdgeEventBus() {
		// TODO Auto-generated method stub
		return null;
	}

}
