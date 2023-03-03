package org.onetwo.common.db.filequery;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.onetwo.common.db.dquery.DbmSqlFileResource;
import org.onetwo.common.db.spi.NamedQueryFile;
import org.onetwo.common.db.spi.NamedQueryFileListener;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.NamedQueryInfoParser;
import org.onetwo.common.db.spi.NamedSqlFileManager;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.propconf.ResourceAdapter;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.watch.FileChangeListener;
import org.onetwo.common.watch.FileWatcher;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.exception.FileNamedQueryException;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


abstract public class BaseNamedSqlFileManager implements NamedSqlFileManager {

	
	/*public static final String COMMENT = "--";
	public static final String MULTIP_COMMENT_START = "/*";
	public static final String MULTIP_COMMENT_END = "* /";
	public static final String CONFIG_PREFIX = "--@@";
	public static final String NAME_PREFIX = "--@";
	public static final String EQUALS_MARK = "=";*/
//	public static final String IGNORE_NULL_KEY = "ignore.null";

	protected final Logger logger = JFishLoggerFactory.getLogger(getClass());
	
	private FileWatcher fileMonitor;
	private long period = 1;
	
	private Map<String, NamedQueryFile> namespaceProperties = Maps.newHashMap();
//	private List<Properties> sqlfiles;
//	private PropertiesWraper wrapper;
	private Map<String, FileBaseNamedQueryInfo> namedQueryCache = Maps.newConcurrentMap();
//	private DbmNamedQueryInfoParser sqlFileParser = new DefaultSqlFileParser();
	private List<NamedQueryInfoParser> queryInfoParsers = Lists.newArrayList(new DefaultSqlFileParser());
	final private NamedQueryFileListener listener;
	
	private boolean watchSqlFile;
	
	public BaseNamedSqlFileManager(boolean watchSqlFile, NamedQueryFileListener listener) {
		this.watchSqlFile = watchSqlFile;
		this.listener = listener;
	}
	
	public NamedQueryFileListener getListener() {
		return listener;
	}

	final public void setQueryInfoParsers(List<NamedQueryInfoParser> queryInfoParsers) {
		this.queryInfoParsers = queryInfoParsers;
	}

	/****
	 * 解释sql文件
	 */
	@Override
	public NamedQueryFile buildSqlFile(DbmSqlFileResource<?> sqlFile){
		Assert.notNull(sqlFile);
		NamedQueryFile info = this.parseSqlFile(sqlFile, true);
		this.buildSqlFileMonitor(sqlFile);

		if(this.listener!=null){
			this.listener.afterBuild(sqlFile, info);
		}
		/*logger.info("all named query : ");
		for(T prop : this.namedQueryCache.values()){
			logger.info(prop.toString());
		}*/
		return info;
	}
	
	/****
	 * 根据配置扫描文件
	 * @param conf
	 * @return
	 */
	/*protected ResourceAdapter<?>[] scanMatchSqlFiles(JFishPropertyConf<T> conf){
		String sqldirPath = FileUtils.getResourcePath(conf.getClassLoader(), conf.getDir());

		File[] sqlfileArray = FileUtils.listFiles(sqldirPath, conf.getPostfix());
		if(logger.isInfoEnabled())
			logger.info("find {} sql named file in dir {}", sqlfileArray.length, sqldirPath);
		
		if(StringUtils.isNotBlank(conf.getOverrideDir())){
			String overridePath = sqldirPath+"/"+conf.getOverrideDir();
			logger.info("scan database dialect dir : " + overridePath);
			File[] dbsqlfiles = FileUtils.listFiles(overridePath, conf.getPostfix());

			if(logger.isInfoEnabled())
				logger.info("find {} sql named file in dir {}", dbsqlfiles.length, overridePath);
			
			if(!LangUtils.isEmpty(dbsqlfiles)){
				sqlfileArray = (File[]) ArrayUtils.addAll(sqlfileArray, dbsqlfiles);
			}
		}
		return FileUtils.adapterResources(sqlfileArray);
	}*/
	
