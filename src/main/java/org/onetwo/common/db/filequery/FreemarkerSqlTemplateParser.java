package org.onetwo.common.db.filequery;

import java.io.StringWriter;

import org.onetwo.common.db.spi.SqlTemplateParser;
import org.onetwo.dbm.exception.DbmException;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author weishao zeng
 * <br/>
 */

public interface FreemarkerSqlTemplateParser extends SqlTemplateParser {
	
	Template getTemplate(String name);
	
	default public String parseSql(String name, Object context) {
//		return parser.parse(name, context);

		Template template = getTemplate(name);
		StringWriter sw = new StringWriter();
		try {
			template.process(context, sw);
		} catch (TemplateException e) {
			e.printStackTrace();
			Throwable cause = e.getCause();
			if(cause!=null){
				throw new DbmException("parse sql tempalte error : " + cause.getMessage(), cause);
			}else{
				throw new DbmException("parse sql tempalte error : " + e.getMessage(), e);
			}
		} catch (Exception e) {
			throw new DbmException("parse sql tempalte error : " + e.getMessage(), e);
		}
		return sw.toString();
	}
}
