package org.onetwo.common.db.generator.dialet;

import java.util.List;
import java.util.Optional;

import org.onetwo.common.db.generator.mapping.ColumnMapping;
import org.onetwo.common.db.generator.mapping.MetaMapping;
import org.onetwo.common.db.generator.meta.TableMeta;

public interface DatabaseMetaDialet {
	public MetaMapping getMetaMapping();
	
	public ColumnMapping getColumnMapping(int sqlType);
	
	public List<String> getTableNames();
	
	public Optional<TableMeta> findTableMeta(String tableName);
	
	public TableMeta getTableMeta(String tableName);
}
