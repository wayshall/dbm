package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.builder.Querys;
import org.onetwo.common.dbm.model.dao.UserDao;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserStatus;
import org.onetwo.common.dbm.model.service.NoAutoIdUserService;
import org.onetwo.common.dbm.model.service.UserAutoidServiceImpl;
import org.onetwo.common.profiling.TimeCounter;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

//@TransactionConfiguration(defaultRollback=true)
@Transactional
@Rollback(false)
public class DbmDaoTest extends DbmBaseTest {
	
	@Resource
	private DbmEntityManager jfishEntityManager;
	@Resource
	private UserAutoidServiceImpl userAutoidServiceImpl;
	@Autowired
	private NoAutoIdUserService noAutoIdUserService;
	@Autowired
	UserDao userDao;
	
	private int startId = 1;
	
	@Test
	public void testQueryName() {
		noAutoIdUserService.deleteAll();
		int insertCount = 10;
		this.noAutoIdUserService.insertWithStatus(startId, insertCount, UserStatus.STOP);
		
		int stopUserCount = userDao.countByStatus("countStop", UserStatus.STOP, int.class);
		assertThat(stopUserCount).isEqualTo(insertCount);
		
		Map<String, Object> ctx = CUtils.asMap("testValue", 1);
		ctx = CUtils.asMap(":sqlParameter1", 1);
		stopUserCount = userDao.countBySql("select count(1) from test_user t where  t.status = :status and 1=${testValue}", 
											UserStatus.STOP, 
											int.class, ctx);
		assertThat(stopUserCount).isEqualTo(insertCount);
	}
	


	@Test
	public void testEnums(){
		noAutoIdUserService.deleteAll();
		int insertCount = 10;
		this.noAutoIdUserService.insertWithStatus(startId, insertCount, UserStatus.STOP);
		List<UserEntity> users = Querys.from(jfishEntityManager, UserEntity.class)
										.where()
											.field("status").is(UserStatus.STOP) //默认取enum.name() 见：JdbcParamValueConvers#getActualValue
										.toQuery()
										.list();
		assertThat(users.size()).isEqualTo(insertCount);
		
		List<UserEntity> users2 = userDao.findByUserStatus(UserStatus.STOP);
		assertThat(users2.size()).isEqualTo(insertCount);
		noAutoIdUserService.deleteAll();
		
		startId += insertCount;
	}
	

	@Test
	public void testInsert(){
		noAutoIdUserService.deleteAll();
		int insertCount = 1000;
		TimeCounter t = new TimeCounter("insertByStep");
		t.start();
		this.noAutoIdUserService.insertByStep(startId, insertCount);
		t.stop();
		noAutoIdUserService.deleteAll();
		
		startId += insertCount;
	}
	
	@Test
	public void testBatchInsertList(){
		noAutoIdUserService.deleteAll();
		int insertCount = 1000;
		TimeCounter t = new TimeCounter("testInsertList");
		t.start();
		this.noAutoIdUserService.batchInsertUser(startId, insertCount);	
		t.stop();
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
