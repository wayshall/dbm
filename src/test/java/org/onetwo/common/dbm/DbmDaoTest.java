package org.onetwo.common.dbm;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.dbm.model.service.NoAutoIdUserService;
import org.onetwo.common.dbm.model.service.UserAutoidServiceImpl;
import org.onetwo.common.profiling.TimeCounter;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

//@TransactionConfiguration(defaultRollback=true)
@Transactional
public class DbmDaoTest extends DbmBaseTest {
	
	@Resource
	private DbmEntityManager jfishEntityManager;
	@Resource
	private UserAutoidServiceImpl userAutoidServiceImpl;
	@Autowired
	private NoAutoIdUserService noAutoIdUserService;
	
	private int startId = 1;
	

	@Test
	public void testInsert(){
		int insertCount = 1000;
		TimeCounter t = new TimeCounter("insertByStep");
		t.start();
		this.noAutoIdUserService.insertByStep(startId, insertCount);
		t.stop();
		noAutoIdUserService.deleteAll();
		
		startId += insertCount;
	}
	
	@Test
	public void testInsertList(){
		int insertCount = 1000;
		TimeCounter t = new TimeCounter("testInsertList");
		t.start();
		this.noAutoIdUserService.insertList(startId, insertCount);	
		t.stop();
		
		noAutoIdUserService.deleteAll();
	}
	
//	@Test
	//deadlock..........
	public void testMultipThread() throws Exception{
		int taskCount = 2;
		CyclicBarrier barrier = new CyclicBarrier(taskCount);
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(taskCount);
		Future<?> res1 = executor.submit(()->{
			try {
				System.out.println("await.....");
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			testInsert();
		});
		LangUtils.await(2);
		Future<?> res2 = executor.submit(()->{
			try {
				System.out.println("await.....");
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			testInsert();
		});

		res1.get();
		res2.get();
		System.out.println("done");
		
	}

}
