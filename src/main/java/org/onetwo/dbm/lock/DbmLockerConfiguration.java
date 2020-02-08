package org.onetwo.dbm.lock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author weishao zeng
 * <br/>
 */
@Configuration
public class DbmLockerConfiguration {
	
	@Bean
	public SimpleDBLocker simpleDBLocker() {
		return new SimpleDBLocker();
	}

}

