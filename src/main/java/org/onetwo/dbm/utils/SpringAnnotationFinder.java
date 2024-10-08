package org.onetwo.dbm.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.onetwo.common.annotation.AnnotationInfo.AnnotationFinder;
import org.onetwo.common.spring.utils.SpringMergedAnnotationFinder;

/**
 * @author wayshall
 * <br/>
 */
public class SpringAnnotationFinder implements AnnotationFinder {
	
	final public static SpringAnnotationFinder INSTANCE = new SpringAnnotationFinder();

	@Override
	public <A extends Annotation> A getAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
//		return AnnotationUtils.findAnnotation(annotatedElement, annotationType);
		return SpringMergedAnnotationFinder.INSTANCE.getAnnotation(annotatedElement, annotationType);
	}

}
