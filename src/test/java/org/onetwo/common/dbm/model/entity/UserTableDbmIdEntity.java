package org.onetwo.common.dbm.model.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.validator.constraints.Length;
import org.onetwo.dbm.annotation.DbmEntity;
import org.onetwo.dbm.annotation.DbmIdGenerator;
import org.onetwo.dbm.core.BaseModel;
import org.onetwo.dbm.core.spi.DbmSessionImplementor;
import org.onetwo.dbm.id.CustomIdGenerator;
import org.onetwo.dbm.id.SnowflakeIdGenerator;

/**
 * @author wayshall
 * <br/>
 */
//@Entity
//@Table(name="TEST_USER")
@DbmEntity(table="TEST_USER")
public class UserTableDbmIdEntity extends BaseModel<UserTableDbmIdEntity, Long> {

	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO, generator="snowflake") 
	@DbmIdGenerator(name="snowflake", generatorClass=SnowflakeGenerator.class)
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
		UserTableDbmIdEntity other = (UserTableDbmIdEntity) obj;
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
	
	public static class SnowflakeGenerator implements CustomIdGenerator<Long>  {

		SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1);
		
		@Override
		public Long generate(DbmSessionImplementor session) {
			return idGenerator.nextId();
		}
		
	}
}
