package org.onetwo.common.db.builder;

import org.onetwo.common.db.InnerBaseEntityManager;
import org.onetwo.common.db.sqlext.DeleteExtQuery;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.onetwo.dbm.exception.DbmException;

public class ExecuteActionImpl implements ExecuteAction {

	protected InnerBaseEntityManager baseEntityManager;
//	private ExtQueryInner extQuery;
	final private QueryBuilder<?> queryBuilder;
	
	public ExecuteActionImpl(QueryBuilderImpl<?> queryBuilder){
		if(queryBuilder.getBaseEntityManager()==null){
			throw new DbmException("to create QueryAction, the baseEntityManager can not be null!");
		}
		this.queryBuilder = queryBuilder;
		this.baseEntityManager = queryBuilder.getBaseEntityManager();
		
	}

	protected SQLSymbolManager getSQLSymbolManager() {
//		SQLSymbolManager symbolManager = SQLSymbolManagerFactory.getInstance().getJdbc();
		return this.baseEntityManager.getSQLSymbolManager();
	}

	protected void checkOperation() {
		if(this.baseEntityManager==null)
			throw new UnsupportedOperationException("no entityManager");
//		this.build();
	}

	@Override
	public int delete() {
		checkOperation();
		DeleteExtQuery extQuery = getSQLSymbolManager().createDeleteQuery(this.queryBuilder.getEntityClass(), this.queryBuilder.getParams());
		int deleteCount = baseEntityManager.remove(extQuery);
		return deleteCount;
	}

	protected DbmEntityManager getDbmEntityManager(){
		return (DbmEntityManager) getBaseEntityManager();
	}

	public InnerBaseEntityManager getBaseEntityManager() {
		return baseEntityManager;
	}

}
