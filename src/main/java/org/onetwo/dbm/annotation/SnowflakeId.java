package org.onetwo.dbm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.persistence.GenerationType;

import org.onetwo.dbm.id.SnowflakeGenerator;

/**
 * 相当于下面三行注解：
 * @Id
 * @GeneratedValue(strategy = GenerationType.AUTO, generator="snowflake") 
 * @DbmIdGenerator(name="snowflake", generatorClass=SnowflakeGenerator.class)
 * 
 * @author wayshall
 * <br/>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@DbmId
@DbmIdGenerator(name="snowflake", generatorClass=SnowflakeGenerator.class)
@DbmGeneratedValue(strategy = GenerationType.AUTO, generator="snowflake")
public @interface SnowflakeId {
}
