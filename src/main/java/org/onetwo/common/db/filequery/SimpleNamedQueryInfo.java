package org.onetwo.common.db.filequery;

import org.onetwo.common.db.spi.FileSqlParserType;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.QueryConfigData;

import lombok.Data;

/**
 * @author weishao zeng
 * <br/>
 */
@Data
public class SimpleNamedQueryInfo implements NamedQueryInfo {
	
	private String name;
	private QueryConfigData queryConfig = new QueryConfigData();
	
	@Override
	public String getFullName() {
		return name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getCountName() {
		return name;
	}

	@Override
	public FileSqlParserType getParserType() {
		return FileSqlParserType.TEMPLATE;
	}

	@Override
	public boolean isAutoGeneratedCountSql() {
		return true;
	}

	@Override
	public QueryConfigData getQueryConfig() {
		return queryConfig;
	}

	@Override
	public String getNamespace() {
		return "";
	}

	@Override
	public String getFragmentTemplateName(String attr) {
		throw new UnsupportedOperationException();
	}

}
