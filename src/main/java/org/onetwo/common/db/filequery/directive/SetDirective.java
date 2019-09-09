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
public class SetDirective extends StrDirective {


	protected String getInsertPrefix(Map params) {
		return "set";
	}
	
	protected List<String> getTrimPrefixs(Map params) {
		return Arrays.asList(",");
	}
	
	protected List<String> getTrimSuffixs(Map params) {
		return Arrays.asList(",");
	}

	@Override
	public String getName() {
		return "set";
	}
}

