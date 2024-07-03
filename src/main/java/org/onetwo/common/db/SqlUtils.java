package org.onetwo.common.db;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.DbmException;
import org.springframework.util.ClassUtils;

final public class SqlUtils {
	private static final String DRUID_PARSER_CLASS = "com.alibaba.druid.sql.parser.SQLParser";
	
//	public static final String[] SQL_KEY_WORKDS = new String[]{" ", ";", ",", "(", ")", "'", "\"", "\"\"", "/", "+", "-", "#"};
	
	// 定义允许的字符模式（字母、数字、下划线）
//	private static final Pattern ALLOWED_FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]*$");
	
	// SQL注入常见关键字和特殊字符的简单正则表达式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)\\b(SELECT|UPDATE|DELETE|INSERT|DROP|TRUNCATE|UNION|ALTER|CREATE|GRANT|REVOKE|EXEC(UTE)?|DECLARE|FETCH|FROM|DUAL|TABLE|WHERE|AND|OR)\\b|(?:'|%|--|;|\\+|,|\\-|\\*|/|\\(|\\)|\\[|\\]|\\{|\\}|\\||`|~|!|@|#|$|%|^|&|\\|)", 
            Pattern.CASE_INSENSITIVE);

    /**
     * 检查输入字符串是否可能包含SQL注入关键字或特殊字符。
     * @param input 待检查的输入字符串
     * @return 如果发现潜在的SQL注入风险，返回true；否则返回false。
     */
    public static boolean checkForSqlInjection(String input) {
        Matcher matcher = SQL_INJECTION_PATTERN.matcher(input);
        return matcher.find();
    }
    
	public static void checkSQLSafeField(String fieldName) {
		if(!isSQLSafeField(fieldName)) {
			throw new DbmException("sql field is unsafe : " + fieldName);
		}
    }
	
	public static boolean isSQLSafeField(String fieldName) {
		return checkForSqlInjection(fieldName);
//        return ALLOWED_FIELD_NAME_PATTERN.matcher(fieldName).matches();
    }

	public static String check(String sqlValue){
		if(StringUtils.isBlank(sqlValue)){
			return sqlValue;
		}
		if (checkForSqlInjection(sqlValue)) {
			throw new DbmException("sql value is unsafe : " + sqlValue);
		}
//		for(String str : SQL_KEY_WORKDS){
//			if(sqlValue.indexOf(str)!=-1) {
//				throw new DbmException("sql value is unsafe : " + sqlValue);
//			}
//		}
		return sqlValue;
	}
	
	public static boolean isDruidPresent(){
		return ClassUtils.isPresent(DRUID_PARSER_CLASS, ClassUtils.getDefaultClassLoader());
	}
	
	
	private SqlUtils(){
	}
}
