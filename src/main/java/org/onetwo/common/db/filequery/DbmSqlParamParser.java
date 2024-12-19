package org.onetwo.common.db.filequery;

import org.onetwo.common.db.spi.SqlTemplateParser;
import org.onetwo.common.spring.ftl.StringTemplateConfigurer;
import org.onetwo.dbm.utils.DbmUtils;

/***
 * 用于实现@Sql注解功能
 * sql通过参数传入
 * 取代旧的实现：TemplateNameIsSqlTemplateParser
 * @author way
 * @see TemplateNameIsSqlTemplateParser
 */
public class DbmSqlParamParser implements SqlTemplateParser {
	
	public static final DbmSqlParamParser INSTANCE;
	static {
		INSTANCE = new DbmSqlParamParser();
	}

	private StringTemplateConfigurer templateParser;
	public DbmSqlParamParser() {
		this.templateParser = new StringTemplateConfigurer();
		DbmUtils.initSqlTemplateDirective(templateParser);
		this.templateParser.initialize();
	}

	@Override
	public String parseSql(String sql, Object context) {
		String parsedSql = templateParser.parse(sql, context);
		return parsedSql;
	}

	public StringTemplateConfigurer getTemplateParser() {
		return templateParser;
	}

}
