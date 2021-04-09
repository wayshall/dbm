package org.onetwo.dbm.routing.parse;

import java.util.List;
import java.util.stream.Collectors;

import org.onetwo.common.db.DataBase;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.routing.RoutingSqlContext;
import org.onetwo.dbm.routing.RoutingTableRoutingService;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;

/**
 * @author weishao zeng
 * <br/>
 */
public class RoutingTableSqlParser {
	private RoutingTableRoutingService shardingTableRoutingService;
	private DataBase dataBase;
	
	public String parseRoutingSql(String sql) {
		SQLExprTableSource a = null;
		return sql;
	}
	
	protected List<SQLStatement> parseSql(String sql) {
		String dbtype = dataBase.toString().toLowerCase();
		List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbtype);
		
		for (SQLStatement stmt : stmtList) {
			visitSQLStatement(stmt);
		}
	}
	
	protected RoutingSqlContext visitSQLStatement(SQLStatement stmt) {
		RoutingSqlContext context = new RoutingSqlContext();
		switch (dataBase) {
		case MySQL:
			MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
			stmt.accept(visitor);
			List<String> tables = visitor.getTables().keySet().stream().map(n -> n.getName().toLowerCase()).collect(Collectors.toList());
			context.setTables(tables);
			context.setConditions(visitor.getConditions());
			break;

		default:
			throw new DbmException("unsupport data base: " + dataBase);
		}
		return context;
	}
	
	public boolean hasShardingConfigs() {
		return shardingTableRoutingService.hasShardingConfigs();
	}

}
