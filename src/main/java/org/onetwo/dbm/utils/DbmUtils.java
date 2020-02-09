package org.onetwo.dbm.utils;

import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.spring.Springs;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.annotation.DbmFieldListeners;
import org.onetwo.dbm.core.spi.DbmTransaction;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.exception.UpdateCountException;
import org.onetwo.dbm.jdbc.JdbcUtils;
import org.onetwo.dbm.jdbc.spi.SqlParametersProvider;
import org.onetwo.dbm.mapping.DbmEntityFieldListener;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.onetwo.dbm.spring.EnableDbm;
import org.slf4j.Logger;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.google.common.collect.Lists;

final public class DbmUtils {
	
	private static final Logger logger = JFishLoggerFactory.getLogger(DbmUtils.class);
	
	public final static ConversionService CONVERSION_SERVICE = new DefaultConversionService();
	
	private static final String CHAINED_TRANSACTION_MANAGER = "org.springframework.data.transaction.ChainedTransactionManager";
	
	public static boolean isChanedTransactionManagerPresent(){
		return ClassUtils.isPresent(CHAINED_TRANSACTION_MANAGER, ClassUtils.getDefaultClassLoader());
	}
	public static void throwIfEffectiveCountError(String errorMsg, int expectCount, int effectiveCount){
		if(effectiveCount!=expectCount)
			throw new UpdateCountException(errorMsg, expectCount, effectiveCount);
	}
	
	public static List<DbmEntityFieldListener> initDbmEntityFieldListeners(DbmFieldListeners listenersAnntation){
		Assert.notNull(listenersAnntation, "listenersAnntationn can not be null");
		Class<? extends DbmEntityFieldListener>[] flClasses = listenersAnntation.value();
		List<DbmEntityFieldListener> fieldListeners = Lists.newArrayList();
		for(Class<? extends DbmEntityFieldListener> flClass : flClasses){
			DbmEntityFieldListener fl = DbmUtils.createDbmBean(flClass); // ReflectUtils.newInstance(flClass);
			fieldListeners.add(fl);
		}
		return fieldListeners;
	}

	/*public static Object getActualSqlValue(Object value){
		if(value!=null && Enum.class.isAssignableFrom(value.getClass())){
//			return Types.asValue(value.toString(), value.getClass());
			return ((Enum<?>)value).name();
		}
		return value;
	}*/


	public static Map<String, Integer> lookupColumnNames(SqlRowSetMetaData resultSetMetaData) throws SQLException {
		int columnCount = resultSetMetaData.getColumnCount();
		Map<String, Integer> names = new HashMap<String, Integer>();
		for (int index = 1; index <= columnCount; index++) {
			String columName = lookupColumnName(resultSetMetaData, index);
			names.put(JdbcUtils.lowerCaseName(columName), index);
			names.put(columName, index);
		}
		return names;
	}

