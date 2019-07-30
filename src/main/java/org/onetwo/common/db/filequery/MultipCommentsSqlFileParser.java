package org.onetwo.common.db.filequery;

import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.db.filequery.SimpleSqlFileLineLexer.LineToken;
import org.onetwo.common.db.spi.NamedQueryFile;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.NamedQueryInfoParser;
import org.onetwo.common.db.spi.QueryConfigData;
import org.onetwo.common.db.spi.QueryContextVariable.QueryGlobalVariable;
import org.onetwo.common.db.spi.SqlDirectiveExtractor;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.propconf.JFishProperties;
import org.onetwo.common.propconf.ResourceAdapter;
import org.onetwo.common.spring.Springs;
import org.onetwo.dbm.exception.FileNamedQueryException;
import org.slf4j.Logger;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.collect.ImmutableList;

/****
 * file example:
 	@name: findById
 	@propertyName1: value1
 	@propertyName2: value2
 	
 		sql.....
 		
 * 
 * @author wayshall
 *
 */
public class MultipCommentsSqlFileParser implements NamedQueryInfoParser {
	
	/***
	 * 这个实现有问题，少了空格
	 * @author wayshall
	 *
	 */
	@Deprecated
	public static class SimpleDirectiveExtractor implements SqlDirectiveExtractor {
		public static final String DIRECTIVE_PREFIX = SimpleSqlFileLineLexer.COMMENT + ">";//-->
		public static final String DIRECTIVE_START = DIRECTIVE_PREFIX + "[";
		public static final String DIRECTIVE_END = "]";

		@Override
		public boolean isDirective(String value) {
			return value.startsWith(DIRECTIVE_START) && value.endsWith(DIRECTIVE_END);
		}

		@Override
		public String extractDirective(String value) {
//			String directive = StringUtils.substringBetween(value, DIRECTIVE_START, DIRECTIVE_END);// value.substring(SimpleSqlFileLineLexer.COMMENT.length());
			String directive = StringUtils.substringAfter(value, DIRECTIVE_PREFIX);// value.substring(SimpleSqlFileLineLexer.COMMENT.length());
			return directive;
		}
		
	}
	
	/****
	 * 重新实现注释型指令，使用"-- >"
	 * @author wayshall
	 *
	 */
	public static class CustomDirectiveExtractor implements SqlDirectiveExtractor {
		private String prefix;
		private String start;
		private String end;

		public CustomDirectiveExtractor(String prefix, String start, String end) {
			super();
			this.prefix = prefix;
			this.start = prefix+start;
			this.end = end;
		}

		@Override
		public boolean isDirective(String value) {
			return value.startsWith(start) && value.endsWith(end);
		}

		@Override
		public String extractDirective(String value) {
			String directive = StringUtils.substringAfter(value, prefix);
			return directive;
		}
	}
	
	public static final String GLOBAL_NS_KEY = "global";
	public static final String AT = "@";
//	public static final String EQUALS_MARK = "=";
	public static final String COLON = ":";
	
	
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	protected boolean debug = false;
//	protected SqlDirectiveExtractor sqlDirectiveExtractor = new SimpleDirectiveExtractor();
	protected List<SqlDirectiveExtractor> sqlDirectiveExtractors = ImmutableList.of(new SimpleDirectiveExtractor(), 
																					new CustomDirectiveExtractor("-- >", "[", "]"));
	
	@Override
	public void parseToNamedQueryFile(NamedQueryFile namespaceInfo, ResourceAdapter<?> sqlFile) {
		if(!sqlFile.exists()){
			logger.info("sql file is not exists, ignore parse. namespace: " + namespaceInfo.getNamespace());
			return ;
		}
		if(!sqlFile.getName().endsWith(POSTFIX)){
			logger.info("file["+sqlFile.getName()+" is not a ["+POSTFIX+"] file, ignore it.");
			return ;
		}
		SimpleSqlFileLineLexer lineLexer = new SimpleSqlFileLineLexer(createLineReader(sqlFile));
		lineLexer.nextLineToken();
		while(lineLexer.getLineToken()!=LineToken.EOF){
			LineToken token = lineLexer.getLineToken();
			if(debug)
				logger.info("current token {}  ", token);
			
			switch (token) {
				case MULTIP_COMMENT:
					this.parseQueryStatement(lineLexer, namespaceInfo, sqlFile);
					break;
					
				default:
					if(debug)
						logger.info("ignore token {} : {} ", token, lineLexer.getLineBuf());
					
					lineLexer.nextLineToken();
					break;
			}
		}
		
	}
	
