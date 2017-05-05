package org.onetwo.dbm.id;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onetwo.common.expr.Expression;
import org.onetwo.common.expr.ExpressionFacotry;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.dbm.core.spi.DbmSessionImplementor;
import org.onetwo.dbm.core.spi.DbmTransaction;
import org.onetwo.dbm.exception.DbmException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author wayshall
 * <br/>
 */
public class TableIdGenerator extends AbstractIdentifierGenerator {
	
	private static final String TEMPLATE_CREATE_TABLE_SQL = " CREATE TABLE `${table}` ("
			+ "`${pkColumnName}`  varchar(255) NOT NULL ,"
			+ "`${valueColumnName}`  bigint NOT NULL ,"
			+ "PRIMARY KEY (`${pkColumnName}`)"
			+ ");";
	
	private static final String TEMPLATE_SELECT_SQL = "select ${valueColumnName} from ${table} where ${pkColumnName} = ? for update";
	private static final String TEMPLATE_INSERT_SQL = "insert into ${table} ( ${pkColumnName}, ${valueColumnName} ) " + " values ( ?, ? )";
	private static final String TEMPLATE_UPDATE_SQL = "update ${table} set ${valueColumnName}=?  where ${pkColumnName}=? and ${valueColumnName}=?";
	

	final private Expression parser = ExpressionFacotry.DOLOR;

	final private String createTableSql;
	final private String selectSql;
	final private String insertSql;
	final private String updateSql;
	final private TableGeneratorAttrs attrs;
	
	public TableIdGenerator(TableGeneratorAttrs attrs) {
		super(attrs.getName());
		this.attrs = attrs;
		Map<String, Object> provider = ReflectUtils.toMap(attrs);
		this.createTableSql = parser.parseByProvider(TEMPLATE_CREATE_TABLE_SQL, provider);
		this.selectSql = parser.parseByProvider(TEMPLATE_SELECT_SQL, provider);
		this.insertSql = parser.parseByProvider(TEMPLATE_INSERT_SQL, provider);
		this.updateSql = parser.parseByProvider(TEMPLATE_UPDATE_SQL, provider);
	}


	@Override
	protected int getAllocationSize() {
		return attrs.getAllocationSize();
	}

	/****
	 */
	@Override
	public List<Long> batchGenerate(DbmSessionImplementor contextSession, int batchSize) {
		DbmSessionImplementor session = (DbmSessionImplementor)contextSession.getSessionFactory().openSession();
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		DbmTransaction transaction = session.beginTransaction(definition);
		try {
			Long currentId = null;
			try {
				currentId = session.getDbmJdbcOperations().queryForObject(selectSql, Long.class, attrs.getPkColumnValue());
			} catch (BadSqlGrammarException e) {
				//MySQLSyntaxErrorException SQLState=42S02 vendorCode=1146
//				throw e;
				this.createSeqTable(session);
			}catch (EmptyResultDataAccessException e) {
				//no result
			}
			if(currentId==null){
				int res = session.getDbmJdbcOperations().update(insertSql, attrs.getPkColumnValue(), attrs.getInitialValue());
				if(res!=1){
					throw new DbmException("error insert table sequeue : "+attrs.getPkColumnValue());
				}
				currentId = Long.valueOf(attrs.getInitialValue());
			}
			Long maxId = currentId + attrs.getAllocationSize();
			int res = session.getDbmJdbcOperations().update(updateSql, maxId, attrs.getPkColumnValue(), currentId);
			if(res!=1){
				throw new DbmException("error update table sequeue : "+attrs.getPkColumnValue()+", updateSql:"+updateSql);
			}
			transaction.commit();
			List<Long> ids = new ArrayList<Long>(batchSize);
			long nextId = currentId;
			for (; nextId < maxId; nextId++) {
				ids.add(nextId);
			}
			if(ids.size()!=batchSize){
				throw new DbmException("generate batch id count error. actual:"+ids.size()+", expect:"+batchSize);
			}
			return ids;
		} catch (DbmException e) {
			transaction.rollback();
			throw e;
		} catch (Exception e) {
			transaction.rollback();
			throw new DbmException("generate id error", e);
		} 
	}
	/*public Pair<Long, Long> batchGenerate(DbmSessionImplementor contextSession, int batchSize) {
		DbmSessionImplementor session = (DbmSessionImplementor)contextSession.getSessionFactory().openSession();
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		DbmTransaction transaction = session.beginTransaction(definition);
		try {
			Long nextId = null;
			try {
				nextId = session.getDbmJdbcOperations().queryForObject(selectSql, Long.class, attrs.getPkColumnValue());
			} catch (BadSqlGrammarException e) {
				//MySQLSyntaxErrorException SQLState=42S02 vendorCode=1146
//				throw e;
				this.createSeqTable(session);
			}catch (EmptyResultDataAccessException e) {
				//no result
			}
			if(nextId==null){
				int res = session.getDbmJdbcOperations().update(insertSql, attrs.getPkColumnValue(), attrs.getInitialValue());
				if(res!=1){
					throw new DbmException("error insert table sequeue : "+attrs.getPkColumnValue());
				}
				nextId = Long.valueOf(attrs.getInitialValue());
			}
			Long maxId = nextId + attrs.getAllocationSize();
			int res = session.getDbmJdbcOperations().update(updateSql, maxId, attrs.getPkColumnValue(), nextId);
			if(res!=1){
				throw new DbmException("error update table sequeue : "+attrs.getPkColumnValue()+", updateSql:"+updateSql);
			}
			transaction.commit();
			return Pair.of(nextId, maxId-1);
		} catch (Exception e) {
			transaction.rollback();
			throw new DbmException("generate id error", e);
		} 
	}*/
	
	private void createSeqTable(DbmSessionImplementor session){
		try {
			session.executeUpdate(createTableSql);
		} catch (Exception e) {
			throw new DbmException("create table error , create table sql:"+createTableSql);
		}
	}
	
}
