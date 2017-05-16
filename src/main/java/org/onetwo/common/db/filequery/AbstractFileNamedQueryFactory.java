package org.onetwo.common.db.filequery;

import java.util.List;
import java.util.Optional;

import org.onetwo.common.db.ParsedSqlContext;
import org.onetwo.common.db.dquery.NamedQueryInvokeContext;
import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.common.db.spi.FileNamedQueryFactory;
import org.onetwo.common.db.spi.FileNamedSqlGenerator;
import org.onetwo.common.db.spi.NamedSqlFileManager;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.utils.LangUtils;

abstract public class AbstractFileNamedQueryFactory implements FileNamedQueryFactory {

//	private FileNamedQueryFactoryListener fileNamedQueryFactoryListener;
//	private QueryProvideManager queryProvideManager;
	protected DbmNamedSqlFileManager sqlFileManager;
//	private DataBase dataBase;

	public AbstractFileNamedQueryFactory(DbmNamedSqlFileManager sqlFileManager) {
		this.sqlFileManager = sqlFileManager;
//		this.dataBase = dataBase;
	}


	@Override
	public NamedSqlFileManager getNamedSqlFileManager() {
		return sqlFileManager;
	}


	@Override
	public <E> E findOne(NamedQueryInvokeContext invokeContex) {
		QueryWrapper jq = this.createQuery(invokeContex);
		E entity = null;
		List<E> list = jq.getResultList();
		if(LangUtils.hasElement(list))
			entity = list.get(0);
		return entity;
	}
	
	/*@Override
	public JFishNamedFileQueryInfo getNamedQueryInfo(String queryName){
		JFishNamedFileQueryInfo queryInfo = sqlFileManager.getNamedQueryInfo(queryName);
		return queryInfo;
	}*/

//	@Override
	
//	@Override
	public FileNamedSqlGenerator createFileNamedSqlGenerator(NamedQueryInvokeContext invokeContext) {
		Optional<SqlFunctionDialet> sqlFunctionDialet = invokeContext.getQueryProvideManager().getSqlFunctionDialet();
//		NamedQueryInfo nameInfo = getNamedQueryInfo(invokeContext);
		ParserContext parserContext = ParserContext.create(invokeContext.getNamedQueryInfo());
		FileNamedSqlGenerator g = new DefaultFileNamedSqlGenerator(parserContext, false, sqlFileManager.getSqlStatmentParser(), invokeContext.getParsedParams(), sqlFunctionDialet);
		return g;
	}

	/*public QueryProvideManager getQueryProvideManager() {
		return queryProvideManager;
	}


	public void setQueryProvideManager(QueryProvideManager queryProvideManager) {
		this.queryProvideManager = queryProvideManager;
	}*/

	@Override
	public ParsedSqlContext parseNamedQuery(NamedQueryInvokeContext invokeContext) {
		FileNamedSqlGenerator sqlGen = createFileNamedSqlGenerator(invokeContext);
		ParsedSqlContext parsedSqlContext = sqlGen.generatSql();
		return parsedSqlContext;
	}


	public DbmNamedSqlFileManager getSqlFileManager() {
		return sqlFileManager;
	}

	
}
