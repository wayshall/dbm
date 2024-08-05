package org.onetwo.common.db.filequery.directive;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.spring.ftl.FtlUtils;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

public enum IsBlankTemplateMethodModelEx implements SqlTemplateMethodModelEx {
	
	BLANK(true, "isBlank", "is_blank"),
	NOT_BLANK(false, "isNotBlank", "is_not_blank");
	
	final private String[] methodNames;
	final private boolean returnTrueIfBlank;
	
	private IsBlankTemplateMethodModelEx(boolean returnBlank, String... methodName) {
		this.returnTrueIfBlank = returnBlank;
		this.methodNames = methodName;
	}
	
	public Object exec(List arguments) throws TemplateModelException {
		Object str = arguments.get(0);
		boolean isblank = true;
		if (str!=null) {
			StringModel stringModel = FtlUtils.getStringMethodArg(arguments, 0, methodNames[0]);
			String val = stringModel.getAsString();
			isblank = StringUtils.isBlank(val);
		}
		
		if (returnTrueIfBlank) {
			// return ture if string is blank
			return isblank;
		} else {
			// return ture if string is not blank
			return !isblank;
		}
	}

	public String[] getMethodNames() {
		return methodNames;
	}
	
}
