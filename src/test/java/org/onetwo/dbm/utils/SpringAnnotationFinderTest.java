package org.onetwo.dbm.utils;
/**
 * @author weishao zeng
 * <br/>
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;
import org.onetwo.common.dbm.model.entity.SnowflakeIdUserEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.dbm.annotation.DbmFieldConvert;
import org.onetwo.dbm.annotation.DbmGeneratedValue;
import org.onetwo.dbm.annotation.DbmId;
import org.onetwo.dbm.annotation.DbmIdGenerator;

public class SpringAnnotationFinderTest {
	SpringAnnotationFinder instance = SpringAnnotationFinder.INSTANCE;
	
	@Test
	public void test() {
		Method field = ReflectUtils.getReadMethod(UserEntity.class, "appCodeUnsensitive", String.class);
		SpringAnnotationFinder finder = new SpringAnnotationFinder();
		DbmFieldConvert dbmfield = finder.getAnnotation(field, DbmFieldConvert.class);
		assertThat(dbmfield).isNotNull();
	}
	

	@Test
	public void testSnowFlakeId() {
		Field id = ReflectUtils.findField(SnowflakeIdUserEntity.class, "id");
		DbmIdGenerator dg = instance.getAnnotation(id, DbmIdGenerator.class);
		assertThat(dg).isNotNull();
		DbmGeneratedValue gv = instance.getAnnotation(id, DbmGeneratedValue.class);
		assertThat(gv).isNotNull();

		DbmId dbmId = instance.getAnnotation(id, DbmId.class);
		assertThat(dbmId).isNotNull();
	}
}

