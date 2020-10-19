package org.onetwo.common.db.filequery;

import org.onetwo.common.spring.ftl.DynamicStringFreemarkerTemplateConfigurer;
import org.onetwo.common.spring.ftl.StringTemplateProvider;
import org.onetwo.dbm.utils.DbmUtils;

/***
 * 提供一个简单的实现，用于从其它地方加载sql时，快速实现
 * @author way
 *
 */
public class SimpleDynamicSqlTemplateParser extends DynamicStringFreemarkerTemplateConfigurer implements FreemarkerSqlTemplateParser {

	public SimpleDynamicSqlTemplateParser(StringTemplateProvider templateProvider) {
		super(templateProvider);
		DbmUtils.initSqlTemplateDirective(this);
	}

}
