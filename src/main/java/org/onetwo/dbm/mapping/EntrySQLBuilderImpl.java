package org.onetwo.dbm.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onetwo.common.expr.Expression;
import org.onetwo.common.expr.ExpressionFacotry;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.mapping.SQLBuilderFactory.SqlBuilderType;

public class EntrySQLBuilderImpl implements EntrySQLBuilder {

	public static final String VAR_SEQ_NAME = "seqName";
	public static final String SQL_MYSQL_PK = "select max(${id}) from ${tableName}";
	
	public static final String SQL_INSERT = "insert into ${tableName} ( ${insertFields} ) values ( ${namedFields} )";
	public static final String SQL_INSERT_OR_UPDATE = "insert into ${tableName} ( ${insertFields} ) values ( ${namedFields} ) "
													+ "on duplicate key update ${duplicateKeyUpdateFields}";
	public static final String SQL_INSERT_OR_IGNORE = "insert ignore into ${tableName} ( ${insertFields} ) values ( ${namedFields} ) ";
	public static final String SQL_UPDATE = "update ${tableName} set ${updateFields} where ${whereCause}";
	public static final String SQL_DELETE = "delete from ${tableName}";
	public static final String SQL_QUERY = "select ${selectFields} from ${tableName} ${alias}";
	public static final String SQL_QUERY_CAUSE = "where ${whereCause}";

	public static final Expression PARSER = ExpressionFacotry.DOLOR;

	
	public static final String QMARK = DbmMappedFieldValue.QMARK;

	protected String alias = "this_";
	protected DbmMappedEntryMeta entry;
	protected String tableName;
//	protected DbmMappedField identifyField;
	protected List<DbmMappedFieldValue> fields = LangUtils.newArrayList();
	protected List<DbmMappedFieldValue> whereCauseFields = LangUtils.newArrayList();
	
	private boolean debug;
//	private String placeHoder;
	private boolean namedPlaceHoder;
	
	private String sql;
	private SqlBuilderType type;
	
	private DBDialect dialet;
//	private LockInfo lock;
	
	EntrySQLBuilderImpl(DBDialect dialect, DbmMappedEntryMeta entry, String alias, boolean namedPlaceHoder, SqlBuilderType type){
		this.entry = entry;
		this.alias = alias;
		this.namedPlaceHoder = namedPlaceHoder;
		this.type = type;
//		this.identifyField = entry.getIdentifyField();
		this.tableName = entry.getTableInfo().getName();
		this.dialet = dialect;
	}
	
	public DBDialect getDialet() {
		return dialet;
	}

	/*public void setDialet(DBDialect dialet) {
		this.dialet = dialet;
	}*/

	public String getAlias(){
		return alias;
	}
	
	public String getColumnWithAlias(String alias){
		return getAlias() + "." + alias;
	}
	
	public EntrySQLBuilder append(DbmMappedField column){
		if(column!=null)
			this.fields.add(DbmMappedFieldValue.create(column, dialet));
		return this;
	}
	
	public EntrySQLBuilder appendWhere(Collection<? extends DbmMappedField> columns){
		columns.forEach(column -> {
			appendWhere(column);
		});
		return this;
	}
	
	public EntrySQLBuilder appendWhere(DbmMappedField column){
		if(column!=null)
			this.whereCauseFields.add(DbmMappedFieldValue.create(column, dialet));
		return this;
	}
	
	public EntrySQLBuilder appendWhere(DbmMappedFieldValue column){
		if(column!=null) {
			if (column.getDialet()==null) {
				column.setDialet(dialet);
			}
			this.whereCauseFields.add(column);
		}
		return this;
	}
	
	public EntrySQLBuilder append(Collection<? extends DbmMappedField> columns){
		if(columns!=null) {
//			this.fields.addAll(columns);
			columns.forEach(column -> {
				append(column);
			});
		}
		return this;
	}
	
	
	protected String getSeqName(){
		return entry.getTableInfo().getSeqName();
	}
	
