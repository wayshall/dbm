package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onetwo.common.base.DbmBaseTestWithouTransactional;
import org.onetwo.common.dbm.model.entity.UserVersionEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserGenders;
import org.onetwo.common.dbm.model.service.UserVersionService;
import org.onetwo.common.utils.JodatimeUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.exception.EntityVersionException;

public class DbmDateVersionTest extends DbmBaseTestWithouTransactional {

	@Resource
	private UserVersionService userVersionService;
//	private ExecutorService executorService = Executors.newFixedThreadPool(2);
	
	@Before
	public void before() {
		this.userVersionService.removeAll();
	}

	@Test
	public void testSave() throws InterruptedException, BrokenBarrierException, Exception {
		CyclicBarrier barrier = new CyclicBarrier(2);
		CountDownLatch latch2 = new CountDownLatch(1);
		
		Long id = 1L;
		UserVersionEntity user = new UserVersionEntity();
		user.setUserName("JdbcTest");
		user.setBirthday(JodatimeUtils.parse("1982-05-06").toDate());
		user.setEmail("username@qq.com");
		user.setHeight(3.3f);
		user.setAge(28);
		user.setId(id);
		user.setGender(UserGenders.MALE);
		
		CompletableFuture<Void> first = CompletableFuture.runAsync(() -> {
			this.userVersionService.persist(user);
			assertThat(user.getId()).isEqualTo(id);
			

			UserVersionEntity quser = userVersionService.findById(id);
			Assert.assertNotNull(quser);
			Assert.assertEquals(user.getId(), quser.getId());
			Assert.assertEquals(user.getUserName(), quser.getUserName());
			Assert.assertEquals(user.getGender(), quser.getGender());
			quser.setAge(35);
			// 由于mysql的时间字段只能精确到秒，所以必须等待一秒后再更新，使更新的时候版本值能变化，否则下面拿旧实体更新也不会抛错
			LangUtils.await(1);
			userVersionService.update(quser);
			assertThat(quser.getUpdateAt().getTime()).isGreaterThan(user.getUpdateAt().getTime());
			
			LangUtils.await(1);
			user.setAge(30);
			// 这里拿旧的实体更新，上面更新过，所以这里抛错
			Object updateAt = user.getUpdateAt();
			assertThatThrownBy(()->{
				userVersionService.update(user);
			})
			.isInstanceOf(EntityVersionException.class)
			.hasFieldOrPropertyWithValue("entityVersion", updateAt);
			
			// 重新加载保存
			UserVersionEntity reloaduser = userVersionService.reload(user);
			assertThat(quser.getUpdateAt().getTime()).isEqualTo(reloaduser.getUpdateAt().getTime());

//			latch.countDown();
		});
		

		first.thenRunAsync(()->{
			try {
				userVersionService.updateWithCountDownLatch1(id, barrier, latch2);
				Assert.fail("实体已被主线程更新过，版本已修改，应该抛出 EntityVersionException 异常");
			} catch(EntityVersionException e) {
				Assert.assertNotNull(e.getEntityVersion());
				System.out.println(e);
				e.printStackTrace();
			} catch (Exception e) {
				Assert.fail("实体已被主线程更新过，版本已修改，应该抛出 EntityVersionException 异常");
			}
			
		});

		CompletableFuture<Void> second = CompletableFuture.runAsync(() -> {
			try {
				latch2.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
//			LangUtils.await(2);	// 等待2秒，确保另外线程的updateWithCountDownLatch1方法，先开始事务查询
			UserVersionEntity user40age = userVersionService.update(id, 40);
			try {
				barrier.await(); // 调用await，让updateWithCountDownLatch1方法开始执行更新操作
			} catch (InterruptedException | BrokenBarrierException e) {
				throw new RuntimeException(e);
			} 
			assertThat(user40age.getAge()).isEqualTo(40);
			assertThat(user40age.getUpdateAt().getTime()).isGreaterThan(user.getUpdateAt().getTime());
		});
		
		CompletableFuture.allOf(first, second).get();

	}
	

	@Test
	public void testDynamicUpdate()  {
		Long id = 10000000001L;
		UserVersionEntity user = new UserVersionEntity();
		user.setUserName("JdbcTest");
		user.setBirthday(JodatimeUtils.parse("1982-05-06").toDate());
		user.setEmail("username@qq.com");
		user.setHeight(3.3f);
		user.setAge(28);
		user.setId(id);
		user.setGender(UserGenders.MALE);
		this.userVersionService.persist(user);
		Assert.assertEquals(id, user.getId(), 0);
		long lastUpdateAt = user.getUpdateAt().getTime();
		
		LangUtils.await(1);
		user.setAge(40);
		UserVersionEntity newuser = userVersionService.dymanicUpdate(user);
		assertThat(newuser.getAge()).isEqualTo(40);
		assertThat(newuser.getUpdateAt().getTime()).isGreaterThan(lastUpdateAt);
		
	}
	
}
