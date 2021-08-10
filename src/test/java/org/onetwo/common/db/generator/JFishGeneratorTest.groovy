package org.onetwo.common.db.generator

import javax.sql.DataSource

import org.junit.Assert
import org.junit.Test
import org.onetwo.common.db.generator.dialet.DatabaseMetaDialet
import org.onetwo.common.db.generator.dialet.MySQLDBMetaDialet
import org.onetwo.common.db.generator.ftl.FtlDbGenerator
import org.onetwo.common.db.generator.meta.TableMeta
import org.onetwo.common.file.FileUtils
import org.onetwo.common.utils.LangUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests

@ContextConfiguration(value="classpath:/db/generator/db-generator-test.xml")
class JFishGeneratorTest extends AbstractJUnit4SpringContextTests {
	
	@Autowired
	private DataSource dataSource;
	
	@Test
	def void testGenerator2(){
		def basePath = FileUtils.getResourcePath("");
		
		List<GeneratedResult<String>> gr = FtlDbGenerator.newGenerator(dataSource)
				//										.templateEngine(new FtlEngine())
														.mysql()
														.stripTablePrefix("")
														//.stripTablePrefix("zyt_estate_")
														.globalConfig()
//															.pageFileBaseDir($/D:\mydev\java\yooyo-workspace\zhiyetong-manager\src\main\resources\templates/$)
//															.resourceDir($/D:\mydev\java\yooyo-workspace\zhiyetong-manager\src\main\resources/$)
															.javaSrcDir($/G:\mydev\cloudsoft-workspace\sysu-service\src\main\java/$)
															.javaBasePackage("com.sysu")
//															.moduleName("admin")
															.moduleName("")
															.defaultTableContexts()
//																.stripTablePrefix("zyt_estate_")
															.end()
														.end()
//														.table("zyt_estate_rental_house")
														.table("SYSU_SPOT")
//														.table("")
//															.pageTemplate("${basePath}/db/generator/datagrid/index.html.ftl")
//															.pageTemplate("${basePath}/db/generator/datagrid/edit-form.html.ftl")
//															.controllerTemplate("${basePath}/db/generator/datagrid/Controller.java.ftl")
//															.serviceImplTemplate("${basePath}/db/generator/datagrid/ServiceImpl.java.ftl")
//															.daoTemplate("${basePath}/db/generator/datagrid/Dao.java.ftl")
															.entityTemplate("model", "${basePath}/db/generator/jfish/Entity.java.ftl")
//															.mybatisDaoXmlTemplate("${basePath}/db/generator/datagrid/Dao.xml.ftl")
														.end()
														.generate(LangUtils.asMap());
		println "gr:${gr}"
	}

}
