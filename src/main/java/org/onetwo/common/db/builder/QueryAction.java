package org.onetwo.common.db.builder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.onetwo.common.utils.Page;
import org.onetwo.dbm.jdbc.DbmMapRowMapperResultSetExtractor;
import org.onetwo.dbm.jdbc.SimpleMapRowMapperResultSetExtractor;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

public interface QueryAction<E> {

//	ExtQuery build(Class<?> entityClass, String alias, Map<Object, Object> properties);
	
	/***
	 * 查找唯一结果，如果找不到则返回null，找到多个则抛异常 IncorrectResultSizeDataAccessException，详见：DataAccessUtils.requiredSingleResult
	 * @author weishao zeng
	 * @return
	 */
	E unique();
	
	E one();
	
	/****
	 * 查询数据是否存在
	 * @author weishao zeng
	 * @return
	 */
	default public boolean exist() {
		return optionalOne().isPresent();
	}
	
	default public Optional<E> optionalOne() {
		return Optional.ofNullable(one());
	}
	
	Number count();

	List<E> list();
	
	<T> List<T> listAs(Class<T> toClass);
	
	Page<E> page(Page<E> page);
	
	<T> T extractAs(ResultSetExtractor<T> rse);
	
	<T> List<T> listWith(RowMapper<T> rowMapper);
	
	default <T> List<T> listWith(SingleArgRowMapper<T> rowMapper) {
		return listWith((RowMapper<T>)rowMapper);
	}
	
	<K, V> Map<K, V> asMap(DbmMapRowMapperResultSetExtractor<K, V> rse);
	
	/****
	 * 此接口参数只有一个，更加简单
	 * @author weishao zeng
	 * @param <K>
	 * @param <V>
	 * @param rse
	 * @return
	 */
	<K, V> Map<K, V> asMap(SimpleMapRowMapperResultSetExtractor<K, V> rse);
	
	@FunctionalInterface
	public interface SingleArgRowMapper<T> extends RowMapper<T> {

		@Override
		default T mapRow(ResultSet rs, int rowNum) throws SQLException {
			return mapRow(rs);
		}

		T mapRow(ResultSet rs) throws SQLException;
		
	}
}
