package org.onetwo.common.dbm;

import java.util.List;

import jakarta.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.date.NiceDate;
import org.onetwo.common.dbm.model.entity.UserAutoidEntity;
import org.onetwo.common.dbm.model.entity.UserAutoidEntity.UserStatus;
import org.onetwo.common.dbm.model.service.UserAutoidServiceImpl;

//@Rollback(false)
public class DbmQueryDispatcherTest extends DbmBaseTest {

	@Resource
	private UserAutoidServiceImpl userAutoidServiceImpl;
	
	
	@Test
	public void testQuerySwitcher(){
		this.userAutoidServiceImpl.removeAll();
		int insertCount = 10;
		//精确到秒，否则会有误差，比如2015-05-06 13:49:09.783存储到mysql后会变成2015-05-06 13:49:10，mysql的datetime只能精确到秒
		String userNamePrefix = "testQuerySwitcher";
		NiceDate niceNowSeconde = NiceDate.New().preciseAtSec();
		int count = this.userAutoidServiceImpl.daoBatchInsert(userNamePrefix, UserStatus.NORMAL, niceNowSeconde.getTime(), insertCount);
		Assert.assertEquals(insertCount, count);
		
		count = this.userAutoidServiceImpl.daoBatchInsert(userNamePrefix, UserStatus.DELETE, niceNowSeconde.getTime(), insertCount);
		Assert.assertEquals(insertCount, count);
		
		List<UserAutoidEntity> userlist = userAutoidServiceImpl.findUserList(UserStatus.NORMAL.name());
		Assert.assertEquals(insertCount, userlist.size());
		userlist = userAutoidServiceImpl.findUserList(UserStatus.DELETE.name());
		Assert.assertEquals(insertCount, userlist.size());
		
		count = this.userAutoidServiceImpl.removeByUserName(userNamePrefix);
		System.out.println("delete count: " + count);
		Assert.assertTrue(count==insertCount*2);
	}

}
