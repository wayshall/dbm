package org.onetwo.common.dbm.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
