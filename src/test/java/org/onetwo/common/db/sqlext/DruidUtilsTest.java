package org.onetwo.common.db.sqlext;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.onetwo.common.db.DruidUtils;
import org.onetwo.common.file.FileUtils;
import org.springframework.core.io.ClassPathResource;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.util.JdbcUtils;

public class DruidUtilsTest {

	@Test
	public void testFullJoinSql(){
		String sql = "SELECT   * FROM  emp e FULL   JOIN dept d     ON d.deptno = e.deptno";
		List<SQLStatement> statements = SQLUtils.parseStatements(sql, JdbcUtils.MYSQL);
		statements.forEach(s -> {
			SQLSelectStatement select = (SQLSelectStatement) s;
			System.out.println("statements: " + select.getSelect().toString());
		});
		
		SQLSelectStatement selectStatement = (SQLSelectStatement) statements.get(0);
		SQLSelect select = selectStatement.getSelect();
		MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) select.getQuery();
		SQLTableSource from = query.getFrom();
		SQLJoinTableSource joinTable = (SQLJoinTableSource) from;
		JoinType joinType = joinTable.getJoinType();
	}

	@Test
	public void testParseSql(){
		String sql1 = "select * from user where user_name=?";
		printStatements(sql1);
		String sql2 = "select id, user_name, password from user where user_name=?";
		printStatements(sql2);
		String sql3 = "select count(id) from user where user_name=?";
		printStatements(sql3);
		
		String sql = sql1 + ";" + sql2 + ";" + sql3;
		List<SQLStatement> statements = SQLUtils.parseStatements(sql, JdbcUtils.MYSQL);
		statements.forEach(s -> {
			SQLSelectStatement select = (SQLSelectStatement) s;
			System.out.println("statements: " + select.getSelect().toString());
		});
	}
	
	private void printStatements(String sql){
		List<SQLStatement> statements = SQLUtils.parseStatements(sql, JdbcUtils.MYSQL);
		System.out.println("-------------------->size is:" + statements.size());
		for(SQLStatement stmt : statements){
			System.out.println(stmt.getClass()+": "+stmt);
			MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            stmt.accept(visitor);
            
            //获取表名称
            System.out.println("Tables : " + visitor.getTables());
            //获取操作方法名称
            System.out.println("Tables : " + visitor.getTables());
            //获取字段名称
            System.out.println("fields : " + visitor.getColumns());
		}
		SQLSelectStatement selectStatement = (SQLSelectStatement)statements.get(0);
		SQLSelect select = selectStatement.getSelect();
		SQLSelectQueryBlock query = (SQLSelectQueryBlock)select.getQuery();
		List<SQLSelectItem> items = query.getSelectList();
		items.forEach(item->{
			System.out.println("SQLSelectItem:"+item.getExpr().getClass()+":"+item.getExpr());
			if(item.getExpr() instanceof SQLAggregateExpr){
				SQLAggregateExpr expr = (SQLAggregateExpr)item.getExpr();
				System.out.println("expr:"+expr);
			}else if(item.getExpr() instanceof SQLIdentifierExpr){
				SQLIdentifierExpr expr = (SQLIdentifierExpr)item.getExpr();
				System.out.println("identifier:"+expr);
			}
		});
		
		SQLExpr where = query.getWhere();
		System.out.println("where:"+where);
		
		SQLOrderBy orderby = query.getOrderBy();
		System.out.println("orderby:"+orderby);
		
	}
	

	private String readSql(String classPathFileName){
		String path = this.getClass().getPackage().getName().replace('.', '/')+"/"+classPathFileName;
		ClassPathResource cpr = new ClassPathResource(path);
		try {
			return FileUtils.readAsString(cpr.getFile());
		} catch (IOException e) {
			throw new RuntimeException("read  sql error!", e);
		}
	}
	@Test
	public void testOrderby2CountSql(){
		String sql = "select * from user where user_name=?";
		SQLSelectStatement selectStatement = this.changeAsCountSql(sql);
		System.out.println("new sql: "+selectStatement);

		//order by
		sql = readSql("testChangeSql.sql");
		printStatements(sql);
		String countSql = DruidUtils.toCountSql(sql);
		System.out.println("new sql: "+countSql);
		
	}

	@Test
	public void testGroupByCountSql(){
		String sql = "SELECT * FROM product_active pa GROUP BY pa.ACTIVE_DATE";
		SQLSelectStatement selectStatement = DruidUtils.changeAsCountStatement(sql);
		System.out.println("new sql: "+selectStatement);
//		assertThat(selectStatement.toString()).isEqualTo("SELECT count(1) FROM (SELECT * FROM product_active pa GROUP BY pa.ACTIVE_DATE ) countView");
	}

	@Test
	public void testGroupUnion2CountSql(){
		//union group by
		String sql = readSql("testChangeSql-groupby.sql");
		printStatements(sql);
		String countSql = DruidUtils.toCountSql(sql);
		System.out.println("new sql: "+countSql);
	}
	
	private SQLSelectStatement changeAsCountSql(String sql){
		List<SQLStatement> statements = SQLUtils.parseStatements(sql, JdbcUtils.MYSQL);
		SQLSelectStatement selectStatement = (SQLSelectStatement)statements.get(0);
		SQLSelect select = selectStatement.getSelect();
		SQLSelectQueryBlock query = (SQLSelectQueryBlock)select.getQuery();
		query.setOrderBy(null);
		if(query.getGroupBy()!=null){
			SQLSelectQueryBlock countquery = new SQLSelectQueryBlock();	
			SQLSelectStatement countSql = new SQLSelectStatement(new SQLSelect(countquery));
			
			SQLSelectItem countItem = new SQLSelectItem();
			SQLAggregateExpr countMethod = new SQLAggregateExpr("count");
			countMethod.setParent(countItem);
			countMethod.addArgument(new SQLIntegerExpr(1));
			countItem.setParent(countquery);
			countItem.setExpr(countMethod);
			
			SQLSubqueryTableSource sub = new SQLSubqueryTableSource(select);
			sub.setAlias("countView");
			
			countquery.addSelectItem(countItem);
			countquery.setFrom(sub);
			
			return countSql;
		}
		List<SQLSelectItem> items = query.getSelectList();
		items.clear();
		
		SQLSelectItem countItem = new SQLSelectItem();
		SQLAggregateExpr countMethod = new SQLAggregateExpr("count");
		countMethod.setParent(countItem);
		countMethod.addArgument(new SQLIntegerExpr(1));
//		countMethod.addArgument(new SQLIdentifierExpr("id"));
		countItem.setParent(query);
		countItem.setExpr(countMethod);
		
		items.add(countItem);
		
		
		return selectStatement;
	}
	
}
