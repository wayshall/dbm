package org.onetwo.common.db.spi;

import java.util.Collection;

import org.onetwo.common.propconf.ResourceAdapter;

public interface NamedSqlFileManager {

	public static final String GLOBAL_NS_KEY = "global";

	public NamedQueryInfo getNamedQueryInfo(String name);
	public boolean contains(String fullname);
//	public void build();
	public NamedQueryFile buildSqlFile(ResourceAdapter<?> sqlFile);
	
	public NamedQueryFile getNamespaceProperties(String namespace);
	public boolean containsNamespace(String namespace);
	public Collection<NamedQueryFile> getAllNamespaceProperties();
	
	/***
	 * 默认你实现为：StringTemplateLoaderFileSqlParser
	 * @author weishao zeng
	 * @return
	 */
	public SqlTemplateParser getSqlStatmentParser();
	
}
