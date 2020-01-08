package org.onetwo.common.db.generator;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

import org.onetwo.common.db.generator.DbGenerator.DbTableGenerator;
import org.onetwo.common.db.generator.DbGenerator.DbTableGenerator.TableGeneratedConfig;
import org.onetwo.common.db.generator.GlobalConfig.OutfilePathFunc;
import org.onetwo.common.db.generator.ftl.FtlEngine;
import org.onetwo.common.db.generator.ftl.TomcatDataSourceBuilder;
import org.onetwo.common.file.FileUtils;
import org.onetwo.common.spring.Springs;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.ui.meta.UIClassMeta;
import org.onetwo.dbm.ui.spi.UIClassMetaManager;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

/**
 * @author wayshall
 * <br/>
 */
public class DbmGenerator {

	public static DbmGenerator dataSource(DataSource dataSource){
		DbmGenerator generator = new DbmGenerator(new DbGenerator(dataSource, new FtlEngine()));
		return generator;
	}

	public static DbmGenerator createWithDburl(String dburl, String dbusername, String dbpassword){
		DataSource dataSource = TomcatDataSourceBuilder.newBuilder()
														.mysql(null, dbusername, dbpassword)
														.url(dburl)
														.build();
		DbmGenerator generator = new DbmGenerator(new DbGenerator(dataSource, new FtlEngine()));
		return generator;
	}
	
	public static DbmGenerator mysql(String dbname, String dbusername, String dbpassword){
		DataSource dataSource = TomcatDataSourceBuilder.newBuilder()
														.mysql(dbname, dbusername, dbpassword)
														.build();
		DbmGenerator generator = new DbmGenerator();
		generator.dbGenerator = new DbGenerator(dataSource, new FtlEngine());
		return generator;
	}
	
	private DbGenerator dbGenerator;
	private String projectPath = FileUtils.getMavenProjectDir().getPath();
	private String resourceDir = LangUtils.toString("${0}/src/main/resources", this.projectPath);
	private String javaSrcDir = LangUtils.toString("${0}/src/main/java", this.projectPath);
	private String pageFileBaseDir = LangUtils.toString("${0}/src/main/resources/templates", this.projectPath);
	
	private String testJavaSrcDir = LangUtils.toString("${0}/src/test/java", this.projectPath);
	private String testResourceDir = LangUtils.toString("${0}/src/test/resources", this.projectPath);
	private String testPageFileBaseDir = LangUtils.toString("${0}/src/test/resources/templates", this.projectPath);
	
	private String javaBasePackage;
	private String moduleName = "";
	private String stripTablePrefix = "";
	
	private String templateBasePath = "META-INF/dbgenerator/";
	
	private WebadminGenerator webadmin;
	private VuePageGenerator vueGenerator;
	
	private Map<String, Object> context = Maps.newHashMap();
	
	private boolean configured;
	
	public DbmGenerator(DbGenerator dbGenerator) {
		super();
		this.dbGenerator = dbGenerator;
	}
	
	private DbmGenerator() {
		super();
	}
	
	protected final void checkConfigured(String methodName){
		if(configured){
			throw new DbmException("dbGenerator has been configured, can not invoke method: " + methodName);
		}
	}
	
	public DbGenerator dbGenerator() {
		return dbGenerator;
	}

	public DbmGenerator projectPath(String projectPath) {
		this.projectPath = projectPath;
		return this;
	}

	public DbmGenerator pageFileBaseDir(String pageFileBaseDir) {
		this.pageFileBaseDir = pageFileBaseDir;
		this.dbGenerator.globalConfig().pageFileBaseDir(pageFileBaseDir);
		return this;
	}

	public DbmGenerator configGenerator(Consumer<DbGenerator> configurer) {
		configurer.accept(dbGenerator);
		this.configured = true;
		return this;
	}
	
	public DbmGenerator mavenProjectDir(){
		Assert.hasText(javaBasePackage, "javaBasePackage not set!");
		return configGenerator(dbGenerator->{
			dbGenerator.stripTablePrefix(stripTablePrefix)
			.globalConfig()
				.projectPath(projectPath)
//				.pageFileBaseDir(pageFileBaseDir)
				.resourceDir(resourceDir)
				.javaSrcDir(javaSrcDir)
				.javaBasePackage(javaBasePackage)
				.moduleName(moduleName)
//				.defaultTableContexts()
//				.end()
			.end();
		});
	}
	
	public DbmGenerator mavenProjectTestDir(){
		Assert.hasText(javaBasePackage, "javaBasePackage not set!");
		return configGenerator(dbGenerator->{
			dbGenerator.stripTablePrefix(stripTablePrefix)
			.globalConfig()
				.projectPath(projectPath)
				.pageFileBaseDir(testPageFileBaseDir)
				.resourceDir(testResourceDir)
				.javaSrcDir(testJavaSrcDir)
				.javaBasePackage(javaBasePackage)
				.moduleName(moduleName)
//				.defaultTableContexts()
//				.end()
			.end();
		});
	}
	
