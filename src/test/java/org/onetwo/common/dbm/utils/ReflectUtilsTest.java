package org.onetwo.common.dbm.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.persistence.TableGenerator;

import org.junit.Test;
import org.onetwo.common.dbm.model.entity.UserTableIdEntity;
import org.onetwo.common.reflect.ReflectUtils;

/**
 * @author wayshall
 * <br/>
 */
public class ReflectUtilsTest {

	
	@Test
	public void testAnnotation(){
		Field idField = ReflectUtils.getIntro(UserTableIdEntity.class).getField("id");
		TableGenerator tg = idField.getAnnotation(TableGenerator.class);
		assertThat(TableGenerator.class).isNotEqualTo(tg.getClass());
		
		Method[] methods = tg.getClass().getDeclaredMethods();
		methods = TableGenerator.class.getDeclaredMethods();
		for(Method method : methods){
			Object val = ReflectUtils.invokeMethod(method, tg);
			System.out.println(""+method.getName()+":"+val);
		}
		
		UserTableIdEntity u = new UserTableIdEntity();
		assertThat(UserTableIdEntity.class).isEqualTo(u.getClass());
		
		Map<String, Object> attrs = ReflectUtils.toMap(TableGenerator.class, tg);
		System.out.println("attrs:"+attrs);
	}
}
