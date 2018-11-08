package org.onetwo.common.dbm;
/**
 * @author weishao zeng
 * <br/>
 */

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.UserEntity;

public class BaseEntityManagerTest {
	
	@Mock
	BaseEntityManager baseEntityManager;
	
	@Test
	public void test() {
		MockitoAnnotations.initMocks(this);
		
		BaseEntityTestService testService = new BaseEntityTestService();
		testService.baseEntityManager = baseEntityManager;
		
//		Mockito.verify(baseEntityManager);
		Mockito.doNothing().when(baseEntityManager).persist(Mockito.argThat(new ArgumentMatcher<UserEntity>() {
			@Override
			public boolean matches(Object argument) {
				UserEntity u = (UserEntity) argument;
				u.setId(1L);
				return true;
			}
		}));
		
		UserEntity user = new UserEntity();
		Long id = testService.save(user);
		assertThat(id).isEqualTo(1);
		Mockito.verify(baseEntityManager, Mockito.times(1)).persist(Mockito.any());
	}
	
	public static class BaseEntityTestService {
		private BaseEntityManager baseEntityManager;
		
		public Long save(UserEntity user) {
			baseEntityManager.persist(user);
			return user.getId();
		}
	}

}

