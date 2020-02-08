package org.onetwo.common.db.generator.dialet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.onetwo.common.db.generator.DBConnecton;
import org.onetwo.common.db.generator.meta.TableMeta;
import org.onetwo.common.db.generator.utils.DBUtils;
import org.onetwo.common.utils.StringUtils;

public class MysqlMetaDialet extends BaseMetaDialet implements DatabaseMetaDialet {
	
	
	public MysqlMetaDialet(DataSource dataSource) {
		super(dataSource);
	}
	
	protected Optional<TableMeta> findTableMeta(DBConnecton dbcon, String tableName) throws SQLException {
		Map<String, Object> rowMap = null;
		ResultSet rs = dbcon.getMetaData().getTables(catalog, schema, tableName.trim(), null);
		if(rs.next()){
			rowMap = DBUtils.toMap(rs);
		}else{
//			throw new DbmException("not table found: " + tableName);
			return Optional.empty();
		}

		String tname = (String)rowMap.get("TABLE_NAME");
		String comment = (String)rowMap.get("REMARKS");
		if(StringUtils.isBlank(comment)){
			rs = dbcon.query("SHOW TABLE STATUS LIKE '"+tableName+"'");
			rowMap = DBUtils.nextRowToMap(rs, "comment");
			comment = (String)rowMap.get("comment");
		}
		TableMeta table = new TableMeta(tname, comment);
		return Optional.of(table);
	}

}
