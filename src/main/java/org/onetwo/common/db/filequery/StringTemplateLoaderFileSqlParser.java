package org.onetwo.common.db.filequery;

import java.util.Map.Entry;

import org.onetwo.common.db.spi.NamedQueryFile;
import org.onetwo.common.db.spi.NamedQueryFileListener;
import org.onetwo.common.propconf.ResourceAdapter;
import org.onetwo.common.spring.ftl.AbstractFreemarkerTemplateConfigurer;
import org.onetwo.dbm.utils.DbmUtils;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

/****
 * 基于StringTemplateLoader的sql模板解释器
 * @author way
 *
 */
public class StringTemplateLoaderFileSqlParser extends AbstractFreemarkerTemplateConfigurer implements FreemarkerSqlTemplateParser, NamedQueryFileListener {

//	public static final String QUERY_POSTFIX = ".query";//for ftl
	
//	private JFishNamedSqlFileManager<T> sqlManager;
	private StringTemplateLoader templateLoader;
//	final private TemplateParser parser;
	
	
	public StringTemplateLoaderFileSqlParser() {
		super();
//		this.sqlManager = sqlm;
		DbmUtils.initSqlTemplateDirective(this);
		this.templateLoader = new StringTemplateLoader();
		
//		DefaultTemplateParser p = new DefaultTemplateParser(this);
		
//		parser = p;
	}
	

//	@Override
//	public void afterBuild(Map<String, NamedQueryFile> namespaceInfos, ResourceAdapter<?>... sqlfileArray) {
//		this.initialize();
//		for(NamedQueryFile namespace : namespaceInfos.values()){
//			this.putTemplateByNamespaceInfo(namespace);
//		}
//	}

	@Override
	public void afterBuild(ResourceAdapter<?> file, NamedQueryFile namepsaceInfo) {
		this.putTemplateByNamespaceInfo(namepsaceInfo);
	}


	@Override
	public void afterReload(ResourceAdapter<?> file, NamedQueryFile namepsaceInfo) {
		this.putTemplateByNamespaceInfo(namepsaceInfo);
	}



	/*@Override
	public void initParser() {
		this.initialize();
	}*/
	


	private void putTemplateByNamespaceInfo(NamedQueryFile namespace){
		for(FileBaseNamedQueryInfo info : namespace.getNamedProperties()){
			if(logger.isInfoEnabled()){
				logger.info("put query template: {}", info.getFullName());
			}
			this.templateLoader.putTemplate(info.getFullName(), info.getSql());
			if(!info.isAutoGeneratedCountSql()){
				this.templateLoader.putTemplate(info.getCountName(), info.getCountSql());
			}
			for(Entry<String, String> entry : info.getFragment().entrySet()){
				if(logger.isInfoEnabled()){
					logger.info("put query sub template: {}", info.getFragmentTemplateName(entry.getKey()));
				}
				this.templateLoader.putTemplate(info.getFragmentTemplateName(entry.getKey()), entry.getValue());
			}
		}
	}



	@Override
	protected void buildConfigration(Configuration cfg) {
		cfg.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
	}

	@Override
	protected TemplateLoader getTempateLoader() {
		return templateLoader;
	}


}
