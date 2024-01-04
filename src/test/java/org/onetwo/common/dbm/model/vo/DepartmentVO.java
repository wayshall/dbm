package org.onetwo.common.dbm.model.vo;

import java.util.List;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.onetwo.common.dbm.model.hib.entity.DepartmentEntity.DepartStatus;
import org.onetwo.dbm.annotation.DbmRowMapper;
import org.onetwo.dbm.annotation.DbmRowMapper.MappingModes;

import lombok.Data;

@Data
@DbmRowMapper(mappingMode=MappingModes.ENTITY)
public class DepartmentVO {
	
	protected Long id;
	protected String name;
	protected Integer employeeNumber;
	protected Long companyId;
	protected List<EmployeeVO> employees;
	
	protected CompanyVO company;
	

	protected List<String> employeeNames;
	

	@Enumerated(EnumType.ORDINAL)
	protected DepartStatus status;
  
	public DepartmentVO(){
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getEmployeeNumber() {
		return employeeNumber;
	}

	public void setEmployeeNumber(Integer employeeNumber) {
		this.employeeNumber = employeeNumber;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public List<EmployeeVO> getEmployees() {
		return employees;
	}

	public void setEmployees(List<EmployeeVO> employees) {
		this.employees = employees;
	}

	public CompanyVO getCompany() {
		return company;
	}

	public void setCompany(CompanyVO company) {
		this.company = company;
	}

	public List<String> getEmployeeNames() {
		return employeeNames;
	}

	public void setEmployeeNames(List<String> employeeNames) {
		this.employeeNames = employeeNames;
	}

}