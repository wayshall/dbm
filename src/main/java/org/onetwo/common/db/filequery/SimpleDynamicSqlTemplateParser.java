package org.onetwo.common.db.filequery;

import org.onetwo.common.spring.ftl.DynamicStringFreemarkerTemplateConfigurer;
import org.onetwo.common.spring.ftl.StringTemplateProvider;
import org.onetwo.dbm.utils.DbmUtils;

public class SimpleDynamicSqlTemplateParser extends DynamicStringFreemarkerTemplateConfigurer implements FreemarkerSqlTemplateParser {

	public SimpleDynamicSqlTemplateParser(StringTemplateProvider templateProvider) {
		super(templateProvider);
		DbmUtils.initSqlTemplateDirective(this);
	}

}
