package org.onetwo.common.dbm.model.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.date.NiceDate;
import org.onetwo.common.dbm.model.entity.UserAutoidEntity;
import org.onetwo.dbm.core.spi.DbmSession;
import org.onetwo.dbm.utils.Dbms;
import org.springframework.transaction.annotation.Transactional;

public class UserAutoidServiceTest extends DbmBaseTest {
	
	@Resource
	private UserAutoidService userAutoidServiceImpl;
	
	@Resource
	private DataSource dataSource;
	
	@Test
	public void testCrud(){
		this._testCrud(userAutoidServiceImpl);
	}

	@Test
	@Transactional
	public void testCrudNoSpring(){
		DbmSession jfishDao = Dbms.newSessionFactory(dataSource).openSession();
		UserAutoidService us = new UserAutoidServiceNoSpringImpl(jfishDao);
		this._testCrud(us);
	}
	
	public void _testCrud(UserAutoidService userAutoidServiceImpl){
		userAutoidServiceImpl.deleteAll();
		
		String userNamePrefix = "userName";
		Date now = new Date();
		System.out.println("now: " + NiceDate.New(now).formatDateTimeMillis());
		
		//精确到秒，否则会有误差，比如2015-05-06 13:49:09.783存储到mysql后会变成2015-05-06 13:49:10，mysql的datetime只能精确到秒
		NiceDate niceNowSeconde = NiceDate.New(now).preciseAtSec();
		System.out.println("niceNowSeconde: " + niceNowSeconde.formatDateTimeMillis());
		int count = 100;
		int insertCount = userAutoidServiceImpl.saveList(userNamePrefix, niceNowSeconde.getTime(), count);
		assertThat(insertCount).isEqualTo(count);
		
		List<UserAutoidEntity> userlist = userAutoidServiceImpl.findUserAutoIdEntity(userNamePrefix, niceNowSeconde.getTime());
		Assert.assertEquals(count, userlist.size());
		
		final String newUserNamePrefix = "update-user-name";
		userlist.stream().forEach(e->  e.setUserName(newUserNamePrefix) );
		count = userAutoidServiceImpl.update(userlist);
		Assert.assertEquals(count, userlist.size());
		
		userlist = userAutoidServiceImpl.findUserAutoIdEntity(newUserNamePrefix, niceNowSeconde.getTime());
		Assert.assertEquals(count, userlist.size());
		
		count = userAutoidServiceImpl.delete(userlist);
		Assert.assertEquals(count, userlist.size());
	}

}
