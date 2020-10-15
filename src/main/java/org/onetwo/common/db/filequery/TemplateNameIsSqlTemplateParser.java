package org.onetwo.common.db.filequery;

import org.onetwo.common.spring.ftl.DynamicStringFreemarkerTemplateConfigurer;
import org.onetwo.common.spring.ftl.StringTemplateProvider;
import org.onetwo.dbm.utils.DbmUtils;

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

		@Override
		public String getTemplateContent(String name) {
			return name;
		}
	}

}
