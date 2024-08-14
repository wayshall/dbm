package org.onetwo.common.db;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.db.sql.SelectItemInfo;
import org.onetwo.dbm.druid.DbmMySqlLexer;
import org.onetwo.dbm.exception.DbmException;

import com.alibaba.druid.DbType;
import com.alibaba.druid.DruidRuntimeException;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.druid.util.JdbcUtils;

abstract public class DruidUtils {

	public static String toCountSql(String sql){
		return changeAsCountStatement(sql).toString();
	}

	/*public static String toCountSql2(String sql, Object value){
		return changeAsCountStatement2(sql, value).toString();
	}

	public static SQLSelectStatement changeAsCountStatement2(String sql, Object value){
		List<SQLStatement> statements = SQLUtils.parseStatements(sql, JdbcUtils.MYSQL);
		SQLSelectStatement selectStatement = getSQLSelectStatement(statements, 0);
		if(selectStatement==null){
			throw new DbmException("it must be a select query, sql: " + sql);
		}
		selectStatement.accept(new TrimOrderBySQLASTVisitorAdapter());
		return selectStatement;
	}*/
	public static SQLSelectStatement getSQLSelectStatement(List<SQLStatement> statements, int index){
		if(!SQLSelectStatement.class.isInstance(statements.get(index))){
			return null;
		}
		SQLSelectStatement selectStatement = (SQLSelectStatement)statements.get(index);
		return selectStatement;
	}
	

	public static List<SQLStatement> parseStatements(String sql) {
		return parseStatements(sql, DbType.mysql);
	}
	
    public static List<SQLStatement> parseStatements(String sql, DbType dbType) {
    	DbmMySqlLexer lexer = new DbmMySqlLexer(sql);
    	lexer.nextToken();
    	MySqlStatementParser parser = new MySqlStatementParser(lexer);
		List<SQLStatement> stmtList = parser.parseStatementList();
        if (parser.getLexer().token() != Token.EOF) {
            throw new DruidRuntimeException("syntax error : " + sql);
        }
        return stmtList;
    }
	
	public static SQLSelectStatement changeAsCountStatement(String sql){
		return changeAsCountStatement(JdbcUtils.MYSQL, sql);
	}
	
	public static SQLSelectStatement changeAsCountStatement(DbType dbType, String sql){
//		List<SQLStatement> statements = SQLUtils.parseStatements(sql, dbType);
		List<SQLStatement> statements = parseStatements(sql, dbType);
		
		SQLSelectStatement selectStatement = getSQLSelectStatement(statements, 0);
		if(selectStatement==null){
			throw new DbmException("it must be a select query, sql: " + sql);
		}
		SQLSelect select = selectStatement.getSelect();
		SQLSelectQueryBlock query = (SQLSelectQueryBlock)select.getQuery();

//		query.setOrderBy(null);
		selectStatement.accept(new TrimOrderBySQLASTVisitorAdapter());
		if(query.getGroupBy()!=null){
			SQLSelectQueryBlock countquery = new SQLSelectQueryBlock();	
			SQLSelectStatement countSql = new SQLSelectStatement(new SQLSelect(countquery), dbType);
			
			SQLSelectItem countItem = createCountSelectForQuery(countquery, "");
			
			SQLSubqueryTableSource sub = new SQLSubqueryTableSource(select);
			sub.setAlias("countView");
			
			countquery.addSelectItem(countItem);
			countquery.setFrom(sub);

			return countSql;
		}
		
		List<SQLSelectItem> items = query.getSelectList();
		items.clear();
		
		SQLSelectItem countItem = createCountSelectForQuery(query, "");
		items.add(countItem);

		
		return selectStatement;
	}

	protected static SQLSelectItem createCountSelectForQuery(SQLSelectQuery query, Object value){
		SQLSelectItem countItem = new SQLSelectItem();
		SQLAggregateExpr countMethod = new SQLAggregateExpr("count");
		countMethod.setParent(countItem);
		if(value==null || value instanceof Number || StringUtils.isBlank(value.toString())){
			countMethod.addArgument(new SQLIntegerExpr(1));
		}else{
			countMethod.addArgument(new SQLIdentifierExpr(value.toString()));
		}
		
		countItem.setExpr(countMethod);
		countItem.setParent(query);
		
		return countItem;
	}
	
	static class TrimOrderBySQLASTVisitorAdapter extends MySqlASTVisitorAdapter {
	    @Override
	    public void endVisit(MySqlSelectQueryBlock x) {
	    	x.setOrderBy(null);
	    }
	}
	
	
	
	public static List<SelectItemInfo> extractSelectItems(String sql) {
		SQLSelectQueryBlock query = parseAsSelectQuery(sql);
		List<SQLSelectItem> selectItems = query.getSelectList();
		List<SelectItemInfo> selectItemList = selectItems.stream().filter(item -> {
			return item.getExpr() instanceof SQLPropertyExpr;
		}).map(item -> {
			SQLPropertyExpr expr = (SQLPropertyExpr)item.getExpr();
			String name = expr.getName().replace("`", "");
			SelectItemInfo sitem = new SelectItemInfo(name, item.getAlias2());
			return sitem;
		}).collect(Collectors.toList());
		return selectItemList;
	}
	
	public static SQLSelectQueryBlock parseAsSelectQuery(String sql) {
		SQLSelectStatement sqlStatement = (SQLSelectStatement)SQLUtils.parseSingleMysqlStatement(sql);
		
		SQLSelect select = sqlStatement.getSelect();
		SQLSelectQueryBlock query = select.getQueryBlock();
//		List<SQLSelectItem> selectItems = query.getSelectList();
		return query;
	}
	
	private DruidUtils(){
	}

}
