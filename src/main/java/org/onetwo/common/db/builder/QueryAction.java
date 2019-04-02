package org.onetwo.common.db.builder;

import java.util.List;
import java.util.Optional;

import org.onetwo.common.utils.Page;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

public interface QueryAction {

//	ExtQuery build(Class<?> entityClass, String alias, Map<Object, Object> properties);
	
	/***
	 * 查找唯一结果，如果找不到则返回null，找到多个则抛异常 IncorrectResultSizeDataAccessException，详见：DataAccessUtils.requiredSingleResult
	 * @author weishao zeng
	 * @return
	 */
	<T> T unique();
	
	<T> T one();
	
	default public <T> Optional<T> optionalOne() {
		return Optional.ofNullable(one());
	}
	
	Number count();

	<T> List<T> list();
	
	<T> List<T> listAs(Class<T> toClass);
	
	<T> Page<T> page(Page<T> page);
	
	<T> T extractAs(ResultSetExtractor<T> rse);
	
	<T> List<T> listWith(RowMapper<T> rowMapper);
}
