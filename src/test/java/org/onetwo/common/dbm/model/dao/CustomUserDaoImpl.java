package org.onetwo.common.dbm.model.dao;

import java.util.Collection;
import java.util.List;

import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.UserTableIdEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wayshall
 * <br/>
 */
@Component
public class CustomUserDaoImpl implements CustomUserDao {
	@Autowired
	private BaseEntityManager baseEntityManager;

	@Override
	public int batchInsert(List<UserTableIdEntity> users) {
		Collection<UserTableIdEntity> dbusers = baseEntityManager.saves(users);
		return dbusers.size();
	}
	

}