	public DbmGenerator pluginProjectDir(String pluginName){
		/*
		 * if (StringUtils.isBlank(moduleName)) { moduleName(pluginName); }
		 */
		this.mavenProjectDir();
		pageFileBaseDir = LangUtils.toString("${0}/src/main/resources/META-INF/resources/webftls/"+pluginName, this.projectPath);
		this.dbGenerator.globalConfig().pageFileBaseDir(pageFileBaseDir);
		
		return this;
	}
	
	public DbmGenerator javaBasePackage(String javaBasePackage) {
		this.checkConfigured("javaBasePackage");
		this.javaBasePackage = javaBasePackage;
		return this;
	}

	public DbmGenerator moduleName(String moduleName) {
		this.checkConfigured("moduleName");
		this.moduleName = moduleName;
		return this;
	}

	public DbmGenerator stripTablePrefix(String stripTablePrefix) {
		this.checkConfigured("stripTablePrefix");
		this.stripTablePrefix = stripTablePrefix;
		return this;
	}

	public WebadminGenerator webadminGenerator(String tableName){
		DbTableGenerator tableGenerator = dbGenerator.table(tableName);
		webadmin = new WebadminGenerator();
		webadmin.tableGenerator = tableGenerator;
		return webadmin;
	}

	public VuePageGenerator vueGenerator(String tableName, String vueModuleName){
		DbTableGenerator tableGenerator = dbGenerator.table(tableName);
		vueGenerator = new VuePageGenerator();
		vueGenerator.tableGenerator = tableGenerator;
		vueGenerator.vueModuleName = vueModuleName;
		tableGenerator.context().put("vueModuleName", vueModuleName);
		getUIClassMeta(tableName).ifPresent(meta -> {
			tableGenerator.context().put("UIClassMeta", meta);
		});
//		vueGenerator.vueBaseDir = vueBaseDir;
		return vueGenerator;
	}
	
	private Optional<UIClassMeta> getUIClassMeta(String tableName) {
		UIClassMeta meta = null;
		UIClassMetaManager uiClassMetaManager = Springs.getInstance().getBean(UIClassMetaManager.class); 
		if (uiClassMetaManager!=null) {
			meta = uiClassMetaManager.getByTable(tableName);
		}
		return Optional.ofNullable(meta);
	}
	
	/****
	 * @see build()
	 * @author wayshall
	 */
	@Deprecated
	public void generate(){
//		Assert.hasText(javaSrcDir, "javaSrcDir not set!");
//		Assert.hasText(javaBasePackage, "javaBasePackage not set!");
//		generate(context);
		build().generate(context);
	}

	/****
	 * @see build()
	 * @author wayshall
	 */
	@Deprecated
	public void generate(Map<String, Object> context){
//		if(context!=this.context){
//			this.context.putAll(context);
//		}
//		if(webadmin!=null){
//			List<GeneratedResult<File>> resullt = this.dbGenerator.generate(context);
//			System.out.println("webadmin result: " + resullt);
//		}
		build().generate(context);
	}
	
	public GeneratorExecutor build(){
		if(!this.configured){
//			throw new BaseException("dbGenerator has not been configured");
			this.mavenProjectDir();
		}
		GeneratorExecutor executor = new GeneratorExecutor();
		return executor;
	}
	
	public class GeneratorExecutor {
		
		public void generate(){
			Assert.hasText(javaSrcDir, "javaSrcDir not set!");
			Assert.hasText(javaBasePackage, "javaBasePackage not set!");
			generate(context);
		}
		
		public void generate(Map<String, Object> context){
			if(context!=DbmGenerator.this.context){
				DbmGenerator.this.context.putAll(context);
			}
			if(webadmin!=null || vueGenerator!=null){
				List<GeneratedResult<File>> resullt = dbGenerator.generate(context);
				System.out.println("generate result: " + resullt);
			}
		}
	}
	
	public class WebadminGenerator {
		private String templateName = templateBasePath + "webadmin";
		private DbTableGenerator tableGenerator;
		
		public WebadminGenerator generateServiceImpl(){
			tableGenerator.serviceImplTemplate(templateName+"/ServiceImpl.java.ftl");
			return this;
		}
		
		public WebadminGenerator generateController(Class<?> pluginBaseController){
			context.put("pluginBaseController", pluginBaseController.getName());
			tableGenerator.javaClassTemplate("controller", templateName+"/Controller.java.ftl");
			return this;
		}
		
