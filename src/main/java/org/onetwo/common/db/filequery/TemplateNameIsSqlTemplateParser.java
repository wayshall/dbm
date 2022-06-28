package org.onetwo.common.db.filequery;

import org.onetwo.common.spring.ftl.DynamicStringFreemarkerTemplateConfigurer;
import org.onetwo.common.spring.ftl.StringTemplateProvider;
import org.onetwo.dbm.utils.DbmUtils;

/***
 * 用于实现@Sql注解功能
 * sql通过参数传入
 * @author way
 *
 */
public class TemplateNameIsSqlTemplateParser extends DynamicStringFreemarkerTemplateConfigurer implements FreemarkerSqlTemplateParser {
	
	public static final TemplateNameIsSqlTemplateParser INSTANCE;
	static {
		INSTANCE = new TemplateNameIsSqlTemplateParser();
		INSTANCE.initialize();
	}

	public TemplateNameIsSqlTemplateParser() {
		super(new NameIsContentTemplateProvider());
		DbmUtils.initSqlTemplateDirective(this);
	}
	
	public static class NameIsContentTemplateProvider implements StringTemplateProvider {
		// 直接把名称返回，作为模版。即把使用@sql注解标记的参数作为模版
		@Override
		public String getTemplateContent(String name) {
			return name;
		}
	}

}
