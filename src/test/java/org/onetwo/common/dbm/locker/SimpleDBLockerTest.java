package org.onetwo.common.dbm.locker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.profiling.TimeCounter;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.lock.SimpleDBLocker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;

/**
 * @author weishao zeng
 * <br/>
 */
@Commit
public class SimpleDBLockerTest extends DbmBaseTest {

	@Autowired
	private SimpleDBLocker dblocker; 
	
	@Test
	public void test() {
		String lockId = "test";
		dblocker.initLocker(lockId);
		
		int lockTime = 5;
		
		TimeCounter t = new TimeCounter("dblocker");
		t.start();
		
//		CountDownLatch latch = new CountDownLatch(1);
		new Thread(() -> {
			dblocker.lock(lockId, ()-> {
				LangUtils.await(lockTime);
//				latch.countDown();
				return null;
			});
		})
		.start();

		// 休眠，让上面的线程先执行
		LangUtils.await(1);
		dblocker.lock(lockId, ()-> {
			t.stop();
			return null;
		});
		assertThat(t.getCostTime()).isGreaterThan(TimeUnit.SECONDS.toMillis(lockTime));
	}
	
	/*@Test
	public void testTimeout() {
		String lockId = "testTimeout";
		dblocker.initLocker(lockId);
		
		int timeout = 3;
		int lockTime = 7;
		
		TimeCounter t = new TimeCounter("dblocker");
		t.start();
		
		CountDownLatch latch = new CountDownLatch(1);
		new Thread(() -> {
			try {
				dblocker.lock(lockId, timeout, ()-> {
					LangUtils.await(lockTime);
					return null;
				});
			} catch (Exception e) {
				e.printStackTrace();
				latch.countDown();
			}
		})
		.start();

		// 休眠，让上面的线程先执行
		LangUtils.await(1);
		dblocker.lock(lockId, ()-> {
			try {
//				latch.await();
				t.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
		assertThat(t.getCostTime()).isGreaterThan(TimeUnit.SECONDS.toMillis(timeout));
		assertThat(t.getCostTime()).isLessThan(TimeUnit.SECONDS.toMillis(lockTime));
	}*/
}

