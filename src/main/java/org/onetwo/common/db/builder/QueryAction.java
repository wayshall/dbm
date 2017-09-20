package org.onetwo.common.db.builder;

import java.util.List;

import org.onetwo.common.utils.Page;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

public interface QueryAction {

//	ExtQuery build(Class<?> entityClass, String alias, Map<Object, Object> properties);
	
	<T> T unique();
	
	<T> T one();

	<T> List<T> list();
	
	<T> List<T> listAs(Class<T> toClass);
	
	<T> Page<T> page(Page<T> page);
	
	<T> T extractAs(ResultSetExtractor<T> rse);
	
	<T> List<T> listWith(RowMapper<T> rowMapper);
}
