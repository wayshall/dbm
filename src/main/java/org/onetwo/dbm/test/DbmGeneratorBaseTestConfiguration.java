package org.onetwo.dbm.test;

import javax.sql.DataSource;

import org.onetwo.common.db.generator.ftl.TomcatDataSourceBuilder;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.mapping.DefaultDbmConfig;
import org.springframework.context.annotation.Bean;

/**
 * @author weishao zeng
 * <br/>
 */

public class DbmGeneratorBaseTestConfiguration {

	protected String dburl;
	protected String dbusername;
	protected String dbpassword;
	
	@Bean
	public DataSource dataSource(){
		DataSource dataSource = TomcatDataSourceBuilder.newBuilder()
				.mysql(null, dbusername, dbpassword)
				.url(dburl)
				.build();
		return dataSource;
	}
	
	@Bean
	public DbmConfig dbmConfig(){
		return new DefaultDbmConfig();
	}
	
}
