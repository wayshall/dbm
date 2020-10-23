package org.onetwo.common.db.filequery;
/**
 * @author weishao zeng
 * <br/>
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.onetwo.common.db.ParsedSqlContext;
import org.onetwo.common.db.spi.FileNamedSqlGenerator;
import org.onetwo.common.db.spi.NamedQueryFile;
import org.onetwo.common.db.spi.NamedQueryInfoParser;
import org.onetwo.common.dbm.model.entity.UserVersionEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserStatus;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.spring.utils.SpringResourceAdapterImpl;

import com.google.common.collect.Maps;

public class DefaultFileNamedSqlGeneratorTest {
	
//	DbmNamedFileQueryFactory dbmNamedFileQueryFactory;
	DbmNamedSqlFileManager sqlFileManager;
	
	@Before
	public void setup() {
		MultipCommentsSqlFileParser parser1 = new MultipCommentsSqlFileParser();
		AnnotationBasedQueryInfoParser parser2 = new AnnotationBasedQueryInfoParser();
		List<NamedQueryInfoParser> queryInfoParsers = Arrays.asList(parser1, parser2);
		
		sqlFileManager = DbmNamedSqlFileManager.createNamedSqlFileManager(false); 
		sqlFileManager.setQueryInfoParsers(queryInfoParsers);
		
//		this.dbmNamedFileQueryFactory = new DbmNamedFileQueryFactory(sqlFileManager);
	}
	
	@Test
	public void testFindUsers() {
		String file = "sql/org.onetwo.common.dbm.model.dao.SqlFileParser.jfish.sql";
		SpringResourceAdapterImpl sqlFile = new SpringResourceAdapterImpl(SpringUtils.classpath(file));
		NamedQueryFile namedQueryFile = sqlFileManager.buildSqlFile(sqlFile);
		
		UserVersionEntity user = new UserVersionEntity();
		Map<Object, Object> params = Maps.newHashMap();
		params.put("query", user);
		
		ParserContext parserContext = ParserContext.create(namedQueryFile.getNamedProperty("findUsers"));
		FileNamedSqlGenerator sqlGen = new DefaultFileNamedSqlGenerator(false, 
													sqlFileManager.getSqlStatmentParser(), 
													parserContext, 
													params, 
													Optional.empty());
		ParsedSqlContext sqlAndValues = sqlGen.generatSql();
		System.out.println("sql: " + sqlAndValues.getParsedSql());
		System.out.println("values: " + sqlAndValues.asMap());
		String sql = "select * from TEST_USER u\n"
				+ " ";
		assertThat(sqlAndValues.getParsedSql()).isEqualTo(sql);
		
		/*user = new UserVersionEntity();
		user.setAge(11);
		params = Maps.newHashMap();
		params.put("query", user);
		parserContext = ParserContext.create(namedQueryFile.getNamedProperty("findUsers"));
		sqlGen = new DefaultFileNamedSqlGenerator(false, 
													sqlFileManager.getSqlStatmentParser(), 
													parserContext, 
													params, 
													Optional.empty());
		sqlAndValues = sqlGen.generatSql();
		System.out.println("sql: " + sqlAndValues.getParsedSql());
		System.out.println("values: " + sqlAndValues.asMap());
		sql = "select * from TEST_USER u \n";
		assertThat(sqlAndValues.getParsedSql()).isEqualTo(sql);*/
	}

	@Test
	public void testFindUsers2() {
		String file = "sql/org.onetwo.common.dbm.model.dao.SqlFileParser.jfish.sql";
		SpringResourceAdapterImpl sqlFile = new SpringResourceAdapterImpl(SpringUtils.classpath(file));
		NamedQueryFile namedQueryFile = sqlFileManager.buildSqlFile(sqlFile);
		
		UserVersionEntity user = new UserVersionEntity();
		user.setAge(11);
		Map<Object, Object> params = Maps.newHashMap();
		params.put("query", user);
		
		ParserContext parserContext = ParserContext.create(namedQueryFile.getNamedProperty("findUsers"));
		FileNamedSqlGenerator sqlGen = new DefaultFileNamedSqlGenerator(false, 
													sqlFileManager.getSqlStatmentParser(), 
													parserContext, 
													params, 
													Optional.empty());
		ParsedSqlContext sqlAndValues = sqlGen.generatSql();
		System.out.println("sql: " + sqlAndValues.getParsedSql());
		System.out.println("values: " + sqlAndValues.asMap());
		String sql = "select * from TEST_USER u\n"
				+ "where  u.age = :query.age ";
		assertThat(sqlAndValues.getParsedSql()).isEqualTo(sql);
		
		
		user = new UserVersionEntity();
		user.setUserName("testUserName");
		user.setStatus(UserStatus.NORMAL);
		params = Maps.newHashMap();
		params.put("query", user);
		parserContext = ParserContext.create(namedQueryFile.getNamedProperty("findUsers"));
		sqlGen = new DefaultFileNamedSqlGenerator(false, 
													sqlFileManager.getSqlStatmentParser(), 
													parserContext, 
													params, 
													Optional.empty());
		sqlAndValues = sqlGen.generatSql();
		System.out.println("sql: " + sqlAndValues.getParsedSql());
		System.out.println("values: " + sqlAndValues.asMap());
		sql = "select * from TEST_USER u\n"
				+ "where u.user_name = :query.userName    and u.status = :query.status  ";
		assertThat(sqlAndValues.getParsedSql()).isEqualTo(sql);
	}


	@Test
	public void testFindUsersWithWhere() {
		String file = "sql/org.onetwo.common.dbm.model.dao.SqlFileParser.jfish.sql";
		SpringResourceAdapterImpl sqlFile = new SpringResourceAdapterImpl(SpringUtils.classpath(file));
		NamedQueryFile namedQueryFile = sqlFileManager.buildSqlFile(sqlFile);
		
		UserVersionEntity user = new UserVersionEntity();
		user.setAge(11);
		Map<Object, Object> params = Maps.newHashMap();
		params.put("query", user);
		
		ParserContext parserContext = ParserContext.create(namedQueryFile.getNamedProperty("findUsersWithWhere"));
		FileNamedSqlGenerator sqlGen = new DefaultFileNamedSqlGenerator(false, 
													sqlFileManager.getSqlStatmentParser(), 
													parserContext, 
													params, 
													Optional.empty());
		ParsedSqlContext sqlAndValues = sqlGen.generatSql();
		System.out.println("sql: " + sqlAndValues.getParsedSql());
		System.out.println("values: " + sqlAndValues.asMap());
		String sql = "select * from TEST_USER u where  u.age = :query.age\n";
		assertThat(sqlAndValues.getParsedSql()).isEqualTo(sql);
		
		
		user = new UserVersionEntity();
		user.setUserName("testUserName");
		user.setStatus(UserStatus.NORMAL);
		params = Maps.newHashMap();
		params.put("query", user);
		parserContext = ParserContext.create(namedQueryFile.getNamedProperty("findUsersWithWhere"));
		sqlGen = new DefaultFileNamedSqlGenerator(false, 
													sqlFileManager.getSqlStatmentParser(), 
													parserContext, 
													params, 
													Optional.empty());
		sqlAndValues = sqlGen.generatSql();
		System.out.println("sql: " + sqlAndValues.getParsedSql());
		System.out.println("values: " + sqlAndValues.asMap());
		sql = "select * from TEST_USER u where u.user_name = :query.userName    and u.status = :query.status \n";
		assertThat(sqlAndValues.getParsedSql()).isEqualTo(sql);
	}
	
	@Test
	public void testUpdateUser() {
		String file = "sql/org.onetwo.common.dbm.model.dao.SqlFileParser.jfish.sql";
		SpringResourceAdapterImpl sqlFile = new SpringResourceAdapterImpl(SpringUtils.classpath(file));
		NamedQueryFile namedQueryFile = sqlFileManager.buildSqlFile(sqlFile);
		
		UserVersionEntity user = new UserVersionEntity();
		user.setId(1L);
		user.setAge(11);
		Map<Object, Object> params = Maps.newHashMap();
		params.put("query", user);
		
		ParserContext parserContext = ParserContext.create(namedQueryFile.getNamedProperty("updateUsers"));
		FileNamedSqlGenerator sqlGen = new DefaultFileNamedSqlGenerator(false, 
													sqlFileManager.getSqlStatmentParser(), 
													parserContext, 
													params, 
													Optional.empty());
		ParsedSqlContext sqlAndValues = sqlGen.generatSql();
		System.out.println("sql: " + sqlAndValues.getParsedSql());
		System.out.println("values: " + sqlAndValues.asMap());
		String sql = "update TEST_USER\n"
				+ "set age = :query.age where id = :query.id\n";
		assertThat(sqlAndValues.getParsedSql()).isEqualTo(sql);
		
		
		user = new UserVersionEntity();
		user.setId(1L);
		user.setUserName("testUserName");
		user.setStatus(UserStatus.NORMAL);
		params = Maps.newHashMap();
		params.put("query", user);
		parserContext = ParserContext.create(namedQueryFile.getNamedProperty("updateUsers"));
		sqlGen = new DefaultFileNamedSqlGenerator(false, 
													sqlFileManager.getSqlStatmentParser(), 
													parserContext, 
													params, 
													Optional.empty());
		sqlAndValues = sqlGen.generatSql();
		System.out.println("sql: " + sqlAndValues.getParsedSql());
		System.out.println("values: " + sqlAndValues.asMap());
		sql = "update TEST_USER\n"
				+ "set user_name = :query.userName,    status = :query.status where id = :query.id\n";
		assertThat(sqlAndValues.getParsedSql()).isEqualTo(sql);
	}

	@Test
	public void testUpdateUserWithSet() {
		String file = "sql/org.onetwo.common.dbm.model.dao.SqlFileParser.jfish.sql";
		SpringResourceAdapterImpl sqlFile = new SpringResourceAdapterImpl(SpringUtils.classpath(file));
		NamedQueryFile namedQueryFile = sqlFileManager.buildSqlFile(sqlFile);
		
		UserVersionEntity user = new UserVersionEntity();
		user.setId(1L);
		user.setAge(11);
		Map<Object, Object> params = Maps.newHashMap();
		params.put("query", user);
		
		ParserContext parserContext = ParserContext.create(namedQueryFile.getNamedProperty("updateUsersWithSet"));
		FileNamedSqlGenerator sqlGen = new DefaultFileNamedSqlGenerator(false, 
													sqlFileManager.getSqlStatmentParser(), 
													parserContext, 
													params, 
													Optional.empty());
		ParsedSqlContext sqlAndValues = sqlGen.generatSql();
		System.out.println("sql: " + sqlAndValues.getParsedSql());
		System.out.println("values: " + sqlAndValues.asMap());
		String sql = "update TEST_USER\n"
				+ "set age = :query.age where id = :query.id\n";
		assertThat(sqlAndValues.getParsedSql()).isEqualTo(sql);
		
		
		user = new UserVersionEntity();
		user.setId(1L);
		user.setUserName("testUserName");
		user.setStatus(UserStatus.NORMAL);
		params = Maps.newHashMap();
		params.put("query", user);
		parserContext = ParserContext.create(namedQueryFile.getNamedProperty("updateUsersWithSet"));
		sqlGen = new DefaultFileNamedSqlGenerator(false, 
													sqlFileManager.getSqlStatmentParser(), 
													parserContext, 
													params, 
													Optional.empty());
		sqlAndValues = sqlGen.generatSql();
		System.out.println("sql: " + sqlAndValues.getParsedSql());
		System.out.println("values: " + sqlAndValues.asMap());
		sql = "update TEST_USER\n"
				+ "set user_name = :query.userName,    status = :query.status where id = :query.id\n";
		assertThat(sqlAndValues.getParsedSql()).isEqualTo(sql);
	}
}

