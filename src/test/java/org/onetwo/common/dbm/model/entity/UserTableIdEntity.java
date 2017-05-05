package org.onetwo.common.dbm.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.validator.constraints.Length;
import org.onetwo.dbm.core.BaseModel;

/**
 * @author wayshall
 * <br/>
 */
@Entity
@Table(name="TEST_USER")
public class UserTableIdEntity extends BaseModel<UserTableIdEntity, Long> {

	/***
	 CREATE TABLE `gen_ids` (
		`gen_name`  varchar(255) NOT NULL ,
		`gen_value`  bigint NOT NULL ,
		PRIMARY KEY (`gen_name`)
		)
		;
		

	 */
	@Id  
	@GeneratedValue(strategy = GenerationType.TABLE, generator="tableIdGenerator")  
	@TableGenerator(name = "tableIdGenerator",  
	    table="gen_ids",  
	    pkColumnName="gen_name",  
	    valueColumnName="gen_value",  
	    pkColumnValue="seq_test_user",  
	    allocationSize=50
	)
	@SequenceGenerator(name="seqGenerator", sequenceName="SEQ_TEST_USER")
	protected Long id;
	@Length(min=1, max=50)
	protected String userName;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserTableIdEntity other = (UserTableIdEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}
	
	
}
