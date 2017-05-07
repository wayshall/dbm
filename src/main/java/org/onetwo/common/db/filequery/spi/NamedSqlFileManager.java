package org.onetwo.common.db.filequery.spi;

import java.util.Collection;

import org.onetwo.common.db.dquery.DbmSqlFileResource;
import org.onetwo.common.db.filequery.DbmNamedQueryFile;
import org.onetwo.common.db.filequery.DbmNamedQueryInfo;

public interface NamedSqlFileManager {

	public static final String GLOBAL_NS_KEY = "global";

	public DbmNamedQueryInfo getNamedQueryInfo(String name);
	public boolean contains(String fullname);
//	public void build();
	public DbmNamedQueryFile buildSqlFile(DbmSqlFileResource<?> sqlFile);
	
	public DbmNamedQueryFile getNamespaceProperties(String namespace);
	public boolean containsNamespace(String namespace);
	public Collection<DbmNamedQueryFile> getAllNamespaceProperties();
}
