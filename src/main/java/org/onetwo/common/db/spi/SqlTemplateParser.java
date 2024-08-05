package org.onetwo.common.db.spi;

import org.onetwo.common.db.filequery.ParserContext;
import org.onetwo.common.spring.ftl.TemplateParser;

/**
 * sql模板适配接口
 * @author weishao zeng
 * @see StringTemplateLoaderFileSqlParser(FreemarkerSqlTemplateParser), TemplateNameIsSqlTemplateParser
 * <br/>
 */
public interface SqlTemplateParser extends TemplateParser {
	
	default String parse(String name, Object context) {
		return parseSql(name, (ParserContext)context);
	}
	
	String parseSql(String name, ParserContext context);

}
