package org.springframework.data.jdbc.dbm.repository;

import org.springframework.data.jdbc.dbm.domain.TestUserDomain;
import org.springframework.data.repository.CrudRepository;

/**
 * @author weishao zeng
 * <br/>
 */
public interface TestUserRepository extends CrudRepository<TestUserDomain, Long>{

}