	private String buildSeq(){
		sql = "select " + getSeqName() + ".nextval from dual";
		return sql;
	}
	private String buildCreateSeq(){
		sql = "create sequence "+getSeqName()+" minvalue 1 maxvalue 9999999999999999999999999999 start with 1 increment by 1 cache 20";
		return sql;
	}
	
	/* (non-Javadoc)
	 * @see org.onetwo.common.jfishdbm.mapping.EntrySQLBuilder#build()
	 */
	@Override
	public String build(){
		if(StringUtils.isNotBlank(sql))
			return sql;
		
		if (SqlBuilderType.insert==type) {
			this.sql = this.buildInsert();
		} else if (SqlBuilderType.insertOrIgnore==type) {
			this.sql = this.buildInsertOrIgnore();
		} else if (SqlBuilderType.insertOrUpdate==type) {
			this.sql = this.buildInsertOrUpdate();
		} else if (SqlBuilderType.update==type) {
			this.sql = this.buildUpdate();
		} else if (SqlBuilderType.delete==type) {
			this.sql = this.buildDelete();
		} else if (SqlBuilderType.query==type) {
			this.sql = this.buildQuery();
		}
//		else if(SqlBuilderType.primaryKey==type){
//			this.sql = this.buildPrimaryKey();
//		}
		else if (SqlBuilderType.seq==type) {
			this.sql = this.buildSeq();
		} else if (SqlBuilderType.createSeq==type) {
			this.sql = this.buildCreateSeq();
		} else {
			LangUtils.throwBaseException("unsupported type: " + type);
		}
		return this.sql;
	}

	
	private String buildInsert(){
		Assert.notEmpty(fields);
//		List<String> fields = sqlBuildable.getFieldNames(entityClass);
		String insertSql = "";
		List<String> insertFields = nameToString(fields, false);
		List<String> namedFields = javaNameToNamedString(fields, false);
		insertSql = PARSER.parse(SQL_INSERT, 
				"tableName", tableName, 
				"insertFields", StringUtils.join(insertFields, ", "),
				"namedFields", StringUtils.join(namedFields, ", "));
		if(isDebug())
			LangUtils.println("build insert sql : ${0}", insertSql);
		return insertSql;
	}
	
	private String buildInsertOrIgnore(){
		Assert.notEmpty(fields);
		String insertSql = "";
		List<String> insertFields = nameToString(fields, false);
		List<String> namedFields = javaNameToNamedString(fields, false);
		insertSql = PARSER.parse(SQL_INSERT_OR_IGNORE, 
				"tableName", tableName, 
				"insertFields", StringUtils.join(insertFields, ", "),
				"namedFields", StringUtils.join(namedFields, ", "));
		if(isDebug())
			LangUtils.println("build insert sql : ${0}", insertSql);
		return insertSql;
	}
	
	private String buildInsertOrUpdate(){
		Assert.notEmpty(fields);
//		List<String> fields = sqlBuildable.getFieldNames(entityClass);
		String insertSql = "";
		List<String> insertFields = nameToString(fields, false);
		List<String> namedFields = javaNameToNamedString(fields, false);
		List<String> duplicateKeyUpdateFields = duplicateKeyUpdateSqlString(fields);
		insertSql = PARSER.parse(SQL_INSERT_OR_UPDATE, 
				"tableName", tableName, 
				"insertFields", StringUtils.join(insertFields, ", "),
				"namedFields", StringUtils.join(namedFields, ", "),
				"duplicateKeyUpdateFields", StringUtils.join(duplicateKeyUpdateFields, ", ")
				);
		if(isDebug())
			LangUtils.println("build insertOrUpdate sql : ${0}", insertSql);
		return insertSql;
	}
	
