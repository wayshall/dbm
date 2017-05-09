package org.onetwo.dbm.query;

import java.util.Optional;

import org.onetwo.common.db.DbmQueryWrapper;
import org.onetwo.common.db.dquery.NamedQueryInvokeContext;
import org.onetwo.common.db.filequery.DbmNamedQueryInfo;
import org.onetwo.common.db.filequery.DefaultFileQueryWrapper;
import org.onetwo.common.db.filequery.spi.CreateQueryCmd;
import org.onetwo.common.db.filequery.spi.QueryProvideManager;
import org.onetwo.common.utils.Assert;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;

public class DbmFileQueryWrapperImpl extends DefaultFileQueryWrapper {

	private NamedQueryInvokeContext invokeContext;
	

	public DbmFileQueryWrapperImpl(QueryProvideManager queryProvideManager, DbmNamedQueryInfo info, boolean count, NamedQueryInvokeContext invokeContext) {
		super(queryProvideManager, info, count, invokeContext.getParser());
		Assert.notNull(queryProvideManager);
		this.invokeContext = invokeContext;
		
	}
	
	protected DbmQueryWrapper createDataQuery(CreateQueryCmd createQueryCmd){
		DbmQueryWrapper dataQuery = this.baseEntityManager.createQuery(createQueryCmd);
		if(!countQuery){
			Optional<RowMapperFactory> rmfOpt = baseEntityManager.getRowMapperFactory();
			rmfOpt.ifPresent(rfm->{
				dataQuery.setRowMapper(rfm.createRowMapper(invokeContext));
			});
		}
		return dataQuery;
	}

	public NamedQueryInvokeContext getInvokeContext() {
		return invokeContext;
	}

}
