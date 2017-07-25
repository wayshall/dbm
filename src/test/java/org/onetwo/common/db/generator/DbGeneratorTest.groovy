package org.onetwo.common.db.generator

import javax.sql.DataSource

import org.junit.Assert
import org.junit.Test
import org.onetwo.common.db.generator.dialet.DatabaseMetaDialet
import org.onetwo.common.db.generator.dialet.MysqlMetaDialet
import org.onetwo.common.db.generator.ftl.FtlDbGenerator
import org.onetwo.common.db.generator.ftl.TomcatDataSourceBuilder
import org.onetwo.common.db.generator.meta.TableMeta
import org.onetwo.common.file.FileUtils
import org.onetwo.common.utils.LangUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests

@ContextConfiguration(value="classpath:/db/generator/db-generator-test.xml")
class DbGeneratorTest extends AbstractJUnit4SpringContextTests {
	
	@Autowired
	private DataSource dataSource;
	
	@Test
	def void test(){
		println "hello world"
		Assert.assertNotNull(dataSource)
	}
	
	@Test
	def void testDatabaseMeta(){
		DatabaseMetaDialet dialet = new MysqlMetaDialet(dataSource)
		List<String> tables = dialet.getTableNames()
		println "tables:${tables}"
		Assert.assertNotNull(tables)
		Assert.assertTrue(!tables.isEmpty())
		
		String tablename = "admin_user"
		TableMeta tableMeta = dialet.getTableMeta(tablename);
		println "table: ${tableMeta}"
		Assert.assertTrue(tableMeta.getColumns().size()>3);
	}
	
	@Test
	def void testGeneratorCode(){
		
		def projectPath =$/G:\mydev\cloudsoft-workspace\sysu-manager/$
		def javaBasePackage = "com.sysu.manager"
		def moduleName = ""
		def stripTablePrefix = "sysu_"
		
		def dbname = "sysu"
		def dbusername = "root"
		def dbpassword = "root"
		
		
		def pageFileBaseDir = "${projectPath}/src/main/resources/templates"
		def resourceDir = "${projectPath}/src/main/resources"
		def javaSrcDir = "${projectPath}/src/main/java"
		
		def basePath = FileUtils.getResourcePath("");
		
		dataSource = TomcatDataSourceBuilder.newBuilder()
								.mysql(dbname, dbusername, dbpassword)
								.build();
		
		List<GeneratedResult<String>> gr = FtlDbGenerator.newGenerator(dataSource)
				//										.templateEngine(new FtlEngine())
														.mysql()
														.stripTablePrefix(stripTablePrefix)
														//.stripTablePrefix("zyt_estate_")
														.globalConfig()
															.pageFileBaseDir(pageFileBaseDir)
															.resourceDir(resourceDir)
															.javaSrcDir(javaSrcDir)
															.javaBasePackage(javaBasePackage)
															.moduleName(moduleName)
															.defaultTableContexts()
//																.stripTablePrefix("zyt_estate_")
															.end()
														.end()
														.table("sysu_type")
//														.table("")
															.pageTemplate("${basePath}/db/generator/product/index.html.ftl")
															.pageTemplate("${basePath}/db/generator/product/edit-form.html.ftl")
															.controllerTemplate("controller", "${basePath}/db/generator/product/Controller.java.ftl")
															.serviceImplTemplate("${basePath}/db/generator/product/ServiceImpl.java.ftl")
															.entityTemplate("entity", "${basePath}/db/generator/product/Entity.java.ftl", ".java")
															/*.daoTemplate("${basePath}/db/generator/datagrid/Dao.java.ftl")
															.entityTemplate("${basePath}/db/generator/datagrid/ExtEntity.java.ftl")
															.mybatisDaoXmlTemplate("${basePath}/db/generator/datagrid/Dao.xml.ftl")*/
														.end()
														.generate(LangUtils.asMap());
		println "gr:${gr}"
	}
	
	def void testGeneratorWebAdmin(){
		def basePath = FileUtils.getResourcePath("");
		
		List<GeneratedResult<String>> gr = FtlDbGenerator.newGenerator(dataSource)
				//										.templateEngine(new FtlEngine())
														.mysql()
														.stripTablePrefix("admin_")
														//.stripTablePrefix("zyt_estate_")
														.globalConfig()
															.pageFileBaseDir($/D:\mydev\java\workspace\bitbucket\onetwo\core\plugins\web-admin\src\main\resources\META-INF\resources\webftls\web-admin/$)
															.resourceDir($/D:\mydev\java\workspace\bitbucket\onetwo\core\plugins\web-admin\src\main\resources\META-INF\resources/$)
															.javaSrcDir($/D:\mydev\java\workspace\bitbucket\onetwo\core\plugins\web-admin\src\main\java/$)
															.javaBasePackage("org.onetwo.plugins")
															.moduleName("admin")
															.defaultTableContexts()
//																.stripTablePrefix("zyt_estate_")
															.end()
														.end()
														.table("admin_application")
//														.table("")
															.pageTemplate("${basePath}/db/generator/product/index.html.ftl")
															.pageTemplate("${basePath}/db/generator/product/edit-form.html.ftl")
															.controllerTemplate("controller", "${basePath}/db/generator/product/Controller.java.ftl")
															.serviceImplTemplate("${basePath}/db/generator/product/ServiceImpl.java.ftl")
															.entityTemplate("entity", "${basePath}/db/generator/product/Entity.java.ftl", ".java")
															/*.daoTemplate("${basePath}/db/generator/datagrid/Dao.java.ftl")
															.entityTemplate("${basePath}/db/generator/datagrid/ExtEntity.java.ftl")
															.mybatisDaoXmlTemplate("${basePath}/db/generator/datagrid/Dao.xml.ftl")*/
														.end()
														.generate(LangUtils.asMap());
		println "gr:${gr}"
	}

}