	protected QueryConfigData createQueryConfigData() {
		QueryConfigData config = new QueryConfigData();
		
		if (Springs.getInstance().isActive()) {
			List<QueryGlobalVariable> globals = Springs.getInstance().getBeans(QueryGlobalVariable.class);
			config.setVariables(globals.toArray(new QueryGlobalVariable[0]));
		}
		
		return config;
	}
	
	protected void parseQueryStatement(SimpleSqlFileLineLexer lineLexer,
										NamedQueryFile namespaceInfo, ResourceAdapter<?> f){
		List<String> comments = lineLexer.getLineBuf();
		JFishProperties config = parseComments(comments);
		//name
		//没有发现 @name 属性的注释，抛错……
//		String name = config.getAndThrowIfEmpty(DbmNamedFileQueryInfo.NAME_KEY);
		String name = config.getProperty(NamedQueryInfo.NAME_KEY);
		//没有发现 @name 属性的注释，忽略
		if(StringUtils.isBlank(name)){
			scanSqlContent(lineLexer);
			return ;
		}
		config.remove(NamedQueryInfo.NAME_KEY);
		
		NamedQueryInfo bean = namespaceInfo.getNamedProperty(name);
		String sqlPropertyName = "value";
		if(bean==null){
			bean = new NamedQueryInfo();
			bean.setDbmNamedQueryFile(namespaceInfo);
			bean.setSrcfile(f);
			bean.setQueryConfig(createQueryConfigData());

			bean.setName(name);
			bean.setConfig(config);

			namespaceInfo.put(bean.getName(), bean, true);

			//别名
			if(config.containsKey(NamedQueryInfo.ALIAS_KEY)){
				bean.setAliasList(config.getStringList(NamedQueryInfo.ALIAS_KEY, ","));
				config.remove(NamedQueryInfo.ALIAS_KEY);
			}
			
			/*if(config.containsKey(JFishNamedFileQueryInfo.MATCHER_KEY)){
				//matcher
				String matchers = config.getProperty(JFishNamedFileQueryInfo.MATCHER_KEY);
				bean.setMatchers(Arrays.asList(StringUtils.split(matchers, JFishNamedFileQueryInfo.MATCHER_SPIT_KEY)));
				config.remove(JFishNamedFileQueryInfo.MATCHER_KEY);
				
			}*/
			if(config.containsKey(NamedQueryInfo.PROPERTY_KEY)
					|| config.containsKey(NamedQueryInfo.FRAGMENT_KEY)){
				throw new FileNamedQueryException("no parent query["+bean.getName()+"] found, the @property or @fragment tag must defined in a subquery, near at "
						+ "line : " + lineLexer.getLineReader().getLineNumber());
			}
		}else{
			if(config.containsKey(NamedQueryInfo.PROPERTY_KEY)){
				//property:propertyName
				sqlPropertyName = config.getProperty(NamedQueryInfo.PROPERTY_KEY);
				config.remove(NamedQueryInfo.PROPERTY_KEY);
				
			}else if(config.containsKey(NamedQueryInfo.FRAGMENT_KEY)){
				//fragment[fragmentValue]=sql
				sqlPropertyName = NamedQueryInfo.FRAGMENT_KEY + "[" + 
															config.getProperty(NamedQueryInfo.FRAGMENT_KEY)
																	+ "]";
				config.remove(NamedQueryInfo.FRAGMENT_KEY);
				
			}else{
				throw new FileNamedQueryException("named query["+name+"]'s  must be specify a @property or @fragment."
						+ "near at line : " + lineLexer.getLineReader().getLineNumber());
			}
//			sqlPropertyName = config.getProperty(PROPERTY_KEY);
		}
		
		
		if(debug)
			logger.info("config: {}", config);
		
		Enumeration<?> keys = config.propertyNames();
		BeanWrapper beanBw = PropertyAccessorFactory.forBeanPropertyAccess(bean);
		while(keys.hasMoreElements()){
			String prop = keys.nextElement().toString();
			this.setNamedInfoProperty(beanBw, prop, config.getProperty(prop));
		}
		
		String sqlContent = scanSqlContent(lineLexer);
//		bean.setValue(buf.toString());
		beanBw.setPropertyValue(sqlPropertyName, sqlContent);
		
		if(debug)
			logger.info("value: {}", bean.getValue());
	}
	
