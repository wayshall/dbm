package org.onetwo.dbm.query;

import org.onetwo.common.db.dquery.NamedQueryInvokeContext;
import org.onetwo.common.db.filequery.DefaultFileQueryWrapper;
import org.onetwo.common.db.spi.CreateQueryCmd;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.utils.Assert;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;

public class DbmFileQueryWrapperImpl extends DefaultFileQueryWrapper {

	private NamedQueryInvokeContext invokeContext;
	

	public DbmFileQueryWrapperImpl(QueryProvideManager queryProvideManager, NamedQueryInfo info, boolean count, NamedQueryInvokeContext invokeContext) {
		super(queryProvideManager, info, count, invokeContext.getParser());
		Assert.notNull(queryProvideManager);
		this.invokeContext = invokeContext;
		
	}
	
	protected QueryWrapper createDataQuery(CreateQueryCmd createQueryCmd){
		QueryWrapper dataQuery = this.baseEntityManager.createQuery(createQueryCmd);
		if(!countQuery && getDbmEntityManager().isPresent()){
			RowMapperFactory rfm = getDbmEntityManager().get().getRowMapperFactory();
			dataQuery.setRowMapper(rfm.createRowMapper(invokeContext));
		}
		return dataQuery;
	}

	public NamedQueryInvokeContext getInvokeContext() {
		return invokeContext;
	}

}
