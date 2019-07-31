package org.onetwo.common.db.filequery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author weishao zeng
 * <br/>
 */
@Configuration
public class NamedQueryInfoParserConfiguration {

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public MultipCommentsSqlFileParser multipCommentsSqlFileParser() {
		return new MultipCommentsSqlFileParser();
	}
	
	@Bean
	@Order(1)
	public AnnotationBasedQueryInfoParser annotationBasedQueryInfoParser() {
		return new AnnotationBasedQueryInfoParser();
	}
}

