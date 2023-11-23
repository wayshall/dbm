package org.onetwo.common.base;

import org.junit.After;
import org.onetwo.common.base.DbmBaseTest.DbmBaseTestInnerContextConfig;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.dbm.stat.SqlExecutedStatis;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@ActiveProfiles({ "dev" })
//@ContextConfiguration(value="classpath:/applicationContext-test.xml")
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes=DbmBaseTestInnerContextConfig.class)
//@Rollback(false)
public class DbmBaseTestWithouTransactional extends AbstractJUnit4SpringContextTests {
	protected Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SqlExecutedStatis sqlExecutedStatis;
	
	
	@After
	public void afterTest(){
		String info = sqlExecutedStatis.toFormatedString();
		logger.info(info);
	}
}
