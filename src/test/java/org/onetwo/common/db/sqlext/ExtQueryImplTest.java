package org.onetwo.common.db.sqlext;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onetwo.common.date.DateUtils;
import org.onetwo.common.db.EntityManagerProvider;
import org.onetwo.common.db.Magazine;
import org.onetwo.common.db.Magazine.EntityWithDataFilter;
import org.onetwo.common.db.Magazine.MagazineWithTable;
import org.onetwo.common.db.filter.annotation.DataQueryFilterListener;
import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.db.sqlext.ExtQuery.K.IfNull;
import org.onetwo.common.db.sqlext.ExtQuery.KeyObject;
import org.onetwo.common.db.sqlext.ExtQueryUtils.F;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.utils.CUtils;

import junit.framework.TestCase;


@SuppressWarnings({"unchecked", "rawtypes"})
public class ExtQueryImplTest {

	Map<Object, Object> properties;
	
	private SQLSymbolManagerFactory sqlSymbolManagerFactory;

	@Test
	public void testEquals() {
		assertThat(KeyObject.builder().key(":sql-join").build()).isEqualTo(KeyObject.builder().key(":sql-join").build());
		assertThat(KeyObject.builder().key(":sql-join").build()).isNotEqualTo(KeyObject.builder().key(":select").build());
	}
	
	@Before
	public void setup(){
		this.properties = new LinkedHashMap<Object, Object>();
		this.properties.put(K.DESC, "id");
		sqlSymbolManagerFactory = SQLSymbolManagerFactory.getInstance();
//		sqlSymbolManagerFactory.register(EntityManagerProvider.JPA, JPASQLSymbolManager.create());
//		sqlSymbolManagerFactory.register(EntityManagerProvider.Hibernate, HibernateSQLSymbolManagerImpl.create());
		JPASQLSymbolManagerImpl jpa = new JPASQLSymbolManagerImpl();
		jpa.setListeners(Arrays.asList(new DataQueryFilterListener()));
		sqlSymbolManagerFactory.register(EntityManagerProvider.JPA, jpa);
	}
	
	

