package org.onetwo.common.db.generator.dialet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.onetwo.common.db.generator.DBConnecton;
import org.onetwo.common.db.generator.mapping.ColumnMapping;
import org.onetwo.common.db.generator.mapping.MetaMapping;
import org.onetwo.common.db.generator.mapping.ResultSetMapper;
import org.onetwo.common.db.generator.mapping.SimpleMetaMapping;
import org.onetwo.common.db.generator.meta.ColumnMeta;
import org.onetwo.common.db.generator.meta.TableMeta;
import org.onetwo.common.db.generator.utils.DBUtils;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.Assert;
import org.onetwo.dbm.exception.DbmException;
import org.slf4j.Logger;

abstract public class BaseMetaDialet implements DatabaseMetaDialet {
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	protected List<String> tableNames = new ArrayList<>();
	final private DataSource dataSource;
	private MetaMapping metaMapping = new SimpleMetaMapping();
	protected String catalog;
	protected String schema;
	
	public BaseMetaDialet(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}
	
	public BaseMetaDialet(DataSource dataSource, String catalog, String schema) {
		super();
		this.dataSource = dataSource;
		this.catalog = catalog;
		this.schema = schema;
	}


	public Optional<TableMeta> findTableMeta(String tableName){
//		ResultSet rs = null;
		Optional<TableMeta> tableOpt = Optional.empty();
		DBConnecton dbcon = newDBConnecton();
		try {
			/*rs = dbcon.getMetaData().getTables(catalog, schema, tableName.trim(), null);
			if(rs.next()){
				rowMap = DBUtils.toMap(rs);
			}else{
				throw new DbmException("not table found: " + tableName);
			}

			String tname = (String)rowMap.get("TABLE_NAME");
			String comment = (String)rowMap.get("REMARKS");
			if(StringUtils.isBlank(comment)){
				rs = dbcon.query("SHOW TABLE STATUS LIKE '"+tableName+"'");
				rowMap = DBUtils.nextRowToMap(rs, "comment");
				comment = (String)rowMap.get("comment");
			}
			table = new TableMeta(tname, comment);*/
			tableOpt = findTableMeta(dbcon, tableName);
			if (tableOpt.isPresent()) {
				createFieldMeta(dbcon, tableOpt.get(), tableName);
				createPrimaryKey(dbcon, tableOpt.get());
			}
		} catch (SQLException e) {
			DBUtils.handleDBException(e);
		} finally{
			dbcon.close();
		}
		return tableOpt;
	}
	
	abstract protected Optional<TableMeta> findTableMeta(DBConnecton dbcon, String tableName) throws SQLException;
	
	public TableMeta getTableMeta(String tableName){
		return findTableMeta(tableName).orElseThrow(() -> {
			return new DbmException("table not found: " + tableName);	
		});
	}

	@Override
	public List<String> getTableNames() {
		if(!tableNames.isEmpty()){
			return tableNames;
		}
		ResultSet rs = null;
		DBConnecton dbcon = newDBConnecton();
		try {
			rs = dbcon.getMetaData().getTables(catalog, schema, null, null);
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				this.tableNames.add(tableName);
			}
		} catch (SQLException e) {
			DBUtils.handleDBException(e);
		}finally{
			dbcon.close();
		}
		return this.tableNames;
	}

	protected DBConnecton newDBConnecton(){
		return new DBConnecton(this.dataSource);
	}

	public MetaMapping getMetaMapping() {
		return metaMapping;
	}
	

	public ColumnMapping getColumnMapping(int sqlType){
		return getMetaMapping().getColumnMapping(sqlType);
	}

	public void setMetaMapping(MetaMapping sqlTypeMapping) {
		this.metaMapping = sqlTypeMapping;
	}

	protected void handleDBException(String dbmExceptionMsg, Exception e){
		if (RuntimeException.class.isInstance(e)) {
			throw (RuntimeException) e;
		} else if( SQLException.class.isInstance(e)) {
			throw new DbmException(dbmExceptionMsg, e);
		} else {
			throw new DbmException(dbmExceptionMsg, e);
		}
	}

	protected void createPrimaryKey(DBConnecton dbcon, TableMeta table) throws SQLException {
		ResultSet rs = null;
		try {
			rs = dbcon.getMetaData().getPrimaryKeys(catalog, schema, table.getName());
			if (rs.next()) {
				ColumnMeta column = table.getColumn(rs.getString("COLUMN_NAME"));
				Assert.notNull(column);
				column.setPrimaryKey(true);
				table.setPrimaryKey(column);
			}
		} catch (Exception e) {
			handleDBException("createTablePrimaryKey error ", e);
		} finally{
			DBUtils.closeResultSet(rs);
		}
	}
	
	protected void createFieldMeta(DBConnecton dbcon, TableMeta table, String tableName) throws SQLException{
		ResultSet rs = dbcon.getMetaData().getColumns(catalog, schema, table.getName(), null);
		DBUtils.toList(rs, true, new ResultSetMapper<ColumnMeta>(){

			@Override
			public ColumnMeta map(ResultSet rs) throws SQLException {
//				Map<String, Object> rsMap = DBUtils.toMap(rs, false);
				String colName = rs.getString("COLUMN_NAME");
				int sqlType = rs.getInt("DATA_TYPE");
				String remark = rs.getString("REMARKS");
				/*if("estate_Id".equalsIgnoreCase(colName)){
					System.out.println("test");
				}*/
				String isNullable  = rs.getString("IS_NULLABLE");
				int columnSize = rs.getInt("COLUMN_SIZE");
				ColumnMapping mapping = getMetaMapping().getRequiredColumnMapping(colName, sqlType);
//				logger.info("mapping -> colunm: {}, sqltype: {}", colName, mapping);
				ColumnMeta meta = new ColumnMeta(table, colName, mapping);
				meta.setComment(remark);
				meta.setNullable("yes".equalsIgnoreCase(isNullable));
				meta.setColumnSize(columnSize);
				
				meta.init();
				table.addColumn(meta);
				return meta;
			}
			
		});
		
		//{pktable_cat=zhiyetong, pktable_schem=null, pktable_name=zyt_estate, pkcolumn_name=ID, fktable_cat=zhiyetong, fktable_schem=null, fktable_name=zyt_estate_unit, fkcolumn_name=ESTATE_ID, key_seq=1, update_rule=3, delete_rule=3, fk_name=FK_Reference_5, pk_name=null, deferrability=7}
		rs = dbcon.getMetaData().getImportedKeys(catalog, schema, table.getName());
		DBUtils.toList(rs, true, new ResultSetMapper<ColumnMeta>(){

			@Override
			public ColumnMeta map(ResultSet rs) throws SQLException {
//				Map<String, Object> rsMap = DBUtils.toMap(rs, false);
				String fkColumnName = rs.getString("fkcolumn_name");
				ColumnMeta fkColumn = table.getColumn(fkColumnName);
				if(fkColumn==null){
					throw new BaseException("找不到外键列：" + fkColumnName);
				}
				/*if("estate_Id".equalsIgnoreCase(colName)){
					System.out.println("test");
				}*/
				return fkColumn;
			}
			
		});
	}
	
}
