package org.springframework.data.jdbc.dbm;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.dbm.SpringDataJdbcBaseTest.SpringDataJdbcTestConfig;
import org.springframework.data.jdbc.dbm.domain.TestUserDomain;
import org.springframework.data.jdbc.dbm.repository.TestUserRepository;

/**
 * @author weishao zeng
 * <br/>
 */
public class TestUserRepositoryTest extends SpringDataJdbcTestConfig {

	@Autowired
	private TestUserRepository testUserRepository;
	
	@Test
	public void testSave() {
		TestUserDomain user = new TestUserDomain();
		testUserRepository.save(user);
	}
	
}

