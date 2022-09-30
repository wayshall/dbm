package org.onetwo.common.hibernate.dao;

import org.onetwo.common.dbm.model.hib.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * @author wayshall
 * <br/>
 */
public interface DepartmentJpaRepository extends CrudRepository<DepartmentEntity, Long>, JpaSpecificationExecutor<DepartmentEntity> {

}
