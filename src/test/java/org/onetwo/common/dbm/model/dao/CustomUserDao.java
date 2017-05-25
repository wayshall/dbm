package org.onetwo.common.dbm.model.dao;

import java.util.List;

import org.onetwo.common.dbm.model.entity.UserTableIdEntity;
import org.onetwo.common.spring.aop.Mixin;

/**
 * @author wayshall
 * <br/>
 */
@Mixin(CustomUserDaoImpl.class)
public interface CustomUserDao {
	
	int batchInsert(List<UserTableIdEntity> users);

}
