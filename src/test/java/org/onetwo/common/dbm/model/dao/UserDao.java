package org.onetwo.common.dbm.model.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EnumType;

import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.dquery.annotation.Param;
import org.onetwo.common.db.dquery.annotation.Query;
import org.onetwo.common.db.dquery.annotation.QueryName;
import org.onetwo.common.db.dquery.annotation.QueryParseContext;
import org.onetwo.common.db.dquery.annotation.QueryResultType;
import org.onetwo.common.db.dquery.annotation.QuerySqlTemplateParser;
import org.onetwo.common.db.dquery.annotation.Sql;
import org.onetwo.common.db.filequery.ParserContext;
import org.onetwo.common.db.spi.SqlTemplateParser;
import org.onetwo.common.dbm.model.entity.UserTableIdEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserStatus;
import org.springframework.transaction.annotation.Transactional;

@DbmRepository
public interface UserDao extends CustomUserDao {
	
	List<UserTableIdEntity> findByUserNameLike(String userName);

	@Query(value="insert  into test_user " +
        " (id, birthday, email, gender, user_name) " +
        " values (:id, :birthday, :email, :gender.enumMappingValue, :userName)"
	)
	int batchInsertUsers(List<UserEntity> users);
	
	@Query(value="select * from test_user t where t.id in ( :userIds )")
	ArrayList<UserEntity> findUserWithIds(Long[] userIds);
	
	@QuerySqlTemplateParser(SimpleSqlTemplateParser.class)
	<T> T countByStatus(@QueryName String name, UserStatus status, @QueryResultType Class<T> resultType);
	<T> T countBySql(@Sql String sql, UserStatus status, @QueryResultType Class<T> resultType, @QueryParseContext Map<String, Object> ctx);
	
	/***
	 * Param注解可指定枚举取值方式，详见：DynamicMethod#convertQueryValue
	 * 
	 * @author weishao zeng
	 * @param status
	 * @return
	 */
	@Transactional
	List<UserEntity> findByUserStatus(@Param(value="status", enumType=EnumType.STRING) UserStatus status);
		
	public class SimpleSqlTemplateParser implements SqlTemplateParser {

		@Override
		public String parseSql(String name, ParserContext context) {
			if ("countStop".equals(name)) {
				return "select count(1) from test_user t where  t.status = :status";
			}
			return name;
		}
		
	}
	
}