		public WebadminGenerator generateVueController(Class<?> pluginBaseController){
			context.put("pluginBaseController", pluginBaseController.getName());
			tableGenerator.javaClassTemplate("controller", templateName+"/MgrController.java.ftl");
			return this;
		}
		
		public WebadminGenerator generateEntity(){
			tableGenerator.entityTemplate(templateName+"/Entity.java.ftl");
			return this;
		}
		
		public WebadminGenerator generateUIEntity(){
			tableGenerator.entityTemplate(templateName+"/UIEntity.java.ftl");
			return this;
		}
		
		public WebadminGenerator generatePage(){
			tableGenerator.pageTemplate(templateName+"/index.html.ftl");
			tableGenerator.pageTemplate(templateName+"/edit-form.html.ftl");
			return this;
		}
		public WebadminGenerator generate(String templatePath, OutfilePathFunc outFileNameFunc){
			tableGenerator.pageTemplate(templatePath, outFileNameFunc);
			return this;
		}
		
		public DbmGenerator end(){
			return DbmGenerator.this;
		}
		
	}
	
	public class VuePageGenerator extends BaseDbmGenerator {
		private String templateName = templateBasePath + "vue";
//		String vueBaseDir;
		String viewDir = "/src/views";
		String vueModuleName;

		Function<String, OutfilePathFunc> vueFileNameFuncCreator = path -> {
			return ctx->{
				TableGeneratedConfig c = ctx.getConfig();
//				Assert.hasText(vueBaseDir, "vueBaseDir must be has text!");
				String tableShortName = c.tableNameStripStart(c.globalGeneratedConfig().getStripTablePrefix());
				String pageFileBaseDir = c.globalGeneratedConfig().getPageFileBaseDir();
				Assert.notNull(pageFileBaseDir, "pageFileBaseDir can not be null");
				String moduleName = viewModuleName(c);
				Assert.notNull(moduleName, "moduleName can not be null");
				
				String fileName = StringUtils.toCamel(tableShortName, false);
				// Mgr.vue
				String fileNameWithoutExt = FileUtils.getFileNameWithoutExt(path);
				// remove Mgr
				fileNameWithoutExt = StringUtils.trimStartWith(fileNameWithoutExt, "Mgr");
				String filePath = pageFileBaseDir + 
									viewDir +
									"/"+moduleName+"/"+
									fileName + 
									fileNameWithoutExt;
				return filePath;
			};
		};
		
		private String viewModuleName(TableGeneratedConfig c) {
			return StringUtils.isNotBlank(vueModuleName)?vueModuleName:c.globalGeneratedConfig().getModuleName();
		}
		
		public VuePageGenerator generateVueCrud(){
			this.generateVueJsApi("/src/api");
			this.generateVueMgr();
			this.generateVueMgrForm();
			return this;
		}
		
		public VuePageGenerator generateVueMgr(){
			String mgrPath = templateName+"/Mgr.vue.ftl";
			tableGenerator.pageTemplate(mgrPath, vueFileNameFuncCreator.apply(mgrPath));
			return this;
		}
		
		public VuePageGenerator generateVueMgrForm(){
			String mgrPath = templateName+"/Mgr.vue.ftl";
			tableGenerator.pageTemplate(mgrPath, vueFileNameFuncCreator.apply(mgrPath));
			
			String formPath = templateName+"/Form.vue.ftl";
			tableGenerator.pageTemplate(formPath, vueFileNameFuncCreator.apply(formPath));
			return this;
		}
		
		public VuePageGenerator generateVueJsApi(String apiDir){
//			String apiDir = "/src/api";
			Function<String, OutfilePathFunc> outFileNameFuncCreator = path -> {
				return ctx->{
					TableGeneratedConfig c = ctx.getConfig();
					String tableShortName = c.tableNameStripStart(c.globalGeneratedConfig().getStripTablePrefix());
					String pageFileBaseDir = c.globalGeneratedConfig().getPageFileBaseDir();
					Assert.notNull(pageFileBaseDir, "pageFileBaseDir can not be null");
					String moduleName = viewModuleName(c);
					Assert.notNull(moduleName, "moduleName can not be null");

					String fileName = StringUtils.toCamel(tableShortName, false);
					String filePath = pageFileBaseDir + 
									apiDir + 
									"/"+moduleName+"/"+
									fileName + 
									FileUtils.getFileNameWithoutExt(path);
					return filePath;
				};
			};
			
			String mgrPath = templateName+"/Api.js.ftl";
			tableGenerator.pageTemplate(mgrPath, outFileNameFuncCreator.apply(mgrPath));
			
			return this;
		}
	}
	
	abstract protected class BaseDbmGenerator {
		protected DbTableGenerator tableGenerator;
		
		public DbmGenerator end(){
			return DbmGenerator.this;
		}
	}
	
}
