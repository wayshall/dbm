package org.onetwo.dbm.sharding;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

/**
 * @author weishao zeng
 * <br/>
 */

public class ExportTableAliasVisitor extends MySqlASTVisitorAdapter {
	
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
        System.out.println("right: " + x.getRight() + ", left: " + x.getLeft());
        return true;
    }
}
