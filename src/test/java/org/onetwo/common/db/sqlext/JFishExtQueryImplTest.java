package org.onetwo.common.db.sqlext;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onetwo.common.db.Magazine;
import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.db.sqlext.ExtQueryUtils.F;
import org.onetwo.common.utils.CUtils;
import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.dialet.DBDialect.LockInfo;
import org.onetwo.dbm.dialet.MySQLDialect;
import org.onetwo.dbm.query.JFishSQLSymbolManagerImpl;

import lombok.Data;

public class JFishExtQueryImplTest {
	
	@Table(name="SYN_LOG_ROUTE")
	@Entity
	@Data
	public static class LogRouteEntity {
		@Id
		private Long id;
		private Long logSupplierId;
		private Long supplierCode;
		private Date synStartTime;
		private Date synEndTime;
	}

	Map<Object, Object> properties;
	
	private SQLSymbolManagerFactory sqlSymbolManagerFactory;
	DBDialect dialect = new MySQLDialect();
	private SQLSymbolManager sqlSymbolManager;
	

	@Before
	public void setup(){
		this.properties = new LinkedHashMap<Object, Object>();
		this.properties.put(K.DEBUG, true);
		sqlSymbolManagerFactory = SQLSymbolManagerFactory.getInstance();
		sqlSymbolManager = sqlSymbolManagerFactory.getJdbc();
	}
	
	@Test
	public void testSqlQueryJoin(){
		properties.put(K.SQL_JOIN, F.sqlJoin("left join syn_log_supplier sup on sup.id = ent.log_supplier_id"));
		properties.put(K.IF_NULL, K.IfNull.Ignore);
		properties.put("routeName", " ");
		properties.put("log_supplier_id", 22l);
		properties.put(".sup.supplier_code", "supplierCodeValue");
		properties.put(F.sqlFunc("ceil(@syn_end_time-@syn_start_time):>="), 1l);
		ExtQueryInner q = sqlSymbolManagerFactory.getJdbc().createSelectQuery(LogRouteEntity.class, "ent",  properties);
		q.build();
		
		/*String sql2 = "select ent.CREATE_TIME as createTime, ent.DELETE_TOUR as deleteTour, ent.FAIL_REASON as failReason, ent.FAIL_TOUR as failTour, ent.ID as id, ent.LAST_UPDATE_TIME as lastUpdateTime, ent.NEW_TOUR as newTour, ent.REPET_LOG_SUPPLIER_ID as repetLogSupplierId, ent.ROUTE_NAME as routeName, ent.STATE as state, ent.SUPPLIER_ROUTE_CODE as supplierRouteCode, ent.SYN_END_TIME as synEndTime, ent.SYN_START_TIME as synStartTime, ent.TYPE as type, ent.UPDATE_TOUR as updateTour, ent.YOOYO_ROUTE_ID as yooyoRouteId from SYN_LOG_ROUTE ent " +
				"left join syn_log_supplier sup on sup.id = ent.log_supplier_id where ent.log_supplier_id = :ent_log_supplier_id0 and sup.supplierCode = :sup_supplierCode1 and ceil(t.syn_end_time-t.syn_start_time) >= :ceil_t_syn_end_time_t_syn_start_time_2 order by ent.ID desc";
		*/
		String sql = "select ent.* from SYN_LOG_ROUTE ent left join syn_log_supplier sup on sup.id = ent.log_supplier_id "
				+ "where ent.log_supplier_id = :ent_log_supplier_id0 and sup.supplier_code = :sup_supplier_code1 and ceil(ent.syn_end_time-ent.syn_start_time) >= :ceil_ent_syn_end_time_ent_syn_start_time_2";
		String paramsting = "{ent_log_supplier_id0=22, sup_supplier_code1=supplierCodeValue, ceil_ent_syn_end_time_ent_syn_start_time_2=1}";
		System.out.println("testSqlQueryJoin: " + sql.trim());
		System.out.println("testSqlQueryJoin: " + q.getSql().trim());
//		System.out.println("testSqlQueryJoin: " + q.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), q.getSql().trim());
		Assert.assertEquals(paramsting.trim(), q.getParamsValue().getValues().toString().trim());
	}
	

	@Test
	public void testJFishExtQuery(){
		JFishSQLSymbolManagerImpl jqm = JFishSQLSymbolManagerImpl.create(dialect);
		this.properties.put("name:", null);
		this.properties.put("nickname:", "way");
		this.properties.put(K.IF_NULL, K.IfNull.Ignore);
		//left join table:alias on maintable.id=tur.magazin_id
		this.properties.put(K.LEFT_JOIN, CUtils.newArray("t_user_role:tur", new Object[]{"tur.magazin_id", "@id"}));
		ExtQueryInner query = jqm.createSelectQuery(Magazine.class, properties);
		query.build();
		String tsql = query.getSql();
		System.out.println("testJFishExtQuery:"+tsql);
		String expected = "select magazine.* from magazine magazine left join t_user_role tur on ( tur.magazin_id=magazine.id ) where magazine.nickname = :magazine_nickname0";
//		String expected = "select magazine.* from magazine magazine left join t_user_role tur on ( tur.magazin_id=id ) where nickname = :nickname0";
		Assert.assertEquals(expected, tsql.trim());
	}
	

	@Test
	public void testForUpdate(){
		properties = new LinkedHashMap<Object, Object>();
		properties.put("field1", "value1");
		properties.put("field2", 333);
		properties.put(K.FOR_UPDATE, LockInfo.write());
		ExtQueryInner q = JFishSQLSymbolManagerImpl.create(dialect).createSelectQuery(Object.class, properties);
		q.build();
		
		System.out.println("testForUpdate: " + q.getSql());
		//  for update 不再在此时生成，见DbmQueryImpl#getSqlString()
//		String sql = "select object.* from object object where object.field1 = :object_field10 and object.field2 = :object_field21 for update";
		String sql = "select object.* from object object where object.field1 = :object_field10 and object.field2 = :object_field21 ";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
	}

	@Test
	public void testUnselect(){
		properties = new LinkedHashMap<Object, Object>();
		properties.put("logSupplierId", 111);
		properties.put("supplierCode", "testCode");
		properties.put(K.DEBUG, true);
		properties.put(K.UNSELECT, new String[] {"synStartTime", "synEndTime"});
		// 涉及到unselect那些字段，需要用可以解释实体注解的sqlSymbolManager
		ExtQueryInner q = sqlSymbolManager.createSelectQuery(LogRouteEntity.class, "t", properties);
		q.build();

		String sql = "select t.log_supplier_id, t.id, t.supplier_code from SYN_LOG_ROUTE t where t.log_supplier_id = :t_log_supplier_id0 and t.supplier_code = :t_supplier_code1";
		Assert.assertEquals(sql.trim(), q.getSql().trim());
	}
}
