package org.onetwo.common.db.generator;

import static org.junit.Assert.*

import org.junit.Test

/**
 * @author wayshall
 * <br/>
 */
class DatasourceGeneratorTest {
	@Test
	void testGenerateCode() {
		DbmGenerator.createWithDburl("jdbc:mysql://localhost:3306/web_admin?useUnicode=true&characterEncoding=utf8&useSSL=true", "root", "root")
					.javaBasePackage("com.test")//基础包名
					.moduleName("adminModule")
					.projectPath($/D:\mydev\js\workspace\neo-vue-admin/$)
					.pageFileBaseDir($/D:\mydev\js\workspace\neo-vue-admin\src\views/$)
					.stripTablePrefix("admin_")
					.webadminGenerator("admin_user")//要生成的表名
//						.generateVueMgr()
						.generateVueMgrForm()
//						.generateVueJsApi()
					.end()
					.build()
					.generate();//生成文件
	}

}
