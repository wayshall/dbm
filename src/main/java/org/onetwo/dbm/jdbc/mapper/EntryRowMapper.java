package org.onetwo.dbm.jdbc.mapper;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.spi.JdbcResultSetGetter;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

@SuppressWarnings("unchecked")
public class EntryRowMapper<T> extends DbmBeanPropertyRowMapper<T> implements RowMapper<T>{
	
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	private DbmMappedEntry entry;
	private boolean debug = false;
//	private DbmTypeMapping sqlTypeMapping;
//	private JdbcResultSetGetter jdbcResultSetGetter;
	private boolean useSmartProperty;
	
	public EntryRowMapper(DbmMappedEntry entry, JdbcResultSetGetter jdbcResultSetGetter) {
		this(entry, jdbcResultSetGetter, false);
	}
	
	public EntryRowMapper(DbmMappedEntry entry, JdbcResultSetGetter jdbcResultSetGetter, boolean useSmartProperty) {
		super(jdbcResultSetGetter, (Class<T>)entry.getEntityClass());
		this.entry = entry;
		this.useSmartProperty = useSmartProperty;
//		this.jdbcResultSetGetter = jdbcResultSetGetter;
	}


	public EntryRowMapper(DbmMappedEntry entry, JdbcResultSetGetter jdbcResultSetGetter, boolean useSmartProperty, boolean debug) {
		this(entry, jdbcResultSetGetter, useSmartProperty);
		this.debug = debug;
//		this.jdbcResultSetGetter = jdbcResultSetGetter;
	}


	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		return (T)mapRowWithBeanWrapper(rs, rowNum).getWrappedInstance();
	}
	
	@Override
	public BeanWrapper mapRowWithBeanWrapper(ResultSet rs, int rowNumber) throws SQLException {
		Assert.state(entry!=null, "no mapping entry!");

		ResultSetWrappingSqlRowSet resutSetWrapper = new ResultSetWrappingSqlRowSet(rs);
		SqlRowSetMetaData rsmd = resutSetWrapper.getMetaData();
		int columnCount = resutSetWrapper.getMetaData().getColumnCount();

//		DbmMappedField field;
		String column = null;
		T entity = entry.newInstance();
//		Object value = null;
		BeanWrapper bw = this.createBeanWrapper(entity);
		
		long start = 0;
		if(debug){
			start = System.currentTimeMillis();
		}
		for (int index = 1; index <= columnCount; index++) {
//			value = null;
			column = DbmUtils.lookupColumnName(rsmd, index);
			/*if(!entry.containsColumn(column))
				continue;*/
			this.setColumnValue(resutSetWrapper, bw, rowNumber, index, column);
			/*try {
				if (entry.containsColumn(column)) {
					field = entry.getFieldByColumnName(column);
					value = getColumnValue(resutSetWrapper, index, field);
					if(value!=null){
						field.setValue(entity, value);
					}
				} else if (useSmartProperty) {
//					if(bw==null){
//						bw = this.createBeanWrapper(entity);
//					}
					this.setValue(resutSetWrapper, bw, rowNum, index, column);
				}
			} catch (Exception e) {
				throw new DbmException(entry.getEntityClass() + " mapped field["+column+", "+value+"] error : " + e.getMessage(), e);
			}*/
		}	
		
		if(debug){
			long costTime = System.currentTimeMillis()-start;
			logger.info("===>>> mapp row cost time (milliseconds): " + costTime);
		}
		
		return bw;
	}
	
	public void setColumnValue(ResultSetWrappingSqlRowSet resutSetWrapper, 
			BeanWrapper bw, 
			int rowNumber, 
			int columnIndex, 
			String column) {
		DbmMappedField field;
		Object value = null;
		Object entity = bw.getWrappedInstance();
		try {
			if (entry.containsColumn(column)) {
				field = entry.getFieldByColumnName(column);
				value = getColumnValue(resutSetWrapper, columnIndex, field);
				if(value!=null){
					field.setValue(entity, value);
				}
			} else if (useSmartProperty) {
//				if(bw==null){
//					bw = this.createBeanWrapper(entity);
//				}
				super.setColumnValue(resutSetWrapper, bw, rowNumber, columnIndex, column);
			}
		} catch (Exception e) {
			throw new DbmException(entry.getEntityClass() + " mapped field["+column+", "+value+"] error : " + e.getMessage(), e);
		}
	}
	
	protected String toPropertyName(final String column){
		/*String propName = column;
		if(propName.contains("___")){
			propName = propName.replace("___", ".");
		}
		propName = StringUtils.toCamel(propName, false);*/
		return StringUtils.toCamel(column, false);
	}
	
	public DbmMappedEntry getEntry() {
		return entry;
	}

	protected Object getColumnValue(ResultSetWrappingSqlRowSet rs, int index, PropertyDescriptor pd, int sqlType) throws SQLException {
//		return jdbcResultSetGetter.getColumnValue(rs, index, pd);
		return jdbcResultSetGetter.getColumnValue(rs, index, pd.getPropertyType());
		/*JFishProperty jproperty = Intro.wrap(pd.getWriteMethod().getDeclaringClass()).getJFishProperty(pd.getName(), false);
		TypeHandler<?> typeHandler = sqlTypeMapping.getTypeHander(jproperty.getType(), sqlType);
		Object value = typeHandler.getResult(rs, index);
		return value;*/
	}

	protected Object getColumnValue(ResultSetWrappingSqlRowSet rs, int index, DbmMappedField field) throws SQLException {
		return jdbcResultSetGetter.getColumnValue(rs, index, field);
		/*TypeHandler<?> typeHandler = sqlTypeMapping.getTypeHander(field.getColumnType(), field.getColumn().getSqlType());
		Object value = typeHandler.getResult(rs, index);
		return value;*/
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entry == null) ? 0 : entry.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntryRowMapper<?> other = (EntryRowMapper<?>) obj;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
			return false;
		return true;
	}
	
	
}
