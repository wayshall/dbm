package org.onetwo.common.db.dquery.repostory;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.onetwo.common.db.DataBase;
import org.onetwo.common.db.dquery.DbmSqlFileResource;
import org.onetwo.common.db.dquery.DynamicMethod;
import org.onetwo.common.db.dquery.DynamicQueryHandlerProxyCreator;
import org.onetwo.common.db.filequery.SpringBasedSqlFileScanner;
import org.onetwo.common.db.spi.SqlFileScanner;
import org.onetwo.common.spring.utils.SpringResourceAdapterImpl;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.JdbcUtils;
import org.springframework.util.ClassUtils;

import com.google.common.cache.LoadingCache;

public class AnnotationDynamicQueryHandlerProxyCreator extends DynamicQueryHandlerProxyCreator {

	private SqlFileScanner sqlFileScanner = new SpringBasedSqlFileScanner(ClassUtils.getDefaultClassLoader());
	
	public AnnotationDynamicQueryHandlerProxyCreator(Class<?> interfaceClass, DbmRepositoryAttrs dbmRepositoryAttrs, LoadingCache<Method, DynamicMethod> methodCache) {
		super(interfaceClass, dbmRepositoryAttrs, methodCache);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected DbmSqlFileResource<?> getSqlFile(DataSource dataSource) {
		Class<?> repostoryClass = this.interfaceClass;
		DataBase database = JdbcUtils.getDataBase(dataSource);
		String filePath = repostoryClass.getName();
		SpringResourceAdapterImpl sqlRes = getClassPathResource(database, filePath, false);
		if(sqlRes==null){
			filePath = ClassUtils.convertClassNameToResourcePath(repostoryClass.getName());
			sqlRes = getClassPathResource(database, filePath, true);
		}
		
//		return sqlRes;
		return new DbmSqlFileResource(sqlRes, interfaceClass, database);
	}
	
	/***
	 * 先根据数据库查找，如果没找到，则默认查找
	 * @param database
	 * @param filePath
	 * @param throwIfNotfound
	 * @return
	 */
	private SpringResourceAdapterImpl getClassPathResource(DataBase database, String filePath, boolean throwIfNotfound){
		SpringResourceAdapterImpl sqlRes = null;
		if(database!=null){
			sqlRes = sqlFileScanner.getClassPathResource(database.getName(), filePath);
		}
		if(!sqlRes.exists()){
			sqlRes = sqlFileScanner.getClassPathResource(null, filePath);
			if(!sqlRes.exists() && throwIfNotfound){
				throw new DbmException("no sql file found for repostory: " + filePath);
			}
		}
		return sqlRes;
	}

}
