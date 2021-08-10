package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.db.spi.SqlTemplateParser;

/***
 * 标记接口为DbmRepository接口，如果接口有实现类（规则为：有接口+Impl的实现类），则使用实现类的实现.
 * 一个DbmRepository接口可以由有实现类的接口和无实现类的接口组成：
public interface CustomUserDao {
	int batchInsert(List<UserTableIdEntity> users);
}

@DbmRepository
public interface UserDao extends CustomUserDao {
	List<UserTableIdEntity> findByUserNameLike(String userName);
}

@Component
public class CustomUserDaoImpl implements CustomUserDao {
	@Autowired
	private BaseEntityManager baseEntityManager;
	@Override
	public int batchInsert(List<UserTableIdEntity> users) {
		Collection<UserTableIdEntity> dbusers = baseEntityManager.saves(users);
		return dbusers.size();
	}
}

 * 优先查找provideManager，如果没有找到，则查找dataSource
 * @author wayshall
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmRepository {
	
	/*****
	 * QueryProvideManager beanName
	 * @return
	 */
	String queryProviderName() default "";
	Class<? extends QueryProvideManager> queryProviderClass() default QueryProvideManager.class;
	String dataSource() default "";
	
	/***
	 * 若指定了dataSource属性，没有找到对应的dataSource Bean时，是否忽略注册此 DbmRepository Bean。
	 * 默认为false，不忽略，抛错
	 * @author weishao zeng
	 * @return
	 */
	boolean ignoreRegisterIfDataSourceNotFound() default false;
	
	Class<? extends SqlTemplateParser> sqlTemplateParser() default SqlTemplateParser.class;
	
	
}
