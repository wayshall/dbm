package org.onetwo.common.dbm.model.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import org.onetwo.dbm.annotation.DbmEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wayshall
 * <br/>
 */
@DbmEntity(table="test_article")
@Data
@EqualsAndHashCode(callSuper=true)
public class ArticleEntity extends TenentBaseEntity {

	@Id  
	@GeneratedValue(strategy = GenerationType.TABLE, generator="tableIdGenerator")  
	@TableGenerator(name = "tableIdGenerator",  
	    table="gen_ids",  
	    pkColumnName="gen_name",  
	    valueColumnName="gen_value",  
	    pkColumnValue="seq_test_atricle",  
	    allocationSize=50
	)
//	@SequenceGenerator(name="seqGenerator", sequenceName="SEQ_TEST_ARTICLE")
	private Long id;
//	@DbmGenerated(GeneratedOn.INSERT)
//	@GeneratedValue(strategy = GenerationType.AUTO, generator="snowflakeId") 
//	@DbmIdGenerator(name="snowflakeId", generatorClass=SnowflakeGenerator.class)
	private Long tid;
	private String title;
	private String content;
	
	@Version
	private int dataVersion;
	
	private Long authorUserId;
	
}
