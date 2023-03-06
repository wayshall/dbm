package org.onetwo.common.db.filequery;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.onetwo.common.db.filequery.postfunc.SqlParamterPostfixFunctions;
import org.onetwo.common.db.filequery.postfunc.SqlParamterPostfixFunctions.SqlPostfixFunctionInfo;

public class SqlParamterPostfixFunctionsTest {
	
	SqlParamterPostfixFunctions func = new SqlParamterPostfixFunctions();
	
	@Test
	public void testOneInPrefx() {
		String input = "$3_minutes_ago";
		SqlPostfixFunctionInfo info  = func.parseSqlPostfixFunc(input);
		System.out.println("func info: " + info);
		assertThat(info.getFunctionName()).isEqualTo("minutes_ago");
		assertThat(info.getArgumentNames().size()).isEqualTo(1);
		assertThat(info.getArgumentNames().get(0)).isEqualTo("3");
		

	}


	@Test
	public void testOneInPostfix() {
		String input = "minutes_ago_$3";
		SqlPostfixFunctionInfo info  = func.parseSqlPostfixFunc(input);
		System.out.println("func info: " + info);
		assertThat(info.getFunctionName()).isEqualTo("minutes_ago");
		assertThat(info.getArgumentNames().size()).isEqualTo(1);
		assertThat(info.getArgumentNames().get(0)).isEqualTo("3");
	}
	
	@Test
	public void testTwo() {
		String input = "from_$3_to_$4";
		SqlPostfixFunctionInfo info  = func.parseSqlPostfixFunc(input);
		System.out.println("func info: " + info);
		assertThat(info.getFunctionName()).isEqualTo("from_to");
		assertThat(info.getArgumentNames().size()).isEqualTo(2);
		assertThat(info.getArgumentNames().get(0)).isEqualTo("3");
		assertThat(info.getArgumentNames().get(1)).isEqualTo("4");
	}

}
