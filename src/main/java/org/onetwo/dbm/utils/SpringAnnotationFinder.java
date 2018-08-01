package org.onetwo.dbm.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.onetwo.common.annotation.AnnotationInfo.AnnotationFinder;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * @author wayshall
 * <br/>
 */
public class SpringAnnotationFinder implements AnnotationFinder {
	
	final public static SpringAnnotationFinder INSTANCE = new SpringAnnotationFinder();

	@Override
	public Annotation getAnnotation(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationType) {
		return AnnotationUtils.findAnnotation(annotatedElement, annotationType);
	}

}
