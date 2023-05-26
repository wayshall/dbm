package org.onetwo.common.db.filequery;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.onetwo.common.db.dquery.DbmSqlFileResource;
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
		DbmSqlFileResource<?> sqlRes = new DbmSqlFileResource<>(f, null, null);
		
		
		CommonNamespaceProperties np = new CommonNamespaceProperties("org.onetwo.common.jfishdbm.model.dao.UserAutoidDao");
		parser.parseToNamedQueryFile(np, sqlRes);
		Assert.assertEquals(4, np.getNamedProperties().size());
		
		String exceptedString = "insert into test_user_autoid (birthday, email, gender, mobile, nick_name, password, status, user_name) values (:birthday, :email, :gender, :mobile, :nickName, :password?encrypt, :status.value, :userName)\n";
//		System.out.println("sql:\n"+exceptedString);
//		System.out.println("sql:\n"+np.getNamedProperty("batchInsert").getValue());
		Assert.assertEquals(exceptedString, np.getNamedProperty("batchInsert").getValue());
		
		exceptedString = "insert into test_user_autoid (birthday, email, gender, mobile, nick_name, password, status, user_name)\nvalues (:allBirthday, :email, :gender, :mobile, :nickName, :password?encrypt, :status, :userName)\n";
		System.out.println("sql:\n"+exceptedString);
		System.out.println("sql:\n"+np.getNamedProperty("batchInsert2").getValue());
		Assert.assertEquals(exceptedString, np.getNamedProperty("batchInsert2").getValue());
		
		
		exceptedString = "delete from test_user_autoid where 1=1 "
				+ "[#if userName?has_content] and user_name like :userName?likeString [/#if]\n"
				+ "[#if nickName?has_content] and nickName like :nickName?likeString\n[/#if] ";
		
		System.out.println("sql:\n"+exceptedString);
		System.out.println("sql:\n"+np.getNamedProperty("removeByUserName").getValue());
		
		Assert.assertEquals(exceptedString, 
				np.getNamedProperty("removeByUserName").getValue());
		

	}
	
	@Test
	public void testDirectiveWithSpace(){
		String fileName = FileUtils.getResourcePath("sql/org.onetwo.common.dbm.model.dao.UserAutoidDao.jfish.sql");
		List<String> lines = FileUtils.readAsList(fileName);
		System.out.println("line:"+lines);
		MultipCommentsSqlFileParser parser = new MultipCommentsSqlFileParser();
		ResourceAdapter<File> f = FileUtils.adapterResource(new File(fileName));
		DbmSqlFileResource<?> sqlRes = new DbmSqlFileResource<>(f, null, null);

		CommonNamespaceProperties np = new CommonNamespaceProperties("org.onetwo.common.jfishdbm.model.dao.UserAutoidDao");
		parser.parseToNamedQueryFile(np, sqlRes);
		
		String exceptedString = "delete from test_user_autoid where 1=1\n"
				+ "[#if userName?has_content] and user_name like :userName?likeString\n[/#if] "
				+ "[#if nickName?has_content] and nickName like :nickName?likeString\n[/#if] ";
		System.out.println("sql:\n"+exceptedString);
		System.out.println("sql:\n"+np.getNamedProperty("removeByUserNameWithSpace").getValue());
		
		Assert.assertEquals(exceptedString, 
				np.getNamedProperty("removeByUserNameWithSpace").getValue());
	}

}
