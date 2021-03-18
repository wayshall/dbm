package org.onetwo.common.dbm.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.onetwo.common.base.DbmSessionCacheTest;
import org.onetwo.common.db.builder.QuerysTest;
import org.onetwo.common.db.filequery.DefaultFileNamedSqlGeneratorTest;
import org.onetwo.common.db.filequery.MultipCommentsSqlFileParserTest;
import org.onetwo.common.db.sqlext.ExtQueryImplTest;
import org.onetwo.common.db.sqlext.JFishExtQueryImplTest;
import org.onetwo.common.dbm.BaseCrudEntityManagerTest;
import org.onetwo.common.dbm.DbmBatchInsertOrUpdateTest;
import org.onetwo.common.dbm.CompositeIDTest;
import org.onetwo.common.dbm.CustomDaoTest;
import org.onetwo.common.dbm.DbmDaoTest;
import org.onetwo.common.dbm.DbmDataFilterTest;
import org.onetwo.common.dbm.DbmDateVersionTest;
import org.onetwo.common.dbm.DbmEntityManagerTest;
import org.onetwo.common.dbm.DbmLongVersionTest;
import org.onetwo.common.dbm.DbmNestedMappingTest;
import org.onetwo.common.dbm.DbmSensitiveFieldTest;
import org.onetwo.common.dbm.DbmSnowflakeTest;
import org.onetwo.common.dbm.DbmSqlScriptTest;
import org.onetwo.common.dbm.EncryptFieldTest;
import org.onetwo.common.dbm.InQueryWithArrayTest;
import org.onetwo.common.dbm.JsonFieldTest;
import org.onetwo.common.dbm.QueryConfigTest;
import org.onetwo.common.dbm.TenentableTest;
import org.onetwo.common.dbm.TransactionalListenerTest;
import org.onetwo.common.dbm.UserDbmIdEntityTest;
import org.onetwo.common.dbm.UserOptionDaoTest;
import org.onetwo.common.dbm.UserTableIdEntityTest;
import org.onetwo.common.dbm.locker.SimpleDBLockerTest;
import org.onetwo.common.dbm.model.service.UserAutoidServiceTest;
import org.onetwo.common.hibernate.UserQueryHibernateDaoTest;
import org.onetwo.common.hibernate.dao.HibernateNestedMappingTest;
import org.onetwo.dbm.mapping.converter.SensitiveFieldValueConverterTest;
import org.onetwo.dbm.utils.SpringAnnotationFinderTest;

@RunWith(Suite.class)
@SuiteClasses({
	SpringAnnotationFinderTest.class,
	SensitiveFieldValueConverterTest.class,
	MultipCommentsSqlFileParserTest.class,
	DefaultFileNamedSqlGeneratorTest.class,
	ExtQueryImplTest.class,
	JFishExtQueryImplTest.class,
	QuerysTest.class,
	DbmDaoTest.class,
	DbmEntityManagerTest.class,
	DbmSensitiveFieldTest.class,
	DbmDataFilterTest.class,
//	DBCheckerTest.class,
//	OneBatchInsertTest.class, // 耗时太长
	DbmBatchInsertOrUpdateTest.class,
	BaseCrudEntityManagerTest.class,
	DbmNestedMappingTest.class,
	TransactionalListenerTest.class,
	DbmSessionCacheTest.class,
	UserTableIdEntityTest.class,
	UserDbmIdEntityTest.class,
	QueryConfigTest.class,
	CustomDaoTest.class,
	JsonFieldTest.class,
	UserOptionDaoTest.class,
	DbmDateVersionTest.class,
	DbmLongVersionTest.class,
	UserAutoidServiceTest.class,
	DbmSnowflakeTest.class,
	CompositeIDTest.class,
	TenentableTest.class,
	InQueryWithArrayTest.class,
	
	HibernateNestedMappingTest.class,
	UserQueryHibernateDaoTest.class,
	EncryptFieldTest.class,
	SimpleDBLockerTest.class,
	DbmSqlScriptTest.class
//	RichModelTest.class
})
public class DbmTestCase {

}
