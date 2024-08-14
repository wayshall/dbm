package org.onetwo.dbm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.GenerationType;
import javax.persistence.TableGenerator;

import org.springframework.core.annotation.AliasFor;

/**
 * 相当于下面的注解配置：
 * @Id  
	@GeneratedValue(strategy = GenerationType.TABLE, generator="tableIdGenerator")  
	@TableGenerator(name = "tableIdGenerator",  
	    table="gen_ids",  
	    pkColumnName="gen_name",  
	    valueColumnName="gen_value",  
	    pkColumnValue="seq_test_atricle",  
    	initialValue = 1,
	    allocationSize=50
	)
 * @author wayshall
 * <br/>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@DbmId
@DbmGeneratedValue(strategy = GenerationType.TABLE, generator="dbmTableIdGenerator")
@TableGenerator(name = "dbmTableIdGenerator",  
    table="gen_ids",
    pkColumnName="gen_name",  
    valueColumnName="gen_value",
    /**
     * 会被DbmTableIdGenerator.initialValue覆盖
     ***/
    initialValue = 100
//    pkColumnValue="seq_test_atricle",  
//    allocationSize=50
)
public @interface DbmTableIdGenerator {
	/****
	 * 生成主键的列值, 即gen_ids表gen_name列对应的值
	 * @return
	 */
	@AliasFor(annotation = TableGenerator.class, attribute = "pkColumnValue")
	String value();

	@AliasFor(annotation = TableGenerator.class, attribute = "name")
    String name() default "dbmTableIdGenerator";

	/***
	 * 会覆盖@TableGenerator上的默认值
	 * @return
	 */
	@AliasFor(annotation = TableGenerator.class, attribute = "initialValue")
    int initialValue() default 1;

	@AliasFor(annotation = TableGenerator.class, attribute = "allocationSize")
    int allocationSize() default 50;

	@AliasFor(annotation = TableGenerator.class, attribute = "table")
    String table() default "gen_ids";

	@AliasFor(annotation = TableGenerator.class, attribute = "pkColumnName")
    String pkColumnName() default "gen_name";

	@AliasFor(annotation = TableGenerator.class, attribute = "valueColumnName")
    String valueColumnName() default "gen_value";
}
