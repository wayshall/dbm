package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.common.db.filequery.ParserContextFunctionSet;
import org.onetwo.common.db.spi.FileSqlParserType;
import org.onetwo.common.db.spi.QueryContextVariable;

/**
 * @author wayshall
 * <br/>
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
	
	String value() default "";
	
	String countQuery() default "";
	
	FileSqlParserType parser() default FileSqlParserType.TEMPLATE;
	

	/***
	 * 配置参数中模糊查询的字段
	 * @return
	 */
	public String[] likeQueryFields() default {};
	/*** 
	 * 如果是hibernate实现，该方法决定使用何种session
	 * @return
	 */
//	public boolean stateful() default true;
	/***
	 * 
	 * @return
	 */
	public Class<? extends QueryContextVariable> funcClass() default ParserContextFunctionSet.class;
	
	/****
	 * 在分页的查询下，是否使用自动生成分页sql(limit sql string)
	 * @author weishao zeng
	 * @return
	 */
	boolean useAutoLimitSqlIfPagination() default true;

}
