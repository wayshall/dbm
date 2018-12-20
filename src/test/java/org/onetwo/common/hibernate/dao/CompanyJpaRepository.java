package org.onetwo.common.hibernate.dao;

import org.onetwo.common.dbm.model.hib.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * @author wayshall
 * <br/>
 */
public interface CompanyJpaRepository extends CrudRepository<CompanyEntity, Long>, JpaSpecificationExecutor<CompanyEntity> {

}