	private String buildUpdate(){
		/*if(LangUtils.isEmpty(fields)){
			throw new DbmException("no update field found!");
		}*/
		if(LangUtils.isEmpty(whereCauseFields)){
			throw new DbmException("no where field found for update!");
		}
//		List<String> fields = sqlBuildable.getFieldNames(entityClass);
		String update = "";
		List<String> updateFields = toWhereString(fields, false);
		List<String> whereColumns = toWhereString(whereCauseFields, false);
		update = PARSER.parse(SQL_UPDATE, 
				"tableName", tableName, 
				"updateFields", StringUtils.join(updateFields, ", "),
				"whereCause", StringUtils.join(whereColumns, " and "));
		if(isDebug())
			LangUtils.println("build update sql : ${0}", update);
		return update;
	}
	
//	private String buildPrimaryKey(){
//		String pkSql = PARSER.parse(SQL_MYSQL_PK, "id", this.identifyField.getColumn().getJavaName(), "tableName", this.tableName);
//		if(isDebug())
//			LangUtils.println("build Primary sql : ${0}", pkSql);
//		return pkSql;
//	}

	private String buildDelete(){
//		Assert.notEmpty(whereCauseColumns);
//		List<String> fields = sqlBuildable.getFieldNames(entityClass);
		String deleteSql = "";

		String deleteTemplate = SQL_DELETE;
//		Assert.notEmpty(whereCauseFields);
		if(LangUtils.isNotEmpty(whereCauseFields)){
			deleteTemplate += " " + SQL_QUERY_CAUSE;
		}
		
		List<String> whereColumns = toWhereString(whereCauseFields, false);
		deleteSql = PARSER.parse(deleteTemplate, 
				"tableName", tableName, 
//				"alias", alias,
				"whereCause", StringUtils.join(whereColumns, " and "));
		if(isDebug())
			LangUtils.println("build delete sql : ${0}", deleteSql);
		return deleteSql;
	}
	
	private String buildQuery(){
//		Assert.notEmpty(columns);
		String selectStr = "*";
		if(!fields.isEmpty()){
			selectStr = StringUtils.join(nameToString(fields, true), ", ");
		}
		String queryTemplate = SQL_QUERY;
//		Assert.notEmpty(whereCauseFields);
		if(LangUtils.isNotEmpty(whereCauseFields)){
			queryTemplate += " " + SQL_QUERY_CAUSE;
		}
//		List<String> fields = sqlBuildable.getFieldNames(entityClass);
		String querySql = "";
		List<String> whereColumns = toWhereString(whereCauseFields, true);
		querySql = PARSER.parse(queryTemplate, 
				"tableName", tableName, 
				"alias", alias, 
				"selectFields", selectStr, 
				"whereCause", StringUtils.join(whereColumns, " and ")
				);
		/*if(this.lock!=null){
			String lockSql = this.dialet.getLockSqlString(lock);
			querySql += " " + lockSql;
		}*/
		if(isDebug())
			LangUtils.println("build query sql : ${0}", querySql);
		return querySql;
	}
	
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	protected List<String> toWhereString(List<DbmMappedFieldValue> columns){
		return toWhereString(columns, true);
	}
	
	protected List<String> toWhereString(Collection<DbmMappedFieldValue> columns, boolean alias){
		List<String> strs = new ArrayList<String>();
//		String namedStr = QMARK;
//		String fstr = null;
//		String op = " = ";
		for(DbmMappedFieldValue fv : columns){
//			DbmMappedField field = fv.getField();
//			fstr = alias?field.getColumn().getNameWithAlias():field.getColumn().getName();
//			fstr = dialet.wrapKeywordColumnName(fstr);
//			if(namedPlaceHoder){
//				namedStr = alias?field.getColumn().getNamedPlaceHolderWithAlias():field.getColumn().getNamedPlaceHolder();
//				strs.add(fstr + op + namedStr);
//			}else{
//				strs.add(fstr + op +namedStr);
//			}
			strs.add(fv.toWhereString(namedPlaceHoder, alias));
		}
		return strs;
	}
	

	protected List<String> nameToString(Collection<DbmMappedFieldValue> columns){
		return this.nameToString(columns, true);
	}
	
	protected List<String> nameToString(Collection<DbmMappedFieldValue> columns, boolean alias){
//		List<String> strs = new ArrayList<String>();
//		for(DbmMappedFieldValue fv : columns){
//			DbmMappedField field = fv.getField();
//			String columnName = alias?field.getColumn().getNameWithAlias():field.getColumn().getName();
//			columnName = dialet.wrapKeywordColumnName(columnName);
//			strs.add(columnName);
//		}
//		return strs;
		if (alias) {
			return fieldNameToString(columns, getAlias());
		} else {
			return fieldNameToString(columns, null);
		}
		
		
	}
	
