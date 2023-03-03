package org.onetwo.common.db.spi;

import org.onetwo.common.db.dquery.DbmSqlFileResource;

/****
 * 解释sql文件，构建为对象
 * @author way
 *
 * @param <T>
 */
public interface NamedQueryInfoParser {

	String POSTFIX = SqlFileScanner.SQL_POSTFIX;
	
	void parseToNamedQueryFile(NamedQueryFile namedQueryFile, DbmSqlFileResource<?> file);
	
}
