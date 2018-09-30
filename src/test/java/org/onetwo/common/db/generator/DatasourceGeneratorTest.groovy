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
//						.generateVueCrud()//会在api目录生成userApi.js，在/views/adminModule/目录生成userMgr.vue和userMgrForm.vue两个文件，包含了基本的增删改查
//						.generateVueMgrForm()
//						.generateVueMgr()
					.end()
					.build()
					.generate();//生成文件
	}

}
