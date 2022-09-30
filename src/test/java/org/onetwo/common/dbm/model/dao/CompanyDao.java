package org.onetwo.common.dbm.model.dao;

import java.util.Collection;
import java.util.List;

import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.dbm.model.vo.CompanyVO;
import org.onetwo.common.dbm.model.vo.DepartmentVO;
import org.onetwo.dbm.annotation.DbmNestedResult;
import org.onetwo.dbm.annotation.DbmNestedResult.NestedType;
import org.onetwo.dbm.annotation.DbmResultMapping;

@DbmRepository
public interface CompanyDao {
	
	List<CompanyVO> findCompaniesByLikeName(String name);
	
	List<CompanyVO> findCompaniesByNames(Collection<String> names);
	
	@DbmResultMapping(value={
			@DbmNestedResult(property="company", id="id", columnPrefix="comp_", nestedType=NestedType.ASSOCIATION)
	})
	List<DepartmentVO> findDepartmentsWithComapny();

	@DbmResultMapping(value={
			@DbmNestedResult(property="employees", columnPrefix="emply_", nestedType=NestedType.COLLECTION),
			@DbmNestedResult(property="company", id="id", columnPrefix="comp_", nestedType=NestedType.ASSOCIATION)
	})
	List<DepartmentVO> findNestedDepartments();

	@DbmResultMapping(value={
			@DbmNestedResult(property="employees", id="id", columnPrefix="emply_", nestedType=NestedType.COLLECTION),
			@DbmNestedResult(property="company", id="id", columnPrefix="comp_", nestedType=NestedType.ASSOCIATION)
	})
	List<DepartmentVO> findNestedDepartmentsWithEmployeeId();
	

	@DbmResultMapping(value={
			@DbmNestedResult(property="departments.employees", columnPrefix="emply_", nestedType=NestedType.COLLECTION),
			@DbmNestedResult(property="departments", id="id", nestedType=NestedType.COLLECTION)
	})
	List<CompanyVO> findNestedCompanies();
	
	@DbmResultMapping(value={
			/****
			 * emply_* 映射到  departmentMap.employees
			 */
			@DbmNestedResult(property="departmentMap.employees", columnPrefix="emply_", nestedType=NestedType.COLLECTION),
			@DbmNestedResult(property="departmentMap", id="id", columnPrefix="departments_", nestedType=NestedType.MAP)
	})
	List<CompanyVO> findNestedCompaniesWithDepartmentMap();
	
	/***
	 * 嵌套的list元素employeeNames为简单类型时，id必须为value
	 * @author wayshall
	 * @return
	 */
	@DbmResultMapping(value={
			/****
			 * emply_value 映射到 employeeNames
			 */
			@DbmNestedResult(property="employeeNames", id="value", columnPrefix="emply_", nestedType=NestedType.COLLECTION),
	})
	List<DepartmentVO> findDepartmentWithEmployeeNames();

}
