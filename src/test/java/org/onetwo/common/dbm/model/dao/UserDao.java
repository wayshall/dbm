package org.onetwo.common.dbm.model.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EnumType;

import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.dquery.annotation.Param;
import org.onetwo.common.db.dquery.annotation.Query;
import org.onetwo.common.dbm.model.entity.UserTableIdEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserStatus;
import org.springframework.transaction.annotation.Transactional;

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
	
	/***
	 * Param注解可指定枚举取值方式，详见：DynamicMethod#convertQueryValue
	 * 
	 * @author weishao zeng
	 * @param status
	 * @return
	 */
	@Transactional
	List<UserEntity> findByUserStatus(@Param(value="status", enumType=EnumType.STRING) UserStatus status);
		

}
