package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.dbm.model.entity.UserEntity.UserGenders;
import org.onetwo.common.dbm.model.entity.UserLongVersionEntity;
import org.onetwo.common.dbm.model.service.UserLongVersionService;
import org.onetwo.common.utils.JodatimeUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.exception.EntityVersionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

@Rollback(false)
public class DbmLongVersionTest extends DbmBaseTest {

	@Autowired
	private UserLongVersionService userLongVersionService;
	private ExecutorService executorService = Executors.newFixedThreadPool(2);
	
	@Before
	public void before() {
		this.userLongVersionService.removeAll();
	}

	@Test
	public void testSave() throws InterruptedException, BrokenBarrierException {
		Long id = 10000000000L;
		UserLongVersionEntity user = new UserLongVersionEntity();
		user.setUserName("JdbcTest");
		user.setBirthday(JodatimeUtils.parse("1982-05-06").toDate());
		user.setEmail("username@qq.com");
		user.setHeight(3.3f);
		user.setAge(28);
		user.setId(id);
		user.setGender(UserGenders.MALE);
		this.userLongVersionService.persist(user);
		Assert.assertEquals(10000000000L, user.getId(), 0);
		
		// 由于mysql的时间字段只能精确到秒，所以必须等待一秒后再更新，否则下面的抛错断言会出错
		LangUtils.await(1);
		UserLongVersionEntity quser = userLongVersionService.findById(id);
		Assert.assertNotNull(quser);
		Assert.assertEquals(user.getId(), quser.getId());
		Assert.assertEquals(user.getUserName(), quser.getUserName());
		Assert.assertEquals(user.getGender(), quser.getGender());
		quser.setAge(35);
		userLongVersionService.update(quser);
		
		LangUtils.await(1);
		user.setAge(30);
		// 这里拿旧的实体更新，上面更新过，所以这里抛错
		Object updateAt = user.getUpdateAt();
		assertThatThrownBy(()->{
			userLongVersionService.update(user);
		})
		.isInstanceOf(EntityVersionException.class)
		.hasFieldOrPropertyWithValue("entityVersion", updateAt);
		
		// 重新加载保存
		userLongVersionService.reloadAndUpdate(user);


		CyclicBarrier barrier = new CyclicBarrier(2);
		CountDownLatch latch = new CountDownLatch(1);
		executorService.execute(()->{
			try {
				userLongVersionService.updateWithCountDownLatch1(id, barrier);
				Assert.fail("实体已被主线程更新过，版本已修改，应该抛出 EntityVersionException 异常");
			} catch(EntityVersionException e) {
				Assert.assertNotNull(e.getEntityVersion());
				System.out.println(e);
			}
			latch.countDown();
		});
		LangUtils.await(2);	// 等待2秒，确保另外线程的updateWithCountDownLatch1方法，先开始事务查询
		UserLongVersionEntity user40age = userLongVersionService.update(id, 40);
		barrier.await(); // 调用await，让updateWithCountDownLatch1方法开始执行更新操作
		assertThat(user40age.getAge()).isEqualTo(40);

		latch.await();
	}
	

	@Test
	public void testDynamicUpdate()  {
		Long id = 10000000001L;
		UserLongVersionEntity user = new UserLongVersionEntity();
		user.setUserName("JdbcTest");
		user.setBirthday(JodatimeUtils.parse("1982-05-06").toDate());
		user.setEmail("username@qq.com");
		user.setHeight(3.3f);
		user.setAge(28);
		user.setId(id);
		user.setGender(UserGenders.MALE);
		this.userLongVersionService.persist(user);
		Assert.assertEquals(id, user.getId(), 0);
		
		user.setAge(40);
		UserLongVersionEntity newuser = userLongVersionService.dymanicUpdate(user);
		assertThat(newuser.getAge()).isEqualTo(40);
		
		// 测试获取新的版本值 val = mfield.getVersionableType().getVersionValule(val);
		
	}
}
