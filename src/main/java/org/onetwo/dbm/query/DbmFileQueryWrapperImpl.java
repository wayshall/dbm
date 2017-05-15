package org.onetwo.dbm.query;

import java.util.Optional;

import org.onetwo.common.db.dquery.NamedQueryInvokeContext;
import org.onetwo.common.db.filequery.DefaultFileQueryWrapper;
import org.onetwo.common.db.spi.CreateQueryCmd;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.utils.Assert;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;

public class DbmFileQueryWrapperImpl extends DefaultFileQueryWrapper {

	private NamedQueryInvokeContext invokeContext;
	

	public DbmFileQueryWrapperImpl(NamedQueryInfo info, boolean count, NamedQueryInvokeContext invokeContext) {
		super(invokeContext.getQueryProvideManager(), info, count, invokeContext.getParser());
		Assert.notNull(invokeContext.getQueryProvideManager());
		this.invokeContext = invokeContext;
		
	}
	
	protected QueryWrapper createDataQuery(CreateQueryCmd createQueryCmd){
		QueryWrapper dataQuery = this.baseEntityManager.createQuery(createQueryCmd);
		if(!countQuery && getDbmEntityManager().isPresent()){a result mapper
			RowMapperFactory rfm = getDbmEntityManager().get().getRowMapperFactory();
			dataQuery.setRowMapper(rfm.createRowMapper(invokeContext));
		}
		return dataQuery;
	}
	
	public Optional<DbmEntityManager> getDbmEntityManager(){
		if(DbmEntityManager.class.isInstance(baseEntityManager)){
			return Optional.of((DbmEntityManager)baseEntityManager);
		}
		return Optional.empty();
	}

	public NamedQueryInvokeContext getInvokeContext() {
		return invokeContext;
	}

}
