package org.onetwo.common.db.generator.ftl;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.onetwo.common.spring.utils.BeanPropertiesMapper;

/**
 * @author wayshall
 * <br/>
 */
public class TomcatDataSourceBuilder {
	
	private int timeBetweenEvictionRunsMillis=60000;
	private int minEvictableIdleTimeMillis=60000;
	private boolean defaultAutoCommit=false;
	private int minIdle=5;
	private int maxIdle=10;
	private int maxActive=50;
	private int initialSize=5;
	
	private String driverClassName;
	private String username = "root";
	private String password= "root";
	private String url;
	
	private Properties config = new Properties();
	
	private TomcatDataSourceBuilder(){
	}
	
	public static TomcatDataSourceBuilder newBuilder(){
		return new TomcatDataSourceBuilder();
	}
	
	public DataSource build(){
		DataSource ds = new DataSource();
		ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		ds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		ds.setDefaultAutoCommit(defaultAutoCommit);
		ds.setMinIdle(minIdle);
		ds.setMaxIdle(maxIdle);
		ds.setMaxActive(maxActive);
		ds.setInitialSize(initialSize);
		ds.setDriverClassName(driverClassName);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setUrl(url);
		
		//other
		if(!config.isEmpty()){
			BeanPropertiesMapper mapper = new BeanPropertiesMapper(config, "");
			mapper.mapToObject(ds);
		}
		
		return ds;
	}
	
	public TomcatDataSourceBuilder mysql(String db, String username, String password){
		this.driverClassName = "com.mysql.jdbc.Driver";
		this.username = username;
		this.password = password;
		if(StringUtils.isNotBlank(db)){
			this.url = "jdbc:mysql://localhost:3306/"+db+"?useUnicode=true&amp;characterEncoding=UTF-8";
		}
		return this;
	}

	public TomcatDataSourceBuilder timeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
		return this;
	}

	public TomcatDataSourceBuilder minEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
		return this;
	}


	public TomcatDataSourceBuilder config(String property, String value) {
		this.config.setProperty(property, value);
		return this;
	}

	public TomcatDataSourceBuilder defaultAutoCommit(boolean defaultAutoCommit) {
		this.defaultAutoCommit = defaultAutoCommit;
		return this;
	}


	public TomcatDataSourceBuilder minIdle(int minIdle) {
		this.minIdle = minIdle;
		return this;
	}


	public TomcatDataSourceBuilder maxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
		return this;
	}


	public TomcatDataSourceBuilder maxActive(int maxActive) {
		this.maxActive = maxActive;
		return this;
	}

	public TomcatDataSourceBuilder initialSize(int initialSize) {
		this.initialSize = initialSize;
		return this;
	}


	public TomcatDataSourceBuilder driverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
		return this;
	}

	public TomcatDataSourceBuilder username(String username) {
		this.username = username;
		return this;
	}


	public TomcatDataSourceBuilder password(String password) {
		this.password = password;
		return this;
	}


	public TomcatDataSourceBuilder url(String url) {
		this.url = url;
		return this;
	}
	
	
	
}
