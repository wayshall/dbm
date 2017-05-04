package org.onetwo.dbm.id;

import java.util.Map;

import org.onetwo.common.expr.Expression;
import org.onetwo.common.expr.ExpressionFacotry;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.dbm.core.spi.DbmSessionImplementor;
import org.onetwo.dbm.exception.DbmException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * @author wayshall
 * <br/>
 */
public class TableIdGenerator extends AbstractIdGenerator {
	
	private static final String TEMPLATE_SELECT_SQL = "select ${valueColumnName} from ${table} where ${pkColumnName} = ? for update";
	private static final String TEMPLATE_INSERT_SQL = "insert into ${table} ( ${pkColumnName}, ${valueColumnName} ) " + " values ( ?, ? )";
	private static final String TEMPLATE_UPDATE_SQL = "update ${table} set ${valueColumnName}=?  where ${pkColumnName}=? and ${valueColumnName}=?";
	

	final private Expression parser = ExpressionFacotry.DOLOR;

	final private String selectSql;
	final private String insertSql;
	final private String updateSql;
	final private TableGeneratorAttrs attrs;
	
	public TableIdGenerator(TableGeneratorAttrs attrs) {
		super(attrs.getName());
		this.attrs = attrs;
		Map<String, Object> provider = ReflectUtils.toMap(attrs);
		this.selectSql = parser.parseByProvider(TEMPLATE_SELECT_SQL, provider);
		this.insertSql = parser.parseByProvider(TEMPLATE_INSERT_SQL, provider);
		this.updateSql = parser.parseByProvider(TEMPLATE_UPDATE_SQL, provider);
	}


	@Override
	protected int getAllocationSize() {
		return attrs.getAllocationSize();
	}

	@Override
	public Pair<Long, Long> batchGenerate(DbmSessionImplementor session, int batchSize) {
//		DbmSessionImplementor session = (DbmSessionImplementor)contextSession.getSessionFactory().openSession();
		另开事务
		Long currentId = null;
		try {
			currentId = session.getDbmJdbcOperations().queryForObject(selectSql, Long.class, attrs.getPkColumnValue());
		} catch (BadSqlGrammarException e) {
			//MySQLSyntaxErrorException SQLState=42S02 vendorCode=1146
			throw e;
		}catch (EmptyResultDataAccessException e) {
			//no result
		}
		if(currentId==null){
			int res = session.getDbmJdbcOperations().update(insertSql, attrs.getPkColumnValue(), attrs.getInitialValue());
			if(res!=1){
				throw new DbmException("error insert table sequeue : "+attrs.getPkColumnValue());
			}
			currentId = Long.valueOf(attrs.getInitialValue());
		}else{
			currentId = currentId + 1;
		}
		Long maxId = currentId + attrs.getAllocationSize() - 1;
		int res = session.getDbmJdbcOperations().update(updateSql, maxId, attrs.getPkColumnValue(), currentId);
		if(res!=1){
			throw new DbmException("error insert table sequeue : "+attrs.getPkColumnValue());
		}
		return Pair.of(currentId, maxId);
	}
	
}
