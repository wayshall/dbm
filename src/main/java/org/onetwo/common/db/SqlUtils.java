package org.onetwo.common.db;

import java.util.regex.Pattern;

import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.DbmException;
import org.springframework.util.ClassUtils;

final public class SqlUtils {
	private static final String DRUID_PARSER_CLASS = "com.alibaba.druid.sql.parser.SQLParser";
	
	public static final String[] SQL_KEY_WORKDS = new String[]{" ", ";", ",", "(", ")", "'", "\"", "\"\"", "/", "+", "-", "#"};
	
	// 定义允许的字符模式（字母、数字、下划线）
	static final Pattern ALLOWED_FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]*$");

	public static void checkSQLSafeField(String fieldName) {
		if(!isSQLSafeField(fieldName)) {
			throw new DbmException("sql field is unsafe : " + fieldName);
		}
    }
	
	public static boolean isSQLSafeField(String fieldName) {
        return ALLOWED_FIELD_NAME_PATTERN.matcher(fieldName).matches();
    }

	public static String check(String sqlValue){
		if(StringUtils.isBlank(sqlValue)){
			return sqlValue;
		}
		for(String str : SQL_KEY_WORKDS){
			if(sqlValue.indexOf(str)!=-1) {
				throw new DbmException("sql value is unsafe : " + sqlValue);
			}
		}
		return sqlValue;
	}
	
	public static boolean isDruidPresent(){
		return ClassUtils.isPresent(DRUID_PARSER_CLASS, ClassUtils.getDefaultClassLoader());
	}
	
	
	private SqlUtils(){
	}
}
