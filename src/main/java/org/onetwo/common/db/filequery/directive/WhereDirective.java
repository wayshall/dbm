package org.onetwo.common.db.filequery.directive;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.onetwo.common.spring.ftl.StrDirective;

/**
 * @author weishao zeng
 * <br/>
 */
@SuppressWarnings("rawtypes")
public class WhereDirective extends StrDirective {


	protected String getInsertPrefix(Map params) {
		return "where";
	}
	
	protected List<String> getTrimPrefixs(Map params) {
		return Arrays.asList("and", "or");
	}
	
	protected List<String> getTrimSuffixs(Map params) {
		return Arrays.asList("and", "or");
	}

	@Override
	public String getName() {
		return "where";
	}
}

