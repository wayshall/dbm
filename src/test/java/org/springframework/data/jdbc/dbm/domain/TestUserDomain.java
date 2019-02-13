package org.springframework.data.jdbc.dbm.domain;
/**
 * @author weishao zeng
 * <br/>
 */

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class TestUserDomain {
	@Id
	private Long id;
	private String userName;

}