	/***
	 * 监测sql文件变化
	 * @param sqlfileArray
	 */
	protected void buildSqlFileMonitor(DbmSqlFileResource<?>... sqlfileArray){
		if(watchSqlFile){
			if(fileMonitor==null)
				fileMonitor = FileWatcher.newWatcher(1);
			this.watchFiles(sqlfileArray);
		}
	}
	protected void watchFiles(DbmSqlFileResource<?>[] sqlResourceArray){
		if(LangUtils.isEmpty(sqlResourceArray))
			return ;
		this.fileMonitor.watchFile(period, new FileChangeListener() {
			
			@Override
			public void fileChanged(ResourceAdapter<?> file) {
				try {
					reloadFile((DbmSqlFileResource<?>)file);
				} catch (Exception e) {
					logger.error("watch sql file error: " + e.getMessage(), e);
				}
			}
		}, sqlResourceArray);
	}
	
	public void reloadFile(DbmSqlFileResource<?> file){
		/*JFishProperties pf = loadSqlFile(file);
		if(pf==null){
			logger.warn("no file relaoded : " + file);
			return ;
		}*/
		NamedQueryFile namepsaceInfo = this.parseSqlFile(file, false);
		logger.warn("file relaoded : " + file);
		if(listener!=null){
			listener.afterReload(file, namepsaceInfo);
		}
	}
	
	private boolean isGlobalNamespace(String namespace){
		return GLOBAL_NS_KEY.equals(namespace) || !namespace.contains(".");
	}
	
	protected Map<String, NamedQueryFile> parseSqlFiles(DbmSqlFileResource<?>[] sqlfileArray){
		if(LangUtils.isEmpty(sqlfileArray)){
			logger.info("no named sql file found.");
			return Collections.emptyMap();
		}
		
//		Map<String, PropertiesNamespaceInfo<T>> nsproperties = LangUtils.newHashMap(sqlfileArray.length);
		Stream.of(sqlfileArray).parallel().forEach( f-> {
//			logger.info("parse named sql file: {}", f);
			this.parseSqlFile(f, true);
		});
		return namespaceProperties;
	}


	protected NamedQueryFile parseSqlFile(DbmSqlFileResource<?> f, boolean throwIfExist){
		logger.info("parse named sql file: {}", f.getName());
		
		String namespace = getFileNameNoJfishSqlPostfix(f);
		boolean globalNamespace = isGlobalNamespace(namespace);
		if(globalNamespace){
			namespace = GLOBAL_NS_KEY;
		}
		

		NamedQueryFile namespaceInfo = null;
		if(globalNamespace){
			namespaceInfo = namespaceProperties.get(namespace);
			if(namespaceInfo==null){
				namespaceInfo = new GlobalNamespaceProperties();
				namespaceProperties.put(namespaceInfo.getKey(), namespaceInfo);
			}
//			np.addAll(namedinfos, throwIfExist);
		}else{
			if(namespaceProperties.containsKey(namespace)){
				namespaceInfo = namespaceProperties.get(namespace);
				if (throwIfExist) {
					throw new DbmException("sql namespace has already exist : " + namespace+", file: " + f+", exists file: "+ namespaceInfo.getSource());
				}
			} else {
				namespaceInfo = new CommonNamespaceProperties(namespace, f);
				namespaceProperties.put(namespaceInfo.getKey(), namespaceInfo);
			}
		}
		
		buildNamedInfosToNamespaceFromResource(namespaceInfo, f);
		/*if(namedinfos.isEmpty())
			return null;*/

//		namespacesMap.put(namespace, np);
		for(FileBaseNamedQueryInfo nsp : namespaceInfo.getNamedProperties()){
			if(namedQueryCache.containsKey(nsp.getFullName()) && throwIfExist){
				throw new FileNamedQueryException("cache file named query error, key is exists : " + nsp.getFullName());
			}
			putIntoCaches(nsp.getFullName(), nsp);
		}
		
		return namespaceInfo;
	}
	
	protected void putIntoCaches(String key, FileBaseNamedQueryInfo nsp){
		logger.info("cache query : {}", key);
		this.namedQueryCache.put(key, nsp);
	}
	
	protected String getFileNameNoJfishSqlPostfix(ResourceAdapter<?> f){
		String fname = f.getName();
		String postfix = f.getPostfix();
		if(!fname.endsWith(postfix)){
			return fname;
		}else{
			return fname.substring(0, fname.length()-postfix.length());
		}
	}

	/****
	 * 解释sql文件，构建为对象
	 * @param np
	 * @param file
	 */
	protected void buildNamedInfosToNamespaceFromResource(NamedQueryFile np, DbmSqlFileResource<?> file){
//		sqlFileParser.parseToNamedQueryFile(np, file);
		
		this.queryInfoParsers.forEach(parser->{
			parser.parseToNamedQueryFile(np, file);
		});
		
		//post process 
	}
	
