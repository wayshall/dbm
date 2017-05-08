package org.onetwo.common.dbm.model.dao;

import java.util.List;

import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.dquery.annotation.QueryConfig;
import org.onetwo.common.dbm.model.entity.UserEntity;
import org.onetwo.common.utils.Page;

/**
 * @author wayshall
 * <br/>
 */
@DbmRepository
public interface UserQueryConfigDao {
	
	@QueryConfig(value="insert intotest_user_autoid (birthday, email, gender, mobile, nick_name, password, status, user_name) "
					+ " values (:email, :gender, :mobile, :nickName, :password, :status, :userName)")
	int batchSaveUsers(List<UserEntity> users);
	
	@QueryConfig(value="select u.* from t_user where t.user_name = :userName")
	Page<UserEntity> findUserPage(Page<UserEntity> page);

}
