package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.generator.dialet.DatabaseMetaDialet;
import org.onetwo.common.db.generator.dialet.DelegateDatabaseMetaDialet;
import org.onetwo.common.db.generator.meta.TableMeta;
import org.onetwo.common.dbm.model.dao.SqlScriptDao;
import org.onetwo.dbm.exception.DbmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * @author weishao zeng
 * <br/>
 */
@Rollback(false)
public class DbmSqlScriptTest extends DbmBaseTest {
	@Autowired
	private SqlScriptDao sqlScriptDao;
	
	@Test
	public void test() {
		DatabaseMetaDialet dialet = new DelegateDatabaseMetaDialet(dataSource);
		
		assertThatExceptionOfType(DbmException.class).isThrownBy(()-> {
			sqlScriptDao.executeSqlScriptError();
		}).withMessageContaining("sql script method must return void:");
		

		sqlScriptDao.executeSqlScriptSuccess();
		TableMeta meta = dialet.getTableMeta("wx_access_token");
		assertThat(meta).isNotNull();
	}

}
