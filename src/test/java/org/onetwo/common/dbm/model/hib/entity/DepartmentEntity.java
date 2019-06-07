package org.onetwo.common.dbm.model.hib.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Length;
import org.onetwo.common.dbm.model.entity.BaseEntity;
import org.onetwo.dbm.mapping.DbmEnumValueMapping;

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
	public static enum DepartStatus implements DbmEnumValueMapping {
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