package org.onetwo.common.hibernate.dao;

import org.onetwo.common.dbm.model.hib.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * @author wayshall
 * <br/>
 */
public interface EmployeeJpaRepository extends CrudRepository<EmployeeEntity, Long>, JpaSpecificationExecutor<EmployeeEntity> {

}
