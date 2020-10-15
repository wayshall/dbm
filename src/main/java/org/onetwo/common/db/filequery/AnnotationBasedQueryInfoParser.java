package org.onetwo.common.db.filequery;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.onetwo.common.db.dquery.DbmSqlFileResource;
import org.onetwo.common.db.dquery.annotation.Query;
import org.onetwo.common.db.spi.NamedQueryFile;
import org.onetwo.common.db.spi.NamedQueryInfoParser;
import org.onetwo.common.db.spi.QueryConfigData;
import org.onetwo.common.db.spi.QueryContextVariable;
import org.onetwo.common.propconf.ResourceAdapter;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.utils.DbmUtils;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * @Query override file
 * @author wayshall
 * <br/>
 */
public class AnnotationBasedQueryInfoParser implements NamedQueryInfoParser {

	protected Set<Method> selectMethods(NamedQueryFile namedQueryFile, ResourceAdapter<?> file){
		if(!DbmSqlFileResource.class.isInstance(file)){
			return Collections.emptySet();
		}
		DbmSqlFileResource<?> dbmSqlFile = (DbmSqlFileResource<?>) file;
		Class<?> interfaceClass = dbmSqlFile.getMappedInterface();
		Set<Method> methods = MethodIntrospector.selectMethods(interfaceClass, (MethodFilter)method->{
			return AnnotationUtils.findAnnotation(method, Query.class)!=null;
		});
		
		return methods;
	}
	
	@Override
	public void parseToNamedQueryFile(NamedQueryFile namedQueryFile, ResourceAdapter<?> file) {
		Set<Method> methods = selectMethods(namedQueryFile, file);
		
		if(methods.isEmpty()){
			return ;
		}
		for(Method method : methods){
			FileBaseNamedQueryInfo info = getOrCreateNamedQueryInfo(namedQueryFile, method);
			processQueryConfig(info, method);
		}
	}
	
	protected FileBaseNamedQueryInfo getOrCreateNamedQueryInfo(NamedQueryFile namedQueryFile, Method method){
		FileBaseNamedQueryInfo info = namedQueryFile.getNamedProperty(method.getName());
		if(info==null){
			info = new FileBaseNamedQueryInfo();
			info.setName(method.getName());
			info.setDbmNamedQueryFile(namedQueryFile);
			namedQueryFile.put(info.getName(), info, true);
		}
		return info;
	}
	
	protected void processQueryConfig(FileBaseNamedQueryInfo info, Method method){
		Query query = AnnotationUtils.findAnnotation(method, Query.class);
		if(StringUtils.isNotBlank(query.value())){
			info.setSql(query.value());
		}
		if(StringUtils.isNotBlank(query.countQuery())){
			info.setCountSql(query.countQuery());
		}
		info.setParserType(query.parser());
		
		QueryConfigData config = info.getQueryConfig();
		if (config==null) {
			config = new QueryConfigData();
			info.setQueryConfig(config);
		}
		config.setLikeQueryFields(Arrays.asList(query.likeQueryFields()));
		
		Set<QueryContextVariable> variables = new LinkedHashSet<>();
		
		if (!LangUtils.isEmpty(config.getVariables())) {
			Stream.of(config.getVariables()).forEach(v -> variables.add(v));
		}
		
		if(query.funcClass()==ParserContextFunctionSet.class){
			variables.add(ParserContextFunctionSet.getInstance());
		}else{
//			QueryContextVariable func = (QueryContextVariable)ReflectUtils.newInstance(query.funcClass());
			variables.add(ParserContextFunctionSet.getInstance());
			QueryContextVariable func = DbmUtils.createDbmBean(query.funcClass());
			variables.add(func);
		}
		config.setVariables(variables.toArray(new QueryContextVariable[0]));
	}
	

}
