package org.onetwo.common.db.filequery;

import org.onetwo.common.db.spi.NamedQueryFileListener;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.spring.ftl.TemplateParser;
import org.onetwo.dbm.exception.FileNamedQueryException;

/****
 * sql文件查询对象管理
 * 管理每个sql文件的查询对象
 * @author weishao
 *
 * @param <T>
 */
public class DbmNamedSqlFileManager extends BaseNamedSqlFileManager {

	public static final StringTemplateLoaderFileSqlParser FILE_SQL_PARSER = new StringTemplateLoaderFileSqlParser();

	public static DbmNamedSqlFileManager createNamedSqlFileManager(boolean watchSqlFile) {
		StringTemplateLoaderFileSqlParser listener = FILE_SQL_PARSER;
		listener.initialize();
		DbmNamedSqlFileManager sqlfileMgr = new DbmNamedSqlFileManager(watchSqlFile, listener, listener);
//		sqlfileMgr.setDataSource(dataSource);
		return sqlfileMgr;
	}
	
	
	public static final String ATTRS_KEY = NamedQueryInfo.FRAGMENT_DOT_KEY;
	private TemplateParser sqlStatmentParser;
	

	public DbmNamedSqlFileManager(boolean watchSqlFile, TemplateParser sqlStatmentParser, NamedQueryFileListener listener) {
		super(watchSqlFile, listener);
//		this.databaseType = conf.getDatabaseType();
//		this.setSqlFileParser(new MultipCommentsSqlFileParser());
		/*this.setQueryInfoParsers(Arrays.asList(
									new MultipCommentsSqlFileParser(),
									new AnnotationBasedQueryInfoParser()
								));*/
		this.sqlStatmentParser = sqlStatmentParser;
	}

	protected void putIntoCaches(String key, NamedQueryInfo nsp){
		super.putIntoCaches(key, nsp);
		nsp.getAliasList().forEach(aliasName->{
			/*try {
				JFishNamedFileQueryInfo cloneBean = nsp.clone();
				cloneBean.setName(aliasName);
				super.putIntoCaches(aliasName, cloneBean);
			} catch (Exception e) {
				throw new DbmException("clone error: " + key);
			}*/
			super.putIntoCaches(nsp.getFullName(aliasName), nsp);
		});
	}
	
	public TemplateParser getSqlStatmentParser() {
		return sqlStatmentParser;
	}

	protected void extBuildNamedInfoBean(NamedQueryInfo propBean){
//		propBean.setDataBaseType(getDatabaseType());
	}

	public NamedQueryInfo getNamedQueryInfo(String name) {
		NamedQueryInfo info = super.getNamedQueryInfo(name);
		if(info==null)
			throw new FileNamedQueryException("namedQuery not found : " + name);
		return info;
	}
	
	/*public static class DialetNamedSqlConf extends JFishPropertyConf<DbmNamedQueryInfo> {
//		private DataBase databaseType;
		
		public DialetNamedSqlConf(boolean watchSqlFile){
			setWatchSqlFile(watchSqlFile);
			setPropertyBeanClass(DbmNamedQueryInfo.class);
		}

	}*/
}
