package org.onetwo.common.db.filequery;

import java.lang.reflect.Method;
import java.util.Set;

import org.onetwo.common.db.dquery.DbmSqlFileResource;
import org.onetwo.common.db.dquery.annotation.Query;
import org.onetwo.common.db.filequery.spi.DbmNamedQueryInfoParser;
import org.onetwo.common.propconf.ResourceAdapter;
import org.onetwo.common.utils.StringUtils;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * @author wayshall
 * <br/>
 */
public class AnnotationBasedQueryInfoParser implements DbmNamedQueryInfoParser {

	@Override
	public void parseToNamedQueryFile(DbmNamedQueryFile namedQueryFile, ResourceAdapter<?> file) {
		if(!DbmSqlFileResource.class.isInstance(file)){
			return ;
		}
		DbmSqlFileResource<?> dbmSqlFile = (DbmSqlFileResource<?>) file;
		Class<?> interfaceClass = dbmSqlFile.getMappedInterface();
		Set<Method> methods = MethodIntrospector.selectMethods(interfaceClass, (MethodFilter)method->{
			return AnnotationUtils.findAnnotation(method, Query.class)!=null;
		});
		
		if(methods.isEmpty()){
			return ;
		}
		for(Method method : methods){
			Query query = AnnotationUtils.findAnnotation(method, Query.class);
			DbmNamedQueryInfo info = new DbmNamedQueryInfo();
			info.setName(method.getName());
			info.setSql(query.value());
			if(StringUtils.isNotBlank(query.countQuery())){
				info.setCountSql(query.countQuery());
			}
			info.setDbmNamedQueryFile(namedQueryFile);
			info.setParserType(query.parser());
			namedQueryFile.put(info.getName(), info, false);
		}
	}
	
	

}
