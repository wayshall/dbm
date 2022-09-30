package org.onetwo.dbm.annotation;

import java.util.Map;

import javax.persistence.Entity;

import org.junit.Test;
import org.onetwo.dbm.mapping.DbmFieldValueConverter;
import org.onetwo.dbm.mapping.DbmMappedField;

/**
 * @author wayshall
 * <br/>
 */
public class DbmFieldTest {
	
	@Test
	public void test(){
		
	}
	
	@Entity
	static class DbmFeildEntity {
		@DbmJsonField
		Map testMapField;
		@DbmField(converterClass=JustTestDbmFieldValueConverter.class)
		String testField;
		
		public Map getTestMapField() {
			return testMapField;
		}
		public void setTestMapField(Map testMapField) {
			this.testMapField = testMapField;
		}
		public String getTestField() {
			return testField;
		}
		public void setTestField(String testField) {
			this.testField = testField;
		}
		
	}
	
	static class JustTestDbmFieldValueConverter implements DbmFieldValueConverter {

		@Override
		public Object forJava(DbmMappedField field, Object fieldValue) {
			return fieldValue;
		}

		@Override
		public Object forStore(DbmMappedField field, Object fieldValue) {
			return fieldValue;
		}
		
	}
	

}
