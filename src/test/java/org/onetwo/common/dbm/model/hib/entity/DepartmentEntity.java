package org.onetwo.common.dbm.model.hib.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.validator.constraints.Length;
import org.onetwo.common.dbm.model.entity.BaseEntity;
import org.onetwo.dbm.mapping.DbmEnumIntMapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;


/*****
 * @Entity
 */
@SuppressWarnings("serial")
@Entity
@Table(name="department")
@Data
@EqualsAndHashCode(callSuper=true)
public class DepartmentEntity extends BaseEntity {
	
	@AllArgsConstructor
	public static enum DepartStatus implements DbmEnumIntMapping {
		ENABLED(1),
		DISABLED(-1);
		
		@Getter
		final private int mappingValue;
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY) 
	@Column(name="ID")
	protected Long id;
	@Length(min=1, max=50)
	protected String name;
	protected Integer employeeNumber;
	protected Long companyId;
	
	@Enumerated(EnumType.ORDINAL)
	protected DepartStatus status;
  
	public DepartmentEntity(){
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

}