package org.onetwo.dbm.id;

import java.util.List;
import java.util.Map;

import org.onetwo.common.expr.Expression;
import org.onetwo.common.expr.ExpressionFacotry;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
public class TableIdGenerator implements IdGenerator<Long>{
	
	private static final String TEMPLATE_SELECT_SQL = "select ${valueColumnName} from ${table} where ${pkColumnName} = ? for update";
	private static final String TEMPLATE_INSERT_SQL = "insert into ${table} ( ${pkColumnName}, ${valueColumnName} ) " + " values ( ?, ? )";
	private static final String TEMPLATE_UPDATE_SQL = "update ${table} set ${valueColumnName}=?  where ${valueColumnName}=? and ${pkColumnName}=?";
	

	static final public String ATTR_TABLE = "table";
	//列1，varchar 类型，存储生成ID的键
	static final public String ATTR_PK_COLUMN_NAME = "pkColumnName";  
	// 列2，int 类型，存储ID值
	static final public String ATTR_VALUE_COLUMN_NAME = "valueColumnName";
	//列1的键值
	static final public String ATTR_PK_COLUMN_VALUE = "pkColumnValue";
	static final public String ATTR_ALLOCATION_SIZE = "allocationSize";
	
	final private Expression parser = ExpressionFacotry.DOLOR;

	final private String selectSql;
	final private String insertSql;
	final private String updateSql;
	final private TableGeneratorAttrs attrs;
	
	public TableIdGenerator(TableGeneratorAttrs attrs) {
		this.attrs = attrs;
		Map<String, Object> provider = ReflectUtils.toMap(attrs);
		this.selectSql = parser.parseByProvider(TEMPLATE_SELECT_SQL, provider);
		this.insertSql = parser.parseByProvider(TEMPLATE_INSERT_SQL, provider);
		this.updateSql = parser.parseByProvider(TEMPLATE_UPDATE_SQL, provider);
	}

	@Override
	public String getName() {
		return attrs.getName();
	}

	@Override
	public Long generate(DbmSessionImplementor session) {
		return null;
	}

	@Override
	public List<Long> batchGenerate(DbmSessionImplementor session, int batchSize) {
		Integer currentValue = session.getDbmJdbcOperations().queryForObject(selectSql, Integer.class, seqName);
		if(currentValue==null){
			session.getDbmJdbcOperations().update(insertSql, args);
		}
		return null;
	}
	
}
