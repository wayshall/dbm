package org.onetwo.common.db.generator;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.onetwo.common.db.generator.DbGenerator.DbTableGenerator;
import org.onetwo.common.db.generator.ftl.FtlEngine;
import org.onetwo.common.db.generator.ftl.TomcatDataSourceBuilder;
import org.onetwo.common.file.FileUtils;
import org.onetwo.common.utils.LangUtils;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

/**
 * @author wayshall
 * <br/>
 */
public class DbmGenerator {

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
	private String pageFileBaseDir = LangUtils.toString("${0}/src/main/resources/templates", this.projectPath);
	private String resourceDir = LangUtils.toString("${0}/src/main/resources", this.projectPath);
	private String javaSrcDir = LangUtils.toString("${0}/src/main/java", this.projectPath);
	
	private String javaBasePackage;
	private String moduleName = "";
	private String stripTablePrefix = "";
	
	private String templateBasePath = "META-INF/dbgenerator/";
	
	private WebadminGenerator webadmin;
	
	private Map<String, Object> context = Maps.newHashMap();
	
	public DbGenerator dbGenerator() {
		return dbGenerator;
	}
	
	public DbmGenerator mavenProjectDir(){
		Assert.hasText(javaBasePackage, "javaBasePackage not set!");
		dbGenerator.stripTablePrefix(stripTablePrefix)
					.globalConfig()
						.pageFileBaseDir(pageFileBaseDir)
						.resourceDir(resourceDir)
						.javaSrcDir(javaSrcDir)
						.javaBasePackage(javaBasePackage)
						.moduleName(moduleName)
						.defaultTableContexts()
						.end()
					.end();
		return this;
	}
	
	public DbmGenerator pluginProjectDir(String pluginName){
		this.mavenProjectDir();
		pageFileBaseDir = LangUtils.toString("${0}/src/main/resources/META-INF/resources/webftls/"+pluginName, this.projectPath);
		this.dbGenerator.globalConfig().pageFileBaseDir(pageFileBaseDir);
		return this;
	}
	
	public DbmGenerator javaBasePackage(String javaBasePackage) {
		this.javaBasePackage = javaBasePackage;
		return this;
	}

	public DbmGenerator moduleName(String moduleName) {
		this.moduleName = moduleName;
		return this;
	}

	public DbmGenerator stripTablePrefix(String stripTablePrefix) {
		this.stripTablePrefix = stripTablePrefix;
		return this;
	}

	public WebadminGenerator webadminGenerator(String tableName){
		DbTableGenerator tableGenerator = dbGenerator.table(tableName);
		webadmin = new WebadminGenerator();
		webadmin.tableGenerator = tableGenerator;
		return webadmin;
	}

	
	public void generate(){
		generate(context);
	}
	
	public void generate(Map<String, Object> context){
		if(context!=this.context){
			this.context.putAll(context);
		}
		if(webadmin!=null){
			List<GeneratedResult<File>> resullt = this.dbGenerator.generate(context);
			System.out.println("webadmin result: " + resullt);
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
			tableGenerator.controllerTemplate("controller", templateName+"/Controller.java.ftl");
			return this;
		}
		
		public WebadminGenerator generateEntity(){
			tableGenerator.controllerTemplate(templateName+"/Entity.java.ftl");
			return this;
		}
		
		public WebadminGenerator generatePage(){
			tableGenerator.pageTemplate(templateName+"/index.html.ftl");
			tableGenerator.pageTemplate(templateName+"/edit-form.html.ftl");
			return this;
		}
		
		public DbmGenerator end(){
			return DbmGenerator.this;
		}
		
	}
	
}