	public List<String> fieldNameToString(String alias){
		return fieldNameToString(this.fields, alias);
	}
	
	protected List<String> fieldNameToString(Collection<DbmMappedFieldValue> columns, String alias){
//		Assert.hasText(alias, "table alias can not be blank");
		List<String> strs = new ArrayList<String>();
		for(DbmMappedFieldValue fv : columns){
			DbmMappedField field = fv.getField();
			String columnName = field.getColumn().getName();
			if (StringUtils.isNotBlank(alias)) {
				columnName = alias + "." + columnName;
			}
			columnName = dialet.wrapKeywordColumnName(columnName);
			strs.add(columnName);
		}
		return strs;
	}
	
	protected List<String> javaNameToString(Collection<BaseColumnInfo> columns, String alias){
		List<String> strs = new ArrayList<String>();
		for(BaseColumnInfo field : columns){
			strs.add(field.getJavaNameWithAlias());
		}
		return strs;
	}
	
	/***
	 * :name
	 * @author weishao zeng
	 * @param columns
	 * @param alias
	 * @return
	 */
	protected List<String> javaNameToNamedString(Collection<DbmMappedFieldValue> columns, boolean alias){
		List<String> strs = new ArrayList<String>();
		for(DbmMappedFieldValue fv : columns){
			DbmMappedField field = fv.getField();
			if(namedPlaceHoder)
				strs.add(alias?field.getColumn().getNamedPlaceHolderWithAlias():field.getColumn().getNamedPlaceHolder());
			else
				strs.add(QMARK);
		}
		return strs;
	}

	/***
	 * ON DUPLICATE KEY UPDATE  field =  VALUES(field);
	 * 
	 * @author weishao zeng
	 * @param columns
	 * @return
	 */
	protected List<String> duplicateKeyUpdateSqlString(Collection<DbmMappedFieldValue> columns){
		List<String> strs = new ArrayList<String>();
		for(DbmMappedFieldValue fv : columns){
			DbmMappedField field = fv.getField();
			// 忽略主键
			if (field.isIdentify()) {
				continue ;
			}
			String columnName = dialet.wrapKeywordColumnName(field.getColumn().getName());
			String duplicateKeyUpdateSql = columnName + " = values( " + columnName + " )";
			strs.add(duplicateKeyUpdateSql);
//			strs.add(field.getColumn().getDuplicateKeyUpdateSql());
		}
		return strs;
	}
	/*public DbmMappedField getIdentifyField() {
		return identifyField;
	}*/
	/* (non-Javadoc)
	 * @see org.onetwo.common.jfishdbm.mapping.EntrySQLBuilder#getSql()
	 */
	@Override
	public String getSql() {
		return sql;
	}

	/* (non-Javadoc)
	 * @see org.onetwo.common.jfishdbm.mapping.EntrySQLBuilder#getType()
	 */
	@Override
	public SqlBuilderType getType() {
		return type;
	}

	public List<DbmMappedFieldValue> getFields() {
		return fields;
	}

	public List<DbmMappedFieldValue> getWhereCauseFields() {
		return whereCauseFields;
	}

	public boolean isNamedPlaceHoder() {
		return namedPlaceHoder;
	}

	public void setNamedPlaceHoder(boolean namedPlaceHoder) {
		this.namedPlaceHoder = namedPlaceHoder;
	}
	
	public Object getVersionValue(Object[] updateValues){
		int valueIndex = -1;
		for (int i = 0; i < fields.size(); i++) {
			if (fields.get(i).getField().equals(entry.getVersionField())) {
				valueIndex = i;
			}
		}
		if (valueIndex==-1) {
			throw new DbmException("version field not found on entry: " + entry.getEntityName());
		}
		return updateValues[valueIndex];
	}

	public DbmMappedEntryMeta getEntry() {
		return entry;
	}

	/*public void setLock(LockInfo lock) {
		this.lock = lock;
	}*/
	
}
