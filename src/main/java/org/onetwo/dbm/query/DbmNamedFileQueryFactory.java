package org.onetwo.dbm.query;

import java.util.List;

import org.onetwo.common.db.dquery.NamedQueryInvokeContext;
import org.onetwo.common.db.filequery.AbstractFileNamedQueryFactory;
import org.onetwo.common.db.filequery.DbmNamedSqlFileManager;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.Page;
import org.springframework.jdbc.core.RowMapper;


public class DbmNamedFileQueryFactory extends AbstractFileNamedQueryFactory {


	public DbmNamedFileQueryFactory(DbmNamedSqlFileManager sqlFileManager) {
		super(sqlFileManager);
	}


	@Override
	public QueryWrapper createQuery(NamedQueryInvokeContext invokeContext){
		return createDataQuery(false, invokeContext);
	}
	
	public QueryWrapper createCountQuery(NamedQueryInvokeContext invokeContext){
		return createDataQuery(true, invokeContext);
	}

	public QueryWrapper createDataQuery(boolean count, NamedQueryInvokeContext invokeContext){
//		public JFishDataQuery createDataQuery(boolean count, String queryName, PlaceHolder type, Object... args){
		Assert.notNull(invokeContext);
		QueryWrapper jq = newQueryWrapperInstance(count, invokeContext);
		jq.setQueryConfig(invokeContext.getParsedParams());
//		jq.setRowMapper(rowMapper);
		return jq.getRawQuery(QueryWrapper.class);
	}
	
	protected QueryWrapper newQueryWrapperInstance(boolean count, NamedQueryInvokeContext invokeContext) {
		return new DbmFileQueryWrapperImpl(invokeContext, count);
	}
	
	@Override
	public <T> List<T> findList(NamedQueryInvokeContext invokeContext) {
		QueryWrapper jq = this.createQuery(invokeContext);
		return jq.getResultList();
	}


	@Override
	public <T> T findUnique(NamedQueryInvokeContext invokeContext) {
		QueryWrapper jq = this.createQuery(invokeContext);
		return jq.getSingleResult();
	}


	@Override
	public <T> Page<T> findPage(Page<T> page, NamedQueryInvokeContext invokeContext) {
		if(page.isAutoCount()){
			QueryWrapper jq = this.createCountQuery(invokeContext);
			Long total = jq.getSingleResult();
			total = (total==null?0:total);
			page.setTotalCount(total);
			if(total>0){
				jq = this.createQuery(invokeContext);
				/*jq.setFirstResult(page.getFirst()-1);
				jq.setMaxResults(page.getPageSize());*/
				jq.setPageParameter(page);
				List<T> datalist = jq.getResultList();
				page.setResult(datalist);
			}
		}else{
			QueryWrapper jq = this.createQuery(invokeContext);
			jq.setPageParameter(page);
			List<T> datalist = jq.getResultList();
			page.setResult(datalist);
//			page.setTotalCount(datalist.size());
		}
		return page;
	}
	
	
//	@Override
	public <T> Page<T> findPage(Page<T> page, NamedQueryInvokeContext invokeContext, RowMapper<T> rowMapper) {
		if(page.isAutoCount()){
			QueryWrapper jq = this.createCountQuery(invokeContext);
			Long total = jq.getSingleResult();
			page.setTotalCount(total);
			if(total!=null && total>0){
				jq = this.createQuery(invokeContext);
				/*jq.setFirstResult(page.getFirst()-1);
				jq.setMaxResults(page.getPageSize());*/
				jq.setPageParameter(page);
				jq.setRowMapper(rowMapper);
				List<T> datalist = jq.getResultList();
				page.setResult(datalist);
			}
		}else{
			QueryWrapper jq = this.createQuery(invokeContext);
			jq.setPageParameter(page);
			jq.setRowMapper(rowMapper);
			List<T> datalist = jq.getResultList();
			page.setResult(datalist);
		}
		return page;
	}

}