	private String scanSqlContent(SimpleSqlFileLineLexer lineLexer){
		StringBuilder buf = new StringBuilder();
		while(lineLexer.nextLineToken()){
			if(lineLexer.getLineToken()==LineToken.EOF){
				break;
			}else if(lineLexer.getLineToken()==LineToken.MULTIP_COMMENT){
				break;
			}else if(lineLexer.getLineToken()==LineToken.ONE_LINE_COMMENT){
				String value = StringUtils.join(lineLexer.getLineBuf(), " ");
				/*if(sqlDirectiveExtractor.isDirective(value)){//-->[#if ... ]
					buf.append(sqlDirectiveExtractor.extractDirective(value)).append(" ");
				}*/
				this.parseSqlDirectiveExtractors(buf, value);
				continue;
			}else if(lineLexer.getLineToken()==LineToken.CONTENT){
				String value = StringUtils.join(lineLexer.getLineBuf(), " ");
//				buf.append(value).append(" ");
				buf.append(value).append('\n');
			}else{
				throw new FileNamedQueryException("error syntax: " + lineLexer.getLineToken());
			}
		}
		return buf.toString();
	}
	
	protected void parseSqlDirectiveExtractors(StringBuilder buf, String value){
		this.sqlDirectiveExtractors.forEach(sqlDirectiveExtractor->{
			if(sqlDirectiveExtractor.isDirective(value)){//-->[#if ... ]
				buf.append(sqlDirectiveExtractor.extractDirective(value)).append(" ");
			}
		});
	}
	
	
	protected JFishProperties parseComments(List<String> comments){
		JFishProperties config = new JFishProperties();
		for(final String comment : comments){
//			logger.info("comment: {}", comment);
			if(comment.startsWith(AT)){
				String line = comment.substring(AT.length());
				String[] strs = StringUtils.split(line, COLON);
				if(strs.length==2){
					config.setProperty(strs[0].trim(), strs[1].trim());
				}else{
					throw new FileNamedQueryException("error syntax for config: " + comment);
				}
			}
		}
		return config;
	}
	
	protected void setNamedInfoProperty(BeanWrapper beanBw, String prop, Object val){
		if(prop.indexOf(NamedQueryInfo.DOT_KEY)!=-1){
			prop = org.onetwo.common.utils.StringUtils.toCamel(prop, NamedQueryInfo.DOT_KEY, false);
		}
		beanBw.setPropertyValue(prop, val);
		/*try {
			ReflectUtils.setExpr(bean, prop, val);
		} catch (Exception e) {
			logger.error("set value error : "+prop);
			LangUtils.throwBaseException(e);
		}*/
	}
	
/*
	protected List<String> readResourceAsList(ResourceAdapter<?> f){
		if(f.isSupportedToFile())
			return FileUtils.readAsList(f.getFile());
		else
			throw new UnsupportedOperationException();
	}*/

	protected SimpleSqlFileLineReader createLineReader(ResourceAdapter<?> f){
//		return new SimpleSqlFileLineReader(readResourceAsList(f));
		return new SimpleSqlFileLineReader(f.readAsList());
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}
