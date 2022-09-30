package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.dbm.model.entity.UserLongVersionEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserGenders;
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
	
	private volatile AtomicBoolean success = new AtomicBoolean(true);
	
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
		assertThat(user.getDataVersion()).isEqualTo(1);
		
		LangUtils.await(1);
		UserLongVersionEntity quser = userLongVersionService.findById(id);
		Assert.assertNotNull(quser);
		Assert.assertEquals(user.getId(), quser.getId());
		Assert.assertEquals(user.getUserName(), quser.getUserName());
		Assert.assertEquals(user.getGender(), quser.getGender());
		quser.setAge(35);
		userLongVersionService.update(quser);
		assertThat(quser.getDataVersion()).isEqualTo(2);
		
		LangUtils.await(1);
		user.setAge(30);
		// 这里拿旧的实体更新，上面更新过，所以这里抛错
		assertThatThrownBy(()->{
			userLongVersionService.update(user);
		})
		.isInstanceOf(EntityVersionException.class)
		.hasFieldOrPropertyWithValue("lastVersion", quser.getDataVersion());

		// 重新加载保存
		UserLongVersionEntity reloaduser = userLongVersionService.reload(user);
		assertThat(reloaduser.getDataVersion()).isEqualTo(2);

		CyclicBarrier barrier = new CyclicBarrier(2);
		final CountDownLatch latch = new CountDownLatch(1);
		executorService.execute(()->{
			try {
				userLongVersionService.updateWithCyclicBarrier(id, barrier);
				success.set(false);
				Assert.fail("实体已被主线程更新过，版本已修改，应该抛出 EntityVersionException 异常");
			} catch(EntityVersionException e) {
				Assert.assertNotNull(e.getEntityVersion());
				System.out.println(e);
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});
		LangUtils.await(2);	// 等待2秒，确保另外线程的updateWithCountDownLatch1方法，先开始事务查询
		UserLongVersionEntity user40age = userLongVersionService.update(id, 40);
		barrier.await(); // 调用await，让updateWithCountDownLatch1方法开始执行更新操作
		assertThat(user40age.getAge()).isEqualTo(40);
		assertThat(user40age.getDataVersion()).isEqualTo(3);
		assertThat(success.get()).isTrue();

		latch.await();
		assertThat(success.get()).isTrue();
		
		
		
		barrier.reset();
		CountDownLatch latch2 = new CountDownLatch(1);
		success.set(true);
		executorService.execute(()->{
			try {
				userLongVersionService.remove(id, barrier);
				success.set(false);
				Assert.fail("实体已被主线程更新过，版本已修改，应该抛出 EntityVersionException 异常");
			} catch(EntityVersionException e) {
				Assert.assertNotNull(e.getEntityVersion());
				System.out.println(e);
				e.printStackTrace();
			} finally {
				latch2.countDown();
			}
		});
		UserLongVersionEntity user50age = userLongVersionService.update(id, 50);
		barrier.await(); // 调用await，让updateWithCountDownLatch1方法开始执行更新操作
		assertThat(user50age.getAge()).isEqualTo(50);
		assertThat(user50age.getDataVersion()).isEqualTo(4);
		

		latch2.await();
		assertThat(success.get()).isTrue();
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
		assertThat(user.getDataVersion()).isEqualTo(1);
		
		user.setAge(40);
		UserLongVersionEntity userV2 = userLongVersionService.dymanicUpdate(user);
		UserLongVersionEntity reloadUser = userLongVersionService.reload(userV2);
		assertThat(reloadUser.getAge()).isEqualTo(40);
		assertThat(reloadUser.getDataVersion()).isEqualTo(2);

		UserLongVersionEntity userV3 = userLongVersionService.dymanicUpdate(userV2);
		assertThat(userV3.getDataVersion()).isEqualTo(3);
		
		assertThatThrownBy(()->{
			userLongVersionService.remove(reloadUser);
		})
		.isInstanceOf(EntityVersionException.class)
		.hasFieldOrPropertyWithValue("lastVersion", userV3.getDataVersion());
		
		// 测试获取新的版本值 val = mfield.getVersionableType().getVersionValule(val);
		
	}
}
