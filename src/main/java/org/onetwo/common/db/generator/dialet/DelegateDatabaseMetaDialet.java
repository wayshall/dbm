package org.onetwo.common.db.generator.dialet;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.onetwo.common.db.DataBase;
import org.onetwo.common.db.generator.mapping.ColumnMapping;
import org.onetwo.common.db.generator.mapping.MetaMapping;
import org.onetwo.common.db.generator.meta.TableMeta;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.JdbcUtils;

/**
 * @author weishao zeng
 * <br/>
 */
public class DelegateDatabaseMetaDialet implements DatabaseMetaDialet {
	
	private DatabaseMetaDialet delegate;
	
	public DelegateDatabaseMetaDialet(DataSource dataSource) {
		DataBase db = JdbcUtils.getDataBase(dataSource);
		if(db==DataBase.MySQL){
			delegate = new MysqlMetaDialet(dataSource);
		}else if(db==DataBase.Oracle){
			delegate =  new OracleMetaDialet(dataSource);
		}else{
			throw new DbmException("unsupported database : " + db);
		}
	}

	@Override
	public Optional<TableMeta> findTableMeta(String tableName) {
		return delegate.findTableMeta(tableName);
	}

	public MetaMapping getMetaMapping() {
		return delegate.getMetaMapping();
	}

	public ColumnMapping getColumnMapping(int sqlType) {
		return delegate.getColumnMapping(sqlType);
	}

	public List<String> getTableNames() {
		return delegate.getTableNames();
	}

	public TableMeta getTableMeta(String tableName) {
		return delegate.getTableMeta(tableName);
	}

}

