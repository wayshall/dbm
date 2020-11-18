package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.date.NiceDate;
import org.onetwo.common.dbm.model.entity.UserAutoidEntity;
import org.onetwo.common.dbm.model.entity.UserAutoidEntity.UserStatus;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.dbm.model.service.NoAutoIdUserService;
import org.onetwo.common.dbm.model.service.UserAutoidServiceImpl;
import org.onetwo.common.profiling.TimeCounter;
import org.onetwo.common.utils.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

//@Rollback(false)
public class DbmBatchInsertOrUpdateTest extends DbmBaseTest {

	@Resource
	private UserAutoidServiceImpl userAutoidServiceImpl;
	@Autowired
	private NoAutoIdUserService noAutoIdUserService;
	
	@Test
	public void testBatchInsert(){
		this.userAutoidServiceImpl.removeAll();
		int insertCount = 1000;
		//精确到秒，否则会有误差，比如2015-05-06 13:49:09.783存储到mysql后会变成2015-05-06 13:49:10，mysql的datetime只能精确到秒
		String userNamePrefix = "testBatchInsert";
		NiceDate niceNowSeconde = NiceDate.New().preciseAtSec();
		TimeCounter t = TimeCounter.start(userNamePrefix);
		int count = this.userAutoidServiceImpl.daoBatchInsert(userNamePrefix, UserStatus.NORMAL, niceNowSeconde.getTime(), insertCount);
		Assert.assertEquals(insertCount, count);
		t.stop();
		// 如果消耗时间超过30秒，断言失败…… 一万数据通常指需要几秒……
		assertThat(t.getCostTime()/1000).isLessThan(30);
		
		Page<UserAutoidEntity> page = Page.create();
		this.userAutoidServiceImpl.findUserPage(page, userNamePrefix);
		Assert.assertEquals(Math.min(insertCount, page.getResult().size()), page.getResult().size());
		page.getResult().stream().forEach(u->Assert.assertTrue(u.getUserName().contains(userNamePrefix)));
		
		List<UserAutoidEntity> dbUsers = this.userAutoidServiceImpl.findAllByUserNamePrefix(userNamePrefix);
		String updateUserNamePrefix = "batchUpdateUserName-";
		dbUsers.forEach(u -> {
			u.setUserName(updateUserNamePrefix);
		});
		int updateCount = this.userAutoidServiceImpl.batchUpdate(dbUsers);
		assertThat(updateCount).isEqualTo(dbUsers.size());
		List<UserAutoidEntity> updatedUsers = this.userAutoidServiceImpl.findAllByUserNamePrefix(updateUserNamePrefix);
		assertThat(updatedUsers.size()).isEqualTo(dbUsers.size());
		updatedUsers.forEach(u -> {
			assertThat(u.getUserName().startsWith(updateUserNamePrefix)).isTrue();
		});
		
		String userNamePrefix2 = userNamePrefix + "2"; 
		count = this.userAutoidServiceImpl.daoBatchInsert2(userNamePrefix2, insertCount);
		Assert.assertEquals(insertCount, count);
		
		count = this.userAutoidServiceImpl.removeByUserName("%"+userNamePrefix+"%");
		System.out.println("delete count: " + count);
		Assert.assertTrue(count==insertCount);

		count = this.userAutoidServiceImpl.removeByUserName("%"+updateUserNamePrefix+"%");
		System.out.println("delete count: " + count);
		Assert.assertTrue(count==insertCount);
	}
	

	@Test
	public void testBatchInsertOrUpdate(){
		noAutoIdUserService.deleteAll();
		int insertCount = 1000;
		int startId = 1;
		String prefix1 = "testBatchInsert";
		this.noAutoIdUserService.batchInsertOrUpdateUser(prefix1, startId, insertCount);
		
		UserEntity user = noAutoIdUserService.findById(1L);
		assertThat(user.getUserName()).startsWith(prefix1);
		user = noAutoIdUserService.findById((long)insertCount);
		assertThat(user.getUserName()).startsWith(prefix1);
		
		prefix1 = "testBatchUpdate";
		try {
			this.noAutoIdUserService.batchInsertUser(startId, insertCount*2);
			Assert.fail("must be fail!");
		} catch (Exception e) {
			assertThat(e).isInstanceOf(DuplicateKeyException.class);
		}
		
		
		this.noAutoIdUserService.batchInsertOrUpdateUser(prefix1, startId, insertCount*2);
		
		user = noAutoIdUserService.findById(1L);
		assertThat(user.getUserName()).startsWith(prefix1);
		user = noAutoIdUserService.findById((long)insertCount);
		assertThat(user.getUserName()).startsWith(prefix1);
		
		int count = noAutoIdUserService.count();
		assertThat(count).isEqualTo(insertCount*2);
	}
	
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
