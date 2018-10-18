package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.date.DateUtils;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.dbm.model.entity.UserWithDataFilterEntity;
import org.onetwo.common.dbm.model.entity.UserWithDataFilterEntity.AgeIDataQueryParamterEnhancer;
import org.onetwo.common.utils.LangOps;
import org.onetwo.common.utils.LangUtils;

public class DbmDataFilterTest extends DbmBaseTest {

	@Resource
	private BaseEntityManager entityManager;
	
	private final int fixAge = AgeIDataQueryParamterEnhancer.FIXED_AGE;

	@Test
	public void testQuery(){
		entityManager.removeAll(UserWithDataFilterEntity.class);
		List<UserWithDataFilterEntity> users = LangOps.generateList(20, i->{
			UserWithDataFilterEntity user = new UserWithDataFilterEntity();
			user.setId(i+1L);
			user.setUserName("JdbcTest");
			user.setBirthday(DateUtils.now());
			user.setEmail("username@qq.com");
			user.setHeight(3.3f);
			if(i%2 == 0) {
				user.setAge(fixAge);
			} else {
				user.setAge(fixAge+5);
			}
			return user;
		});
		entityManager.save(users);

		List<UserWithDataFilterEntity> dbuserList = entityManager.findList(UserWithDataFilterEntity.class, K.DATA_FILTER, false);
		assertThat(dbuserList.size()).isEqualTo(20);
		
		dbuserList = entityManager.findList(UserWithDataFilterEntity.class);
		assertThat(dbuserList.size()).isEqualTo(10);
		
		
		for(UserWithDataFilterEntity u : dbuserList){
			LangUtils.println("id: ${0}, name: ${1}", u.getId(), u.getUserName());
		}
		
		entityManager.removeAll(UserWithDataFilterEntity.class);
	}
	

}
