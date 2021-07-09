package org.onetwo.dbm.sharding;
/**
 * @author weishao zeng
 * <br/>
 */

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.onetwo.common.db.DruidUtils;
import org.onetwo.common.db.sqlext.ExtQueryUtils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat.Condition;
import com.alibaba.druid.util.JdbcConstants;

public class ShardingSqlTest {
	
	@Test
	public void testSelect() {
		String sql = "select * from user u where u.depart_id = :departId and u.name like :request.userName?likeString and u.age = ?";
		
		String countSql = ExtQueryUtils.buildCountSql(sql, "1");
		System.out.println("countsql: " + countSql);
		
		printSql(sql);
		
		sql = "select * from user u left join organ org on org.id = u.organ_id where u.depart_id = :departId";
		printSql(sql);
	}
	
	private void printSql(String sql) {
		
//		DbmUtils.parseNamedSql(sql, new EmptySqlParameterSource());
		
		String dbType = JdbcConstants.MYSQL;
//		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
		List<SQLStatement> stmtList = DruidUtils.parseStatements(sql, dbType);
 
		ExportTableAliasVisitor visitor = new ExportTableAliasVisitor();
		MySqlSchemaStatVisitor mySqlStatVisitor = new MySqlSchemaStatVisitor();
//		MySqlExportParameterVisitor paramterVisitor = new MySqlExportParameterVisitor(Lists.newArrayList(), new StringBuilder(), true);
		for (SQLStatement stmt : stmtList) {
			stmt.accept(visitor);
			stmt.accept(mySqlStatVisitor);
//			stmt.accept(paramterVisitor);
		}
 
		String sqlString = SQLUtils.toSQLString(stmtList, dbType);
		System.out.println(sqlString);
		
		Collection<Condition> conditions = mySqlStatVisitor.getConditions();
		for (Condition condition : conditions) {
			System.out.println("condition column: " + condition.getColumn().getName() + 
					", table: " + condition.getColumn().getTable() + 
					", values: " + condition.getValues());
		}
		
//		paramterVisitor.getParameters().forEach(p -> {
//			System.out.println("parameter: " + p);
//		});
		System.out.println("================================================");
	}

}
