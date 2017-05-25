package org.onetwo.common.dbm.model.dao;

import java.util.List;

import org.onetwo.common.dbm.model.entity.UserTableIdEntity;

/**
 * @author wayshall
 * <br/>
 */
public interface CustomUserDao {
	
	int batchInsert(List<UserTableIdEntity> users);

}
