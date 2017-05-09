package org.onetwo.common.db.filequery;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import org.onetwo.common.db.dquery.DbmSqlFileResource;
import org.onetwo.common.db.dquery.annotation.Query;
import org.onetwo.common.db.spi.NamedQueryFile;
import org.onetwo.common.db.spi.NamedQueryInfo;
import org.onetwo.common.db.spi.NamedQueryInfoParser;
import org.onetwo.common.db.spi.QueryConfigData;
import org.onetwo.common.db.spi.QueryContextVariable;
import org.onetwo.common.propconf.ResourceAdapter;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.StringUtils;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * @Query override file
 * @author wayshall
 * <br/>
 */
public class AnnotationBasedQueryInfoParser implements NamedQueryInfoParser {

	@Override
	public void parseToNamedQueryFile(NamedQueryFile namedQueryFile, ResourceAdapter<?> file) {
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
			NamedQueryInfo info = namedQueryFile.getNamedProperty(method.getName());
			if(info==null){
				info = new NamedQueryInfo();
				info.setName(method.getName());
				info.setDbmNamedQueryFile(namedQueryFile);
				namedQueryFile.put(info.getName(), info, true);
			}
			if(StringUtils.isNotBlank(query.value())){
				info.setSql(query.value());
			}
			if(StringUtils.isNotBlank(query.countQuery())){
				info.setCountSql(query.countQuery());
			}
			info.setParserType(query.parser());
			
			QueryConfigData config = new QueryConfigData();
			config.setLikeQueryFields(Arrays.asList(query.likeQueryFields()));
			if(query.funcClass()==ParserContextFunctionSet.class){
				config.setVariables(ParserContextFunctionSet.getInstance());
			}else{
				QueryContextVariable func = (QueryContextVariable)ReflectUtils.newInstance(query.funcClass());
				config.setVariables(ParserContextFunctionSet.getInstance(), func);
			}
			info.setQueryConfig(config);
		}
	}
	
	

}
