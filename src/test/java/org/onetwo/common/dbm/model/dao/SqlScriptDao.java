package org.onetwo.common.dbm.model.dao;

import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.dquery.annotation.SqlScript;

/**
 * @author weishao zeng
 * <br/>
 */
@DbmRepository
public interface SqlScriptDao {
	
	@SqlScript
	int executeSqlScriptError();

	@SqlScript
	void executeSqlScriptSuccess();

}
