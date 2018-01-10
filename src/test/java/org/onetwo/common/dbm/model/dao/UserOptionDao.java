package org.onetwo.common.dbm.model.dao;

import java.util.List;
import java.util.Optional;

import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.dquery.annotation.Query;
import org.onetwo.common.dbm.model.entity.UserEntity;

/**
 * @author wayshall
 * <br/>
 */
@DbmRepository
public interface UserOptionDao {

	@Query("select * from  TEST_USER u  where u.id = :userId")
	Optional<UserEntity> findById(Long userId);

	@Query("select * from  TEST_USER u  where u.id in (:userIds)")
	Optional<List<UserEntity>> findByIds(List<Long> userIds);
}
