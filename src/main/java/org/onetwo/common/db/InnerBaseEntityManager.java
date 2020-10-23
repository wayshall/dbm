package org.onetwo.common.db;

import java.util.List;

import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.db.sqlext.SelectExtQuery;
import org.onetwo.common.utils.Page;

public interface InnerBaseEntityManager extends BaseEntityManager {
	
	<T> List<T> select(SelectExtQuery extQuery);
	<T> T selectUnique(SelectExtQuery extQuery);
	<T> void selectPage(Page<T> page, SelectExtQuery extQuery);
	

	<T> List<T> findList(DbmQueryValue queryValue);
	<T> T findUnique(DbmQueryValue queryValue);

	Number count(SelectExtQuery extQuery);
	
	/****
	 * 检测数据是否存在
	 * @author weishao zeng
	 * @param extQuery
	 * @return
	 */
//	boolean exist(SelectExtQuery extQuery);
//	<T> T selectOne(SelectExtQuery extQuery);
}