	@Test
	public void testFunctionRef(){
		Map<Object, Object> properties = new LinkedHashMap<Object, Object>();

		properties.put("&LOWER(name)", "way");
		properties.put("&substring(name, 5, 1)", "w");
		properties.put(K.DEBUG, true);

		ExtQueryInner q = SQLSymbolManagerFactory.getInstance().getJPA().createSelectQuery(Object.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Object mag where lower(mag.name) = :lower_mag_name_0 and substring(mag.name, 5, 1) = :substring_mag_name_5_1_1";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals("{lower_mag_name_0=way, substring_mag_name_5_1_1=w}", q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testFindAll(){

		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", null);
		q.build();
		
		String sql = "select mag from Magazine mag";
		String paramsting = "{}";
//		System.out.println("testFindAll: " + q.getSql().trim());
//		System.out.println("testFindAll: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	

	@Test
	public void testNull(){
		this.properties.put("name:is null", true);
		this.properties.put("nickname:is null", false);
		this.properties.put(K.DEBUG, true);
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag where mag.name is null and mag.nickname is not null order by mag.id desc";
		String paramsting = "{}";
//		System.out.println("testNull: " + q.getSql().trim());
//		System.out.println("testNull: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testNull2(){

		this.properties.put("name:=", SQLKeys.Null);
		this.properties.put("nickname:!=", SQLKeys.Null);
		this.properties.put(K.DEBUG, true);
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag where mag.name is null and mag.nickname is not null order by mag.id desc";
		String paramsting = "{}";
//		System.out.println("testNull2: " + q.getSql().trim());
//		System.out.println("testNull2: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testLike(){

		this.properties.put("name:like", "way%");
		this.properties.put(K.DEBUG, true);
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag where mag.name like :mag_name0 order by mag.id desc";
		String paramsting = "{mag_name0=way%}";
//		System.out.println("testNull2: " + q.getSql().trim());
//		System.out.println("testNull2: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testLikeWithoutPersent(){

		this.properties.put("name:like", "way");
		this.properties.put(K.DEBUG, true);
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag where mag.name like :mag_name0 order by mag.id desc";
		String paramsting = "{mag_name0=%way%}";
//		System.out.println("testNull2: " + q.getSql().trim());
//		System.out.println("testNull2: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testLike2(){

		this.properties.put("name:=~", "way%");
		this.properties.put(K.DEBUG, true);
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag where mag.name like :mag_name0 order by mag.id desc";
		String paramsting = "{mag_name0=way%}";
//		System.out.println("testNull2: " + q.getSql().trim());
//		System.out.println("testNull2: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());

		this.properties.clear();
		this.properties.put("name:!=~", "way%");
		this.properties.put(K.DEBUG, true);
		q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		sql = "select mag from Magazine mag where mag.name not like :mag_name0";
		paramsting = "{mag_name0=way%}";
		System.out.println("testLike2: " + q.getSql().trim());
		System.out.println("testLike2: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	/*@Test
	public void testEmpty(){
		
		this.properties.put("pages:is empty", true);
		this.properties.put("types:is empty", false);
		this.properties.put(K.DEBUG, true);
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag where mag.pages is empty and mag.types is not empty order by mag.id desc";
		String paramsting = "{}";
//		System.out.println("testEmpty: " + q.getSql().trim());
//		System.out.println("testEmpty: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}*/
	
	@Test
	public void testCommon(){
		Map params = new LinkedHashMap();
		params.put(new String[]{"id", "name"}, new Object[]{11, 2l});
		params.put(K.MAX_RESULTS, 222333);
		params.put("column.id:in", new Object[]{222, 111});
		params.put("userName", null);
		params.put("id:!=", new Object[] { Long.class, "id", "aa", 1, "bb", "cc" });
		params.put(K.FETCH, "bid");
		params.put(K.DESC, "name, text");
		params.put(K.ORDERBY, "object.nameid desc, object.textid asc");
		params.put(K.IF_NULL, IfNull.Ignore);
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, params);
		q.build();
		
//		System.out.println("testCommon:" + q.getSql().trim());
//		System.out.println("testCommon:" + q.getParamsValue().getValues().toString());
		
		String sql = "select object from Object object left join fetch bid where ( object.id = :object_id0 or object.name = :object_name1 ) and object.column.id in ( :object_column_id2, :object_column_id3) and object.id != ( select sub_long.id from Long sub_long where sub_long.aa = :sub_long_aa4 and sub_long.bb = :sub_long_bb5 ) order by object.name desc, object.text desc, object.nameid desc, object.textid asc";
		String paramsting = "{object_id0=11, object_name1=2, object_column_id2=222, object_column_id3=111, sub_long_aa4=1, sub_long_bb5=cc}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testCommonIfNullCalm(){
		properties.put(new String[]{"id", "name"}, new Object[]{11, 2l});
		properties.put("column.id:in", new Object[]{222, 111});
		properties.put("userName", "");
		properties.put(K.FETCH, "bid");
		properties.put(K.IF_NULL, IfNull.Calm);
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();

//		System.out.println("testCommonIfNullCalm:" + q.getSql().trim());
//		System.out.println("testCommonIfNullCalm:" + q.getParamsValue().getValues().toString());
		String sql = "select object from Object object left join fetch bid where ( object.id = :object_id0 or object.name = :object_name1 ) and object.column.id in ( :object_column_id2, :object_column_id3) and object.userName = :object_userName4 order by object.id desc";
		String paramsting = "{object_id0=11, object_name1=2, object_column_id2=222, object_column_id3=111, object_userName4=}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}

	@Test
	public void testBetweenDate(){
		properties.put("startTime:>=", new Date());
		properties.put("startTime:<=", new Date());
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
//		System.out.println("date: " + q.getSql());
		String sql = "select object from Object object where object.startTime >= :object_startTime0 and object.startTime <= :object_startTime1 order by object.id desc ";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
	}

	@Test
	public void testOrderBy(){
		properties = new LinkedHashMap<Object, Object>();
		properties.put(K.ASC, "sort");
		properties.put(K.DESC, "id");
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
		System.out.println("testOrderBy: " + q.getSql());
		String sql = "select object from Object object order by object.sort asc, object.id desc";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
	}
	

	@Test
	public void testSelectMap(){
		//or 示例用法
		properties.put(K.SELECT, new Object[]{HashMap.class, "aa", "bb"});
		properties.put("aa", "bb");
		
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
		String sql = "select new map(object.aa, object.bb) from Object object where object.aa = :object_aa0 order by object.id desc ";
		String paramsting = "{object_aa0=bb}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
		
		properties.clear();
		properties.put(K.SELECT, new Object[]{List.class, "aa", "bb"});
		properties.put("aa", "bb");
		
		q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
		sql = "select new list(object.aa, object.bb) from Object object where object.aa = :object_aa0";
		paramsting = "{object_aa0=bb}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
		

		
		properties.clear();
		properties.put(K.SELECT, new Object[]{UserEntity.class, "aa", "bb"});
		properties.put("aa", "bb");
		
		q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
		sql = "select new org.onetwo.common.dbm.model.hib.entity.UserEntity(object.aa, object.bb) from Object object where object.aa = :object_aa0";
		paramsting = "{object_aa0=bb}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
		
	}
	


	@Test
	public void testSelectAlias(){
		//or 示例用法
		properties.put(K.SELECT, new Object[]{"aa:a1", "bb:b1"});
		properties.put("aa", "bb");
		
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
		String sql = "select object.aa as a1, object.bb as b1 from Object object where object.aa = :object_aa0 order by object.id desc ";
		String paramsting = "{object_aa0=bb}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
		
	}


	@Test
	public void testDistinctSelect(){
		//or 示例用法
		properties.put(K.SELECT, new String[]{"aa", "bb"});
		properties.put("aa", "bb");
		
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
		String sql = "select object.aa, object.bb from Object object where object.aa = :object_aa0 order by object.id desc ";
		String paramsting = "{object_aa0=bb}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
		

		properties.remove(K.SELECT);
		properties.put(K.DISTINCT, new String[]{"aa", "bb"});
		properties.put("aa", "bb");
		ExtQueryInner q2 = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q2.build();
		String sql2 = "select distinct object.aa, object.bb from Object object where object.aa = :object_aa0 order by object.id desc ";
		Assert.assertEquals(sql2.trim(), q2.getSql().trim());
		Assert.assertEquals(paramsting, q2.getParamsValue().getValues().toString());
		
	}

	@Test
	public void testDistinctSelect2(){
		//or 示例用法
		properties.put(K.SELECT, new String[]{"cc", "aa", "bb"});
		properties.put("aa", "bb");
		properties.put(K.DISTINCT, null);
		
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
		String sql = "select distinct object.cc, object.aa, object.bb from Object object where object.aa = :object_aa0 order by object.id desc ";
		String paramsting = "{object_aa0=bb}";
		System.out.println("testDistinctSelect2:"+q.getSql());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
		
		
	}
	
	@Test
	public void testDistinctCount(){
		properties.remove(K.SELECT);
//		properties.put(K.DISTINCT, "object");
		properties.put(K.COUNT, "object.id");
		properties.put("aa", "bb");
		ExtQueryInner q2 = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q2.build();
		String paramsting = "{object_aa0=bb}";
		String sql2 = "select count(object.id) from Object object where object.aa = :object_aa0 order by object.id desc";
//		System.out.println("testDistinctCount: "+q2.getSql());
		System.out.println("testDistinctCount getSql: "+q2.getSql());
//		System.out.println("testDistinctCount paramsting: "+paramsting);
		Assert.assertEquals(sql2.trim(), q2.getSql().trim());
		Assert.assertEquals(paramsting, q2.getParamsValue().getValues().toString());
		
	}

	@Test
	public void testFetch(){
		properties.put(K.FETCH, "cc");
		properties.put("aa", "bb");
		properties.put(".cc.cc2", 22);
		
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
		String sql = "select object from Object object left join fetch cc where object.aa = :object_aa0 and cc.cc2 = :cc_cc21 order by object.id desc";
		String paramsting = "{object_aa0=bb, cc_cc21=22}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}

	@Test
	public void testJoinFetch(){
		properties.put(K.JOIN_FETCH, new String[]{"author:auth", "pages:page"});
		properties.put("aa", "bb");
		properties.put("cc", 22);
		
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, properties);
		q.build();
		
		String sql = "select magazine from Magazine magazine join fetch author auth join fetch pages page where magazine.aa = :magazine_aa0 and magazine.cc = :magazine_cc1 order by magazine.id desc";
		String paramsting = "{magazine_aa0=bb, magazine_cc1=22}";
		
//		System.out.println("testJoinFetch: " + q.getSql().trim());
//		System.out.println("testJoinFetch: " + q.getParamsValue().getValues().toString());
		
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testJoin(){
		properties.put(K.DISTINCT, null);
		properties.put(K.JOIN, new String[]{"articles:art", "art.author:auth"});
		properties.put("auth.lastName", "Grisham");

		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select distinct mag from Magazine mag join articles art join art.author auth where auth.lastName = :auth_lastName0 order by mag.id desc";
		String paramsting = "{auth_lastName0=Grisham}";
		
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testLeftJoin(){
		properties.put(K.DISTINCT, null);
		properties.put(K.JOIN, new String[]{"user:u", "role:r"});
		properties.put(K.LEFT_JOIN, new String[]{"articles:art", "art.author:auth"});
		properties.put("auth.lastName", "Grisham");

		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select distinct mag from Magazine mag join user u join role r left join articles art left join art.author auth where auth.lastName = :auth_lastName0 order by mag.id desc";
//		String sql = "select distinct mag from Magazine mag left join articles art left join art.author auth join user u join role r where auth.lastName = :auth_lastName0 order by mag.id desc";
		String paramsting = "{auth_lastName0=Grisham}";

		System.out.println("testLeftJoin: " + q.getSql().trim());
//		System.out.println("testLeftJoin: " + q.getParamsValue().getValues().toString());
		
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testInJoin(){
		properties.put(K.JOIN_IN, "articles:art");
		properties.put(K.NO_PREFIX+"art.lastName", "Grisham");

		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag , in(articles) art where art.lastName = :art_lastName0 order by mag.id desc";
		String paramsting = "{art_lastName0=Grisham}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}

	@Test
	public void testSqlFuncFail(){
		properties.put(K.JOIN_IN, "articles:art");
		properties.put(K.NO_PREFIX+"art.lastName", "Grisham");
		properties.put(".LOWER(name)", "way");

		try {
			ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
			q.build();
			Assert.fail("it must thorw : [ERROR]:the field is inValid : lower(name)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Test
	public void testSqlFunc(){
//		properties.put(K.SQL_QUERY, true);
		properties.put(F.sqlFunc("LOWER(name)"), "way");
		properties.put(F.sqlFunc("substring(name, 5, 1)"), "w");
		properties.put(K.IF_NULL, IfNull.Ignore);

		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag where LOWER(name) = :LOWER_name_0 and substring(name, 5, 1) = :substring_name_5_1_1 order by mag.id desc";
		String paramsting = "{LOWER_name_0=way, substring_name_5_1_1=w}";
		
		System.out.println("testFunc sql: " + q.getSql().trim());
		System.out.println("testFunc values: " + q.getParamsValue().getValues());
		
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testFunc(){
//		properties.put(K.SELECT, ".max(@name, @age)");
		properties.put(K.JOIN_IN, "articles:art");
		properties.put(K.NO_PREFIX+"art.lastName", "Grisham");
		properties.put("&LOWER(@name)", "way");
		properties.put("&substring(@name, 5, 1)", "w");

		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag , in(articles) art where art.lastName = :art_lastName0 and lower(mag.name) = :lower_mag_name_1 and substring(mag.name, 5, 1) = :substring_mag_name_5_1_2 order by mag.id desc ";
		String paramsting = "{art_lastName0=Grisham, lower_mag_name_1=way, substring_mag_name_5_1_2=w}";
		
//		System.out.println("testFunc sql: " + q.getSql().trim());
//		System.out.println("testFunc values: " + q.getParamsValue().getValues());
		
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testFunc2(){
		properties.put(K.JOIN_IN, "articles:art");
		properties.put(K.NO_PREFIX+"art.lastName", "Grisham");
		properties.put("&LOWER(@name)", "way");
		properties.put(("&substring(@name, 5, 1)"), "w");

		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag , in(articles) art where art.lastName = :art_lastName0 and lower(mag.name) = :lower_mag_name_1 and substring(mag.name, 5, 1) = :substring_mag_name_5_1_2 order by mag.id desc";
		String paramsting = "{art_lastName0=Grisham, lower_mag_name_1=way, substring_mag_name_5_1_2=w}";
//		System.out.println("testFunc2 sql: " + q.getSql().trim());
//		System.out.println("testFunc2 values: " + q.getParamsValue().getValues());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	
	@Test
	public void testDateIn(){

//		System.out.println("\ntestDateIn start ==================>>>>>>>>>");
		
//		Map<Object, Object> properties = new LinkedHashMap<Object, Object>();

		properties.put("createTime:date in", new String[]{":this-year", ":this-year :end"});
		
		properties.put("createTime:date in", new String[]{":this-year", ":this-year.end"});
		
		properties.put("createTime:date in", "2011-10-27");
		

		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();

		String sql = "select mag from Magazine mag where ( mag.createTime >= :mag_createTime0 and mag.createTime < :mag_createTime1 ) order by mag.id desc";
		String paramsting = "{mag_createTime0=Thu Oct 27 00:00:00 CST 2011, mag_createTime1=Fri Oct 28 00:00:00 CST 2011}";
//		System.out.println("aa:"+q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
		

		properties.clear();
		properties.put(K.DESC, "id");
		
		properties.put("createTime:date in", new String[]{"2011-10-27", "2011-10-28"});
		properties.put("regiestTime:date in", DateUtils.parse("2011-10-27"));
		
		q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();

		sql = "select mag from Magazine mag where ( mag.createTime >= :mag_createTime0 and mag.createTime < :mag_createTime1 ) and ( mag.regiestTime >= :mag_regiestTime2 and mag.regiestTime < :mag_regiestTime3 ) order by mag.id desc";
		paramsting = "{mag_createTime0=Thu Oct 27 00:00:00 CST 2011, mag_createTime1=Fri Oct 28 00:00:00 CST 2011, mag_regiestTime2=Thu Oct 27 00:00:00 CST 2011, mag_regiestTime3=Fri Oct 28 00:00:00 CST 2011}";

//		System.out.println(q.getSql());
//		System.out.println((Map)q.getParamsValue().getValues());
		
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());

		properties = CUtils.asLinkedMap(
				"lastUpdateTime:date in", ":yesterday", 
				"createTime:date in", ":today",
						"&lower(name):like", "tom%", 
						"age:=", 17, 
						"regiestTime:date in", new Date());
		properties.put(K.DESC, "id");
		
		q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		String str = "select mag from Magazine mag where ( mag.lastUpdateTime >= :mag_lastUpdateTime0 and mag.lastUpdateTime < :mag_lastUpdateTime1 ) and ( mag.createTime >= :mag_createTime2 and mag.createTime < :mag_createTime3 ) and lower(mag.name) like :lower_mag_name_4 and mag.age = :mag_age5 and ( mag.regiestTime >= :mag_regiestTime6 and mag.regiestTime < :mag_regiestTime7 ) order by mag.id desc ";
//		System.out.println("testDateIn:" + q.getSql());
//		System.out.println("testDateIn:" +(Map)q.getParamsValue().getValues());
		Assert.assertEquals(str.trim(), q.getSql());
		
//		System.out.println("testDateIn end ==================>>>>>>>>>\n");
	}


	@Test
	public void testAnd(){
		properties.put("name:like", "way");
		properties.put(K.AND, CUtils.asMap(new String[]{"age"}, new Object[]{17, 18}));

		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();

//		System.out.println("testAnd:" + q.getSql());
//		System.out.println("testAnd:" + q.getParamsValue().getValues());
		
		String sql = "select mag from Magazine mag where mag.name like :mag_name0 and ( (mag.age = :mag_age1  or mag.age = :mag_age2 ) ) order by mag.id desc";
		String paramsting = "{mag_name0=%way%, mag_age1=17, mag_age2=18}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}
	

	@Test
	public void testOr(){
		//or 示例用法
		Map or1 = new LinkedHashMap();
		or1.put("columnId", 111);
		
		properties.put("siteId", 1);
		properties.put("title:like", "sdsd");
		
		properties.put(K.OR, or1);
		
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
		System.out.println("testOr sql: " + q.getSql());
		
		String sql = "select object from Object object where object.siteId = :object_siteId0 and object.title like :object_title1 or ( object.columnId = :object_columnId2 ) order by object.id desc ";
		String paramsting = "{object_siteId0=1, object_title1=%sdsd%, object_columnId2=111}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}

	@Test
	public void testOr2(){
		//or 示例用法
		Map or1 = new LinkedHashMap();
		Map and1 = new HashMap();
		and1.put("department.id", 11);
		and1.put("columns.id", 1);
		
		or1.put("columnId", 111);
		or1.put(K.OR, and1);
		
		properties.put("siteId", 1);
		properties.put("title:like", "sdsd");
		
		properties.put(K.AND, or1);
		
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Object.class, properties);
		q.build();
		
		String sql = "select object from Object object where object.siteId = :object_siteId0 and object.title like :object_title1 and ( object.columnId = :object_columnId2 or ( object.columns.id = :object_columns_id3 and object.department.id = :object_department_id4 ) ) order by object.id desc ";
		String paramsting = "{object_siteId0=1, object_title1=%sdsd%, object_columnId2=111, object_columns_id3=1, object_department_id4=11}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}

	/*@Test
	public void testHas(){
		this.properties.put("name:is null", true);
//		this.properties.put("address:!=", Keys.Empty);
		this.properties.put("address:has", "testAddress");
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
		q.build();
		
		String sql = "select mag from Magazine mag where mag.name is null and :mag_address0 member of mag.address order by mag.id desc";
		String paramsting = "{mag_address0=testAddress}";
//		System.out.println("testHas: " + q.getSql().trim());
//		System.out.println("testHas: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}*/
	

	
	@Test
	public void testExceptionIfNullValue(){
		try {
			this.properties.put("name:", null);
			this.properties.put("nickname:", "way");
			this.properties.put(K.DEBUG, true);
			this.properties.put(K.IF_NULL, K.IfNull.Throw);
			ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(Magazine.class, "mag", properties);
			q.build();
			TestCase.fail("null value should be fail!");
		} catch (Exception e) {
			e.printStackTrace();
//			Assert.assertEquals(BaseException.Prefix+ExtQuery.Msg.THROW_IF_NULL_MSG, e.getMessage());
		}
	}


	@Test
	public void testDeleteQuery(){
		this.properties.put("name:", null);
		this.properties.put("nickname:not in", "way");
		this.properties.put(K.DEBUG, true);
		this.properties.put(K.IF_NULL, IfNull.Ignore);
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createDeleteQuery(Magazine.class, properties);
		q.build();
		
		String sql = "delete from org.onetwo.common.db.Magazine where nickname not in ( :nickname0)";
		String paramsting = "{nickname0=way}";
		System.out.println("testHas: " + q.getSql().trim());
//		System.out.println("testHas: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
		
	}

	@Test
	public void testDeleteQueryWithTable(){
		this.properties.put("name:", null);
		this.properties.put("nickname:not in", "way");
		this.properties.put(K.DEBUG, true);
		this.properties.put(K.IF_NULL, IfNull.Ignore);
		
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createDeleteQuery(MagazineWithTable.class, properties);
		q.build();
		
		String sql = "delete from org.onetwo.common.db.Magazine$MagazineWithTable where nickname not in ( :nickname0)";
		String paramsting = "{nickname0=way}";
		System.out.println("testHas: " + q.getSql().trim());
//		System.out.println("testHas: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
	}

	@Test
	public void testDataFilter(){
		this.properties.put(K.DEBUG, true);
		this.properties.put(K.DATA_FILTER, true);
		
		ExtQueryInner q = sqlSymbolManagerFactory.getJPA().createSelectQuery(EntityWithDataFilter.class, "t", properties);
		q.build();
		
		String sql = "select t from EntityWithDataFilter t where t.status != :t_status0 and (t.testField = :t_testField1  or t.testField = :t_testField2 ) order by t.id desc";
		String paramsting = "{t_status0=DISABLED, t_testField1=1, t_testField2=2}";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting, q.getParamsValue().getValues().toString());
		

		this.properties.put(K.DATA_FILTER, false);
		
		q = sqlSymbolManagerFactory.getJPA().createSelectQuery(EntityWithDataFilter.class, "t", properties);
		q.build();

		sql = "select t from EntityWithDataFilter t order by t.id desc";
		System.out.println("generated sql: " + q.getSql().trim());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(q.getParamsValue().getValues().toString(), "{}");
	}
	
}
