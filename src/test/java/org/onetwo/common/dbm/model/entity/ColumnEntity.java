package org.onetwo.common.dbm.model.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.onetwo.dbm.annotation.DbmIdGenerator;
import org.onetwo.dbm.id.SnowflakeGenerator;
import org.onetwo.dbm.jpa.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author weishao zeng
 * <br/>
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper=true)
@Entity
@Table(name="test_column")
public class ColumnEntity extends BaseEntity {

	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO, generator="snowflake") 
	@DbmIdGenerator(name="snowflake", generatorClass=SnowflakeGenerator.class)
	Long id;
	
	String name;
	
	Long parentId;
}
