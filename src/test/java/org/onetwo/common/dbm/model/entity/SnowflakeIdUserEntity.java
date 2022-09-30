package org.onetwo.common.dbm.model.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.validator.constraints.Length;
import org.onetwo.dbm.annotation.DbmEntity;
import org.onetwo.dbm.annotation.DbmIdGenerator;
import org.onetwo.dbm.annotation.SnowflakeId;
import org.onetwo.dbm.id.SnowflakeGenerator;
import org.onetwo.dbm.jpa.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wayshall
 * <br/>
 */
//@Entity
//@Table(name="TEST_USER")
@DbmEntity(table="TEST_USER")
@Data
@EqualsAndHashCode(callSuper=true)
public class SnowflakeIdUserEntity extends BaseEntity {

//	@Id  
//	@GeneratedValue(strategy = GenerationType.AUTO, generator="snowflake") 
//	@DbmIdGenerator(name="snowflake", generatorClass=SnowflakeGenerator.class)
	@SnowflakeId
	protected Long id;
	@Length(min=1, max=50)
	protected String userName;
	
	@DbmEntity(table="TEST_USER")
	@Data
	@EqualsAndHashCode(callSuper=true)
	public static class SnowflakeIdUser2Entity extends BaseEntity {
	
		@Id  
		@GeneratedValue(strategy = GenerationType.AUTO, generator="snowflakeIdUser2") 
		@DbmIdGenerator(name="snowflakeIdUser2", generatorClass=SnowflakeGenerator.class, valueType=String.class)
		protected String id;
		@Length(min=1, max=50)
		protected String userName;
		
	}
}
