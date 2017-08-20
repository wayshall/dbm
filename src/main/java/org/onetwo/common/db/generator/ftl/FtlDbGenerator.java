package org.onetwo.common.db.generator.ftl;

import javax.sql.DataSource;

import org.onetwo.common.db.generator.DbGenerator;

final public class FtlDbGenerator {
	public static DbGenerator newGenerator(DataSource dataSource){
		return new DbGenerator(dataSource, new FtlEngine());
	}
	public static DbGenerator newGenerator(String dbname, String dbusername, String dbpassword){
		DataSource dataSource = TomcatDataSourceBuilder.newBuilder()
														.mysql(dbname, dbusername, dbpassword)
														.build();
		return new DbGenerator(dataSource, new FtlEngine());
	}
	
	private FtlDbGenerator(){}
}
