package org.onetwo.common.dbm.jdbc;
/**
 * @author weishao zeng
 * <br/>
 */

import org.junit.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;

public class NamedParameterSqlTest {
	
	@Test
	public void test() {
		String sql = "select u.id, u.name, u.user_name, u.status, u.avatar, d.id as department_id, d.`name` as department_name, du.is_leader_in_dept as department_leader_in_dept, d.depart_code as department_depart_code, d.depart_level as department_depart_level, d.parent_id as department_parent_id from org_user u left join org_department_user du on du.user_id = u.id left join org_department d on d.id = du.department_id where d.depart_code like :departCode?postlike and du.is_leader_in_dept = 0 order by du.is_leader_in_dept desc  limit :DbmQueryFirstResult, :DbmQueryMaxResult";
		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		System.out.println("parsedSql: " + parsedSql);
	}

}
