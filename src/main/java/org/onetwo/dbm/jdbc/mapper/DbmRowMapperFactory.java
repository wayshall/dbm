package org.onetwo.dbm.jdbc.mapper;

import java.util.concurrent.ExecutionException;

import org.onetwo.common.db.dquery.DynamicMethod;
import org.onetwo.common.db.dquery.NamedQueryInvokeContext;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.Assert;
import org.onetwo.dbm.annotation.DbmResultMapping;
import org.onetwo.dbm.annotation.DbmRowMapper;
import org.onetwo.dbm.annotation.DbmRowMapper.MappingModes;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.mapper.DataRowMapper.NoDataRowMapper;
import org.onetwo.dbm.jdbc.spi.JdbcResultSetGetter;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.MappedEntryManager;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class DbmRowMapperFactory extends JdbcDaoRowMapperFactory {

	private LoadingCache<Class<?>, DataRowMapper<?>> beanPropertyRowMapperCache = CacheBuilder.newBuilder()
																						.build(new CacheLoader<Class<?>, DataRowMapper<?>>(){

																							@Override
																							public DataRowMapper<?> load(Class<?> type)
																									throws Exception {
																								return getBeanPropertyRowMapper0(type);
																							}
																							
																						});
	private MappedEntryManager mappedEntryManager;
	private JdbcResultSetGetter jdbcResultSetGetter;
	
	public DbmRowMapperFactory(MappedEntryManager mappedEntryManager, JdbcResultSetGetter jdbcResultSetGetter) {
		super();
		this.mappedEntryManager = mappedEntryManager;
		this.jdbcResultSetGetter = jdbcResultSetGetter;
	}

	public MappedEntryManager getMappedEntryManager() {
		return mappedEntryManager;
	}

	public void setMappedEntryManager(MappedEntryManager mappedEntryManager) {
		this.mappedEntryManager = mappedEntryManager;
	}

	protected DataRowMapper<?> getBeanPropertyRowMapper(Class<?> type) {
		try {
			return beanPropertyRowMapperCache.get(type);
		} catch (ExecutionException e) {
			throw new DbmException("no BeanPropertyRowMapper found for type:"+type);
		}
	}
//	@SuppressWarnings("unchecked")
	protected DataRowMapper<?> getBeanPropertyRowMapper0(Class<?> type) {
		DataRowMapper<?> rowMapper = null;
		if(AnnotationUtils.findAnnotation(type, DbmRowMapper.class)!=null){
			DbmRowMapper dbmRowMapper = AnnotationUtils.findAnnotation(type, DbmRowMapper.class);
			/*if(dbmRowMapper.value()==Void.class){
				return new DbmBeanPropertyRowMapper<>(this.jdbcResultSetGetter,  type);
			}else */
			if(dbmRowMapper.value()!=NoDataRowMapper.class){
				Assert.isAssignable(RowMapper.class, dbmRowMapper.value());
				Class<? extends DataRowMapper<?>> rowMapperClass = (Class<? extends DataRowMapper<?>>)dbmRowMapper.value();
				return ReflectUtils.newInstance(rowMapperClass, type);
			}else if(dbmRowMapper.mappingMode()==MappingModes.ENTITY){
				DbmMappedEntry entry = this.getMappedEntryManager().getReadOnlyEntry(type);
				rowMapper = new EntryRowMapper<>(entry, this.jdbcResultSetGetter);
				return rowMapper;
			}else if(dbmRowMapper.mappingMode()==MappingModes.SMART_PROPERTY){
				rowMapper = new DbmBeanPropertyRowMapper<>(this.jdbcResultSetGetter, type);
				return rowMapper;
			}else if(dbmRowMapper.mappingMode()==MappingModes.MIXTURE){
				DbmMappedEntry entry = this.getMappedEntryManager().getReadOnlyEntry(type);
				rowMapper = new EntryRowMapper<>(entry, this.jdbcResultSetGetter, true);
				return rowMapper;
			}
		} else if (getMappedEntryManager().isSupportedMappedEntry(type)){
			DbmMappedEntry entry = this.getMappedEntryManager().getEntry(type);
			rowMapper = new EntryRowMapper<>(entry, this.jdbcResultSetGetter);
			return rowMapper;
		} else{
//			rowMapper = super.getBeanPropertyRowMapper(type);
			rowMapper = new DbmBeanPropertyRowMapper<>(this.jdbcResultSetGetter,  type);
		}
		return rowMapper;
	}
	
	@Override
	public DataRowMapper<?> createRowMapper(NamedQueryInvokeContext invokeContext) {
		DynamicMethod dmethod = invokeContext.getDynamicMethod();
		if(!dmethod.isAnnotationPresent(DbmResultMapping.class)){
			return super.createRowMapper(invokeContext);
		}
		DbmResultMapping dbmCascadeResult = dmethod.getMethod().getAnnotation(DbmResultMapping.class);
		DbmNestedBeanRowMapper<?> rowMapper = new DbmNestedBeanRowMapper<>(this, invokeContext.getResultComponentClass(), dbmCascadeResult);
		return rowMapper;
	}

	public JdbcResultSetGetter getJdbcResultSetGetter() {
		return jdbcResultSetGetter;
	}
	
}
