package org.onetwo.common.db.dquery.condition.directive;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.onetwo.common.db.dquery.condition.DynamicFieldCondition;
import org.onetwo.common.spring.ftl.FtlUtils;
import org.onetwo.common.spring.ftl.NamedDirective;
import org.onetwo.common.utils.LangUtils;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/*****
 * 
 * [@dynamicCondition fields=dynamicFieldConditionList]
   [/@dynamicCondition]
    	
 * @author way
 *
 */
@SuppressWarnings("rawtypes")
public class DynamicConditionDirective implements NamedDirective {
	
	public static final String DIRECTIVE_NAME = "dynamicCondition";

	public static final String PARAMS_FIELDS = "fields";

	@SuppressWarnings("unchecked")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
		BeanModel fieldsBeanModel = (BeanModel)FtlUtils.getRequiredParameter(params, PARAMS_FIELDS);
		List<DynamicFieldCondition> fields = (List<DynamicFieldCondition>) fieldsBeanModel.getWrappedObject();
		if (LangUtils.isEmpty(fields)) {
			return ;
		}

		StringBuilder buffer = new StringBuilder(100);
		int index = 0;
		for (DynamicFieldCondition field : fields) {
			if (index!=0) {
				buffer.append("and ");
			}
			buffer.append(field.getFieldName()).append(" ")
					.append(field.getOperator().getActualOperator()).append(" ")
					.append(":").append(field.getParameterName()).append(" ");
			index++;
		}
		env.getOut().append(buffer);
	}

	@Override
	public String getName() {
		return DIRECTIVE_NAME;
	}

}
