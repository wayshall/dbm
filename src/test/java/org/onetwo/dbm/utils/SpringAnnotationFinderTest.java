package org.onetwo.dbm.utils;
/**
 * @author weishao zeng
 * <br/>
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.dbm.annotation.DbmField;

public class SpringAnnotationFinderTest {

	@Test
	public void test() {
		Method field = ReflectUtils.getReadMethod(UserEntity.class, "appCode", String.class);
		SpringAnnotationFinder finder = new SpringAnnotationFinder();
		DbmField dbmfield = finder.getAnnotation(field, DbmField.class);
		assertThat(dbmfield).isNotNull();
	}
}

