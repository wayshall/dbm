package org.onetwo.common.dbm.model.dao;

import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.dquery.annotation.Param;
import org.onetwo.common.db.dquery.annotation.Query;
import org.onetwo.common.dbm.JsonFieldTest.JdbcMapperJsonCompanyVO;
import org.onetwo.common.dbm.JsonFieldTest.NoJdbcMapperJsonCompanyVO;

/**
 * @author wayshall
 * <br/>
 */
@DbmRepository
public interface JsonCompanyDao {
	
	@Query(value="select t.* from company t where t.name=:name")
	JdbcMapperJsonCompanyVO findOne(@Param("name")String name);

	@Query(value="select t.* from company t where t.name=:name")
	NoJdbcMapperJsonCompanyVO findOneNoJdbcMapper(@Param("name")String name);

}
