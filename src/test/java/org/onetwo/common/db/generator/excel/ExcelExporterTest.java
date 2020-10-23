package org.onetwo.common.db.generator.excel;
/**
 * @author weishao zeng
 * <br/>
 */

import javax.sql.DataSource;

import org.junit.Test;
import org.onetwo.common.db.generator.ftl.TomcatDataSourceBuilder;

public class ExcelExporterTest {

	String dbname = "jormtest";
	String dburl = "jdbc:mysql://localhost:3306/"+dbname+"?&useSSL=false&characterEncoding=UTF-8";
	DataSource dataSource = TomcatDataSourceBuilder.newBuilder()
			.mysql(dbname, "root", "root")
			.url(dburl)
			.build();
	
	ExcelExporter export = ExcelExporter.create(dataSource);
	
	@Test
	public void test() {
		export.exportTableShema("test_user", "employee");
	}

}
