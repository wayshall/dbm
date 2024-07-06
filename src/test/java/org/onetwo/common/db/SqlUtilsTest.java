package org.onetwo.common.db;
/**
 * @author weishao zeng
 * <br/>
 */

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SqlUtilsTest {
	
	@Test
	public void testIsSQLSafeField() {
		String input = "dataDictionary_sort";
		boolean inject = SqlUtils.isSQLSafeField(input);
		assertThat(inject).isTrue();
		
		input = "dataDictionary.sort";
		inject = SqlUtils.isSQLSafeField(input);
		assertThat(inject).isTrue();
		
		input = " dataDictionary.sort ";
		inject = SqlUtils.isSQLSafeField(input);
		assertThat(inject).isFalse();
	}

	
	@Test
	public void testSqlValue() {
		String input = "test";
		String sqlValue = SqlUtils.checkSqlValue(input);
		assertThat(sqlValue).isEqualTo(input);
		
		input = "'1=1";
		sqlValue = SqlUtils.checkSqlValue(input);
		System.out.println("sqlvalue: " + sqlValue);
		assertThat(sqlValue).isEqualTo("\\'1\\=1");
		
		input = "@1=1";
		sqlValue = SqlUtils.checkSqlValue(input);
		System.out.println("sqlvalue: " + sqlValue);
		assertThat(sqlValue).isEqualTo("\\@1\\=1");
	}

}
