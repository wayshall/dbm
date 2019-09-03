package org.onetwo.common.db.generator;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.generator.meta.ColumnMeta;
import org.onetwo.common.db.generator.meta.TableMeta;

/**
 * @author weishao zeng
 * <br/>
 */
public class DbmEntityGeneratorTest extends DbmBaseTest {

	@Resource
	private DataSource dataSource;
	
	@Test
	public void testJson() {
		DbmGenerator g = DbmGenerator.dataSource(dataSource)
			.javaBasePackage("org.onetwo.common.db.generator")//基础包名
			.mavenProjectTestDir()
			.webadminGenerator("test_user")//要生成的表名
			.generateEntity()
			.end();
		
		TableMeta table = g.dbGenerator().databaseMetaDialet().getTableMeta("test_user");
		ColumnMeta address = table.getColumn("address_list");
		int sqlType = address.getSqlType();
		System.out.println("sqlType:" + sqlType);
		System.out.println("isJsonType: " + address.isJsonType());
		
		g.build().generate();
	}

}

