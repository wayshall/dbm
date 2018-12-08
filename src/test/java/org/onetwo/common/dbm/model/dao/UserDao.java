package org.onetwo.common.dbm.model.dao;

import java.util.ArrayList;
import java.util.List;

import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.dquery.annotation.Query;
import org.onetwo.common.dbm.model.entity.UserTableIdEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;

@DbmRepository
public interface UserDao extends CustomUserDao {
	
	List<UserTableIdEntity> findByUserNameLike(String userName);

	@Query(value="insert  into test_user " +
        " (id, birthday, email, gender, user_name) " +
        " values (:id, :birthday, :email, :gender.mappingValue, :userName)"
	)
	int batchInsertUsers(List<UserEntity> users);
	
	@Query(value="select * from test_user t where t.id in ( :userIds )")
	ArrayList<UserEntity> findUserWithIds(Long[] userIds);
		

}
