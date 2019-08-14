package org.onetwo.common.db.filequery;

import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.spring.ftl.FtlUtils;
import org.onetwo.common.spring.ftl.TemplateParser;
import org.onetwo.dbm.exception.FileNamedQueryException;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FragmentTemplateParser implements TemplateHashModel {
	public static final String DOT = ".";
	
	private final TemplateParser parser;
	private final ParserContext parserContext;
	private final NamedQueryInfo query;
	
	public FragmentTemplateParser(TemplateParser parser, ParserContext parserContext, NamedQueryInfo query) {
		super();
		this.parser = parser;
		this.parserContext = parserContext;
		this.query = query;
	}
	
	/***
	 * 在sql里引用访问的变量名称 fragment
	 * @author weishao zeng
	 * @return
	 */
	public String getVarName() {
		return NamedQueryInfo.FRAGMENT_KEY;
	}
	
	/***
	 * 是否带命名空间，即是否包含了'.'符号，如果有包含点号，返回true，否则返回false
	 * @author weishao zeng
	 * @param key
	 * @return
	 */
	private boolean isNamespaceScope(String key){
//		return key.startsWith(AbstractPropertiesManager.NAME_PREFIX);
		return key.indexOf(NamedQueryInfo.DOT_KEY)!=-1;
	}
	
	private String getQueryName(String key){
		int start = 0;//AbstractPropertiesManager.NAME_PREFIX.length();
		int end = key.indexOf(DOT);
		return key.substring(start, end);
	}

	/***
	 * findUserPage.fragment.subWhere
	 * 
	 * @author weishao zeng
	 * @param key
	 */
	private void checkKeyIfNamespaceScope(String key){
		String qname = getQueryName(key);
		String subkey = key.substring(qname.length()+DOT.length());
		// 跨命名空间访问片段时，必须以fragment开头，如：fragment.subWhere
		if(!subkey.startsWith(NamedQueryInfo.FRAGMENT_KEY)){
			throw new FileNamedQueryException("only can access "+NamedQueryInfo.FRAGMENT_KEY+" of query, error key: " + key);
		}
	}
	/****
	 * {@link NamedQueryInfo#getFragment()}
	 */
	@Override
	public TemplateModel get(String key) throws TemplateModelException {
		String value = null;
		// 是否带命名空间，即是否包含了'.'符号
		if(isNamespaceScope(key)){
//			String qname = getQueryName(key);
//			JFishNamedFileQueryInfo queryInfo = (JFishNamedFileQueryInfo)query.getNamespaceInfo().getNamedProperty(qname);
//			String subkey = key.substring(qname.length()+DOT.length()+1);
//			checkKeyIfNamespaceScope(subkey);
//			value = (String)SpringUtils.newBeanWrapper(queryInfo).getPropertyValue(subkey);
			checkKeyIfNamespaceScope(key);
			value = query.getDbmNamedQueryFile().isGlobal()?key:query.getNamespace()+"."+key;
		}else{
//			checkKey(key);
//			value = query.getAttrs().get(key);
			value = query.getFragmentTemplateName(key);
		}
//		value = this.parser.parse(parser.asFtlContent(value), parserContext);// 不再解释，value含有星号的话，freemarker会认为是路径模糊匹配导致出错
		value = this.parser.parse(value, parserContext);
		return FtlUtils.wrapAsModel(value);
	}

	@Override
	public boolean isEmpty() throws TemplateModelException {
		return false;
	}
	
	
	
}
