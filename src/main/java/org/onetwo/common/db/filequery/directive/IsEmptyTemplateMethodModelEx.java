package org.onetwo.common.db.filequery.directive;

import java.util.List;

import org.onetwo.common.spring.ftl.FtlUtils;

import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;

public enum IsEmptyTemplateMethodModelEx implements SqlTemplateMethodModelEx {
	
	IS_EMPTY(true, "isEmpty", "is_empty"),
	IS_NOT_EMPTY(false, "isNotEmpty", "is_not_empty");
	
	final private String[] methodNames;
	final private boolean returnTrueIfEmpty;
	
	private IsEmptyTemplateMethodModelEx(boolean returnEmpty, String... methodName) {
		this.returnTrueIfEmpty = returnEmpty;
		this.methodNames = methodName;
	}
	
	public Object exec(List arguments) throws TemplateModelException {
		Object collection = arguments.get(0);
		boolean isEmpty = true;
		if (collection!=null) {
			TemplateCollectionModel collectionModel = FtlUtils.getCollectionMethodArg(arguments, 0, methodNames[0]);
			TemplateModelIterator it = collectionModel.iterator();
			isEmpty = !it.hasNext();
		}

		
		if (returnTrueIfEmpty) {
			// return true if empty
			return isEmpty;
		} else {
			// return true if not empty
			return !isEmpty;
		}
	}

	public String[] getMethodNames() {
		return methodNames;
	}
	
}