	public static String lookupColumnName(SqlRowSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (name == null || name.length() < 1) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}
	
	
	public static Collection<String> getAllDbmPackageNames(ApplicationContext applicationContext){
		ListableBeanFactory bf = (ListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
		return getAllDbmPackageNames(bf);
	}
	public static Collection<String> getAllDbmPackageNames(ListableBeanFactory beanFactory){
		Collection<String> packageNames = new HashSet<>();
		packageNames.addAll(scanEnableDbmPackages(beanFactory));
//		packageNames.addAll(scanDbmPackages(beanFactory));
		return packageNames;
	}
	
	public static List<String> scanEnableDbmPackages(ApplicationContext applicationContext){
		ListableBeanFactory bf = (ListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
		return scanEnableDbmPackages(bf);
	}
	public static List<String> scanEnableDbmPackages(ListableBeanFactory beanFactory){
		List<String> packageNames = new ArrayList<String>();
		SpringUtils.scanAnnotation(beanFactory, EnableDbm.class, (beanDef, beanClass)->{
			EnableDbm enableDbm = beanClass.getAnnotation(EnableDbm.class);
			if(enableDbm==null){
				return ;
			}
			String[] modelPacks = enableDbm.packagesToScan();
			if(ArrayUtils.isNotEmpty(modelPacks)){
				for(String pack : modelPacks){
					packageNames.add(pack);
				}
			}else{
				String packageName = beanClass.getPackage().getName();
				if(!packageName.startsWith("org.onetwo.")){
					packageNames.add(packageName);
				}
			}
		});
		return packageNames;
	}
	
	/*public static List<String> scanDbmPackages(ApplicationContext applicationContext){
		ListableBeanFactory bf = (ListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
		return scanDbmPackages(bf);
	}
	public static List<String> scanDbmPackages(ListableBeanFactory beanFactory){
		return scanAnnotationPackages(beanFactory, DbmPackages.class, dbmPackages->dbmPackages.value());
	}*/
	public static <T extends Annotation> List<String> scanAnnotationPackages(ListableBeanFactory beanFactory, 
																		Class<T> annoClass,
																		Function<T, String[]> packageExtractor){
		List<String> packageNames = new ArrayList<String>();
		SpringUtils.scanAnnotation(beanFactory, annoClass, (beanDef, beanClass)->{
			T annoInst = beanClass.getAnnotation(annoClass);
			if(annoInst==null){
				return ;
			}
			String[] modelPacks = packageExtractor.apply(annoInst);
			if(ArrayUtils.isNotEmpty(modelPacks)){
				for(String pack : modelPacks){
					packageNames.add(pack);
				}
			}else{
				String packageName = beanClass.getPackage().getName();
				packageNames.add(packageName);
			}
		});
		return packageNames;
	}
	

	public static void rollbackOnException(DbmTransaction transaction, Throwable ex) throws TransactionException {
		logger.debug("Initiating transaction rollback on application exception", ex);
		try {
			transaction.rollback();
		}
		catch (TransactionSystemException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			ex2.initApplicationException(ex);
			throw ex2;
		}
		catch (RuntimeException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			throw ex2;
		}
		catch (Error err) {
			logger.error("Application exception overridden by rollback error", ex);
			throw err;
		}
	}
	

	/*****
	 * find the {@link PlatformTransactionManager} from {@link ApplicationContext} by {@link DataSource}
	 * @param applicationContext
	 * @param dataSource
	 * @return
	 */
	public static PlatformTransactionManager getDataSourceTransactionManager(ApplicationContext applicationContext, DataSource dataSource, Supplier<PlatformTransactionManager> notFoudCallback){
		if(DbmUtils.isChanedTransactionManagerPresent()){
			List<ChainedTransactionManager> chaineds = SpringUtils.getBeans(applicationContext, ChainedTransactionManager.class);
			for(ChainedTransactionManager chain : chaineds){
				if(isChanedTMContainDataSource(chain, dataSource)){
					return chain;
				}
			}
		}
		Map<String, DataSourceTransactionManager> tms = SpringUtils.getBeansAsMap(applicationContext, DataSourceTransactionManager.class);
		Entry<String, DataSourceTransactionManager> tm = null;
		for(Entry<String, DataSourceTransactionManager> entry : tms.entrySet()){
			if(isSameDataSource(entry.getValue(), dataSource)){
				tm = entry;
				break;
			}
		}
		if(tm!=null){
			if(logger.isDebugEnabled()){
				logger.debug("auto find DataSourceTransactionManager for current dataSource: {}", tm.getKey());
			}
			return tm.getValue();
		}else{
			if(notFoudCallback!=null){
				return notFoudCallback.get();
			}
			throw new DbmException("no DataSourceTransactionManager configurate for dataSource: " + dataSource);
		}
	}
	
	public static boolean isSameDataSource(DataSourceTransactionManager tm, DataSource dataSource){
		return tm.getDataSource().equals(dataSource);
	}
	
	@SuppressWarnings("unchecked")
	public static boolean isChanedTMContainDataSource(ChainedTransactionManager chained, DataSource dataSource){
		List<PlatformTransactionManager> lists = (List<PlatformTransactionManager>)ReflectUtils.getFieldValue(chained, "transactionManagers");
		if(LangUtils.isEmpty(lists)){
			return false;
		}
		for(PlatformTransactionManager tm : lists){
			if(!DataSourceTransactionManager.class.isInstance(tm)){
				continue;
			}
			DataSourceTransactionManager dtm = (DataSourceTransactionManager)tm;
			if(isSameDataSource(dtm, dataSource)){
				return true;
			}
		}
		return false;
	}
	

	public static Pair<String, Object> findSqlAndParams(Object[] args){
		String sql = null;
		Object params = null;
		int maxArgSize = 50;
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if(arg==null){
				continue;
			}
			if(arg instanceof String){
				sql = (String)arg;
			}else if(arg instanceof Map){
				params = arg;
			}else if(arg.getClass().isArray()){
				params = CUtils.tolist(arg, false);
			}else if(arg instanceof Collection){//batch operation...
				params = arg;
			}else{
				//if arg is SimpleArgsPreparedStatementCreator
				if(arg instanceof SqlProvider){
					sql = ((SqlProvider)arg).getSql();
				}
				if(arg instanceof SqlParametersProvider){
					params = ((SqlParametersProvider)arg).getSqlParameterList();
				}
			}
			if (LangUtils.size(params)>maxArgSize) {
				params = "<<Parameter Size is more than " + maxArgSize + ">>";
			}
		}
		if(sql==null){
			return null;
		}
		return Pair.of(sql, params);
	}
	

	/***
	 * 
	 * @see SqlParameterSourceUtils#createBatch
	 * 
	 * @author weishao zeng
	 * @param valueMaps
	 * @return
	 */
	public static SqlParameterSource[] createBatch(List<Map<String, ?>> valueMaps) {
		int size = valueMaps.size();
		MapSqlParameterSource[] batch = new MapSqlParameterSource[size];
		for (int i = 0; i < size; i++) {
			batch[i] = new MapSqlParameterSource(valueMaps.get(i));
		}
		return batch;
	}
	
	public static <T> T createDbmBean(Class<T> clazz) {
		T bean;
		if (Springs.getInstance().isInitialized()) {
			bean = Springs.getInstance().getBean(clazz);
			// 如果找不到则自动创建，并注入
			if (bean==null) {
				bean = ReflectUtils.newInstance(clazz);
				Springs.getInstance().autoInject(bean);
			}
		} else {
			bean = ReflectUtils.newInstance(clazz);
		}
		return bean;
	}
	
	public static SqlParameterValue convert2SqlParameterValue(DbmMappedField field, Object value){
		return new DbmSqlParameterValue(field.getColumn().getSqlType(), value);
	}
	
	public static Object convertFromSqlParameterValue(DbmMappedField field, Object sqlParametableValue){
		Object val = sqlParametableValue;
		if (sqlParametableValue instanceof SqlParameterValue) {
			SqlParameterValue spv = (SqlParameterValue) sqlParametableValue;
			val = spv.getValue();
		}
		return val;
	}
	
	public static class DbmSqlParameterValue extends SqlParameterValue {

		public DbmSqlParameterValue(int sqlType, Object value) {
			super(sqlType, value);
		}
		
		public String toString() {
			return getValue()==null?"NULL":getValue().toString();
		}
	}
	
	private DbmUtils(){
	}
}
