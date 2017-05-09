package org.onetwo.common.dbm.model.dao;

import java.util.List;

import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.dquery.annotation.Query;
import org.onetwo.common.dbm.model.entity.UserEntity;
import org.onetwo.common.utils.Page;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wayshall
 * <br/>
 */
@DbmRepository
@Transactional
public interface UserQueryConfigDao {
	
	@Query("insert into test_user (id, email, gender, mobile, nick_name, password, status, user_name) "
			+ " values (:id, :email, :gender, :mobile, :nickName, :password, :status, :userName)")
	int batchSaveUsers(List<UserEntity> users);
	
	@Query(value="select t.* from test_user t where 1=1 "
			+ "[#if userName?has_content] "
				+ "and t.user_name like :userName?likeString "
			+ "[/#if]")
	Page<UserEntity> findUserPage(Page<UserEntity> page, String userName);

}
