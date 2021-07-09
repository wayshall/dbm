package org.onetwo.dbm.sharding;

import java.util.Set;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.google.common.collect.Sets;

/**
 * @author weishao zeng
 * <br/>
 */

public class ExportTableAliasVisitor extends MySqlASTVisitorAdapter {
	
	private Set<SQLBinaryOperator> shardableOperators = Sets.newHashSet(SQLBinaryOperator.Equality);
	
	public boolean visit(SQLExprTableSource x) {
        String alias = x.getAlias();
        String table = x.getExpr().toString();
        if (x.getExpr() instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr tableExp = (SQLIdentifierExpr)x.getExpr();
            table = tableExp.getName();
        }
        System.out.println("table: " + table + ", alias: " + alias);
        return true;
    }
	

    public boolean visit(SQLBinaryOpExpr x) {
    	if (shardableOperators.contains(x.getOperator())) {
    		System.out.println("left: " + x.getLeft() + ", right: " + x.getRight());
    	}
        return true;
    }
}
