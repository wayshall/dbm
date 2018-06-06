package org.onetwo.common.db.filequery;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.onetwo.common.db.filequery.BaseNamedSqlFileManager.CommonNamespaceProperties;
import org.onetwo.common.file.FileUtils;
import org.onetwo.common.propconf.ResourceAdapter;

public class MultipCommentsSqlFileParserTest {
	
	@Test
	public void testBase(){
		String fileName = FileUtils.getResourcePath("sql/org.onetwo.common.dbm.model.dao.UserAutoidDao.jfish.sql");
		List<String> lines = FileUtils.readAsList(fileName);
		System.out.println("line:"+lines);
		MultipCommentsSqlFileParser parser = new MultipCommentsSqlFileParser();
		ResourceAdapter<File> f = FileUtils.adapterResource(new File(fileName));
		
		
		CommonNamespaceProperties np = new CommonNamespaceProperties("org.onetwo.common.jfishdbm.model.dao.UserAutoidDao");
		parser.parseToNamedQueryFile(np, f);
		Assert.assertEquals(4, np.getNamedProperties().size());
		Assert.assertEquals("insert into test_user_autoid (birthday, email, gender, mobile, nick_name, password, status, user_name) values (:birthday, :email, :gender, :mobile, :nickName, :password, :status.value, :userName) ", np.getNamedProperty("batchInsert").getValue());
		Assert.assertEquals("insert into test_user_autoid (birthday, email, gender, mobile, nick_name, password, status, user_name) values (:allBirthday, :email, :gender, :mobile, :nickName, :password, :status, :userName) ", np.getNamedProperty("batchInsert2").getValue());
		
		
		String exceptedString = "delete from test_user_autoid where 1=1 "
				+ "[#if userName?has_content] and user_name like :userName?likeString [/#if] "
				+ "[#if nickName?has_content] and nickName like :nickName?likeString [/#if] ";
		
//		System.out.println("sql:\n"+exceptedString);
//		System.out.println("sql:\n"+np.getNamedProperty("removeByUserName").getValue());
		
		Assert.assertEquals(exceptedString, 
				np.getNamedProperty("removeByUserName").getValue());
		

		exceptedString = "delete from test_user_autoid where 1=1 "
				+ "[#if userName?has_content] and user_name like :userName?likeString [/#if] "
				+ "[#if nickName?has_content] and nickName like :nickName?likeString [/#if] ";
		System.out.println("sql:\n"+exceptedString);
		System.out.println("sql:\n"+np.getNamedProperty("removeByUserNameWithSpace").getValue());
		
		Assert.assertEquals(exceptedString, 
				np.getNamedProperty("removeByUserNameWithSpace").getValue());
	}

}
