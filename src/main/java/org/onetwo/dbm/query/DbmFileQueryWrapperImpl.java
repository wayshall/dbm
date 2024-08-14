package org.onetwo.dbm.query;

import java.util.Optional;

import org.onetwo.common.db.dquery.NamedQueryInvokeContext;
import org.onetwo.common.db.filequery.DefaultFileQueryWrapper;
import org.onetwo.common.db.spi.CreateQueryCmd;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;

public class DbmFileQueryWrapperImpl extends DefaultFileQueryWrapper {

	public DbmFileQueryWrapperImpl(NamedQueryInvokeContext invokeContext, boolean count) {
		super(invokeContext, count);
	}
	
	protected QueryWrapper createDataQuery(CreateQueryCmd createQueryCmd){
		// DbmQueryWrapperImpl
		QueryWrapper dataQuery = this.queryProvideManager.createQuery(createQueryCmd);
		Optional<DbmEntityManager> dbm = getProviderManager(DbmEntityManager.class);
		if(!countQuery && dbm.isPresent()){
			RowMapperFactory rfm = dbm.get().getRowMapperFactory();
			dataQuery.setRowMapper(rfm.createRowMapper(invokeContext));
		}
		return dataQuery;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getProviderManager(Class<T> providerClass){
		if(providerClass.isInstance(queryProvideManager)){
			return Optional.of((T)queryProvideManager);
		}
		return Optional.empty();
	}

	public NamedQueryInvokeContext getInvokeContext() {
		return invokeContext;
	}

}