	/*private static final Pattern DIPATCHER_PATTER = Pattern.compile("\\((\\w+)(?:\\|{2}(\\w+))*\\)");
	private void postProccessNamedInnfo(PropertiesNamespaceInfo<T> np){
		//JFishNamedFileQueryInfo
		np.getNamedProperties().forEach(prop->{
			Matcher matcher = DIPATCHER_PATTER.matcher(prop.getName());
			if(matcher.find()){
				
			}
		});
	}*/
	@Override
	public NamedQueryInfo getNamedQueryInfo(String fullname) {
		FileBaseNamedQueryInfo info = namedQueryCache.get(fullname);
		return info;
	}

	@Override
	public boolean contains(String fullname){
		return namedQueryCache.containsKey(fullname);
	}

	@Override
	public boolean containsNamespace(String namespace){
		return namespaceProperties.containsKey(namespace);
	}

	public String toString(){
		StringBuilder sb = new StringBuilder("named query : \n");
		for(NamedQueryInfo info : this.namedQueryCache.values()){
			sb.append(info).append(",\n");
		}
		return sb.toString();
	}

	@Override
	public NamedQueryFile getNamespaceProperties(String namespace) {
		return namespaceProperties.get(namespace);
	}

	public Collection<NamedQueryFile> getAllNamespaceProperties() {
		return namespaceProperties.values();
	}
	
	/*final public void setSqlFileParser(DbmNamedQueryInfoParser sqlFileParser) {
		this.sqlFileParser = sqlFileParser;
	}*/



	public static class CommonNamespaceProperties implements NamedQueryFile {
		private final String namespace;
		private ResourceAdapter<?> source;
		private Map<String, FileBaseNamedQueryInfo> dbmNamedQueryInfoMap;
		
		public CommonNamespaceProperties(String namespace){
			this.namespace = namespace;
			this.dbmNamedQueryInfoMap = LangUtils.newHashMap();
		}
		public CommonNamespaceProperties(String namespace, ResourceAdapter<?> source) {
			super();
			this.namespace = namespace;
			this.source = source;
			this.dbmNamedQueryInfoMap = LangUtils.newHashMap();
//			this.namedProperties = namedProperties;
		}

		@Override
		public String getNamespace() {
			return namespace;
		}

		/****
		 * cache key
		 */
		@Override
		public String getKey() {
			return namespace;
		}

		@Override
		public Collection<FileBaseNamedQueryInfo> getNamedProperties() {
			return ImmutableList.copyOf(dbmNamedQueryInfoMap.values());
		}

		public ResourceAdapter<?> getSource() {
			return source;
		}
		@Override
		public FileBaseNamedQueryInfo getNamedProperty(String name) {
			return dbmNamedQueryInfoMap.get(name);
		}
		@Override
		public void addAll(Map<String, FileBaseNamedQueryInfo> namedInfos, boolean throwIfExist) {
			for(Entry<String, FileBaseNamedQueryInfo> entry : namedInfos.entrySet()){
				put(entry.getKey(), entry.getValue(), throwIfExist);
			}
		}
		@Override
		public void put(String name, FileBaseNamedQueryInfo info, boolean throwIfExist) {
			Assert.hasText(name);
			Assert.notNull(info);
			if(throwIfExist && this.dbmNamedQueryInfoMap.containsKey(name)){
				FileBaseNamedQueryInfo exitProp = this.dbmNamedQueryInfoMap.get(name);
				throw new BaseException("int file["+info.getSrcfile()+"], sql key["+name+"] has already exist in namespace: " + namespace+", in file: "+ exitProp.getSrcfile());
			}
			this.dbmNamedQueryInfoMap.put(name, info);
			/*DbmNamedQueryInfo newE = this.dbmNamedQueryInfoMap.get(name);
			logger.info("newE:"+newE);*/
		}
		@Override
		public boolean isGlobal() {
			return false;
		}
		
	}

	public class GlobalNamespaceProperties extends CommonNamespaceProperties {
		private final List<File> sources;
		
		private GlobalNamespaceProperties() {
			super("");
			this.sources = LangUtils.newArrayList();
		}

		@Override
		public String getKey() {
			return GLOBAL_NS_KEY;
		}

		public List<File> getSources() {
			return sources;
		}
		@Override
		public boolean isGlobal() {
			return true;
		}
	}
}
