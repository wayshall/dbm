package org.onetwo.common.db;

import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.DbmException;
import org.springframework.util.ClassUtils;

final public class SqlUtils {
	private static final String DRUID_PARSER_CLASS = "com.alibaba.druid.sql.parser.SQLParser";
	
//	public static final String[] SQL_KEY_WORKDS = new String[]{" ", ";", ",", "(", ")", "'", "\"", "\"\"", "/", "+", "-", "#"};
	
	// 定义允许的字符模式（字母、数字、下划线）
	private static final Pattern ALLOWED_FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\.]*$");
	
    private final static char[] IMMUNE_SQL = { ' ' };
    
    private final static String[] HEX = new String[256];

    /**
     * Default constructor
     */
    static {
        for ( char c = 0; c < 0xFF; c++ ) {
            if ( c >= 0x30 && c <= 0x39 || c >= 0x41 && c <= 0x5A || c >= 0x61 && c <= 0x7A ) {
            	HEX[c] = null;
            } else {
            	HEX[c] = Integer.toHexString(c).intern();
            }
        }
    }


    private static String getHexForNonAlphanumeric(char c) {
        if(c<0xFF)
            return HEX[c];
        return Integer.toHexString(c);
    }

    private static String encode(char[] immune, String input) {
        StringBuilder sb = new StringBuilder();
        for(int offset  = 0; offset < input.length(); ) {
            final int point = input.codePointAt(offset);
            if (Character.isBmpCodePoint(point)) {
                //We can then safely cast this to char and maintain legacy behavior.
                sb.append(encodeCharacter(immune, new Character((char) point)));
            } else {
                sb.append(encodeCharacter(immune, point));
            }
            offset += Character.charCount(point);
        }
        return sb.toString();
    }
    
    private static String encodeCharacter( char[] immune, int codePoint ) {
        String rval = "";
        if(Character.isValidCodePoint(codePoint)){
            rval = new StringBuilder().appendCodePoint(codePoint).toString();
        }
        return rval;
    }
    
    private static String encodeCharacter( char[] immune, Character c ) {
        char ch = c.charValue();

        // check for immune characters
        if ( ArrayUtils.contains(immune, ch) ) {
            return ""+ch;
        }

        // check for alphanumeric characters
        String hex = getHexForNonAlphanumeric(ch);
        if ( hex == null ) {
            return ""+ch;
        }


        return encodeCharacterMySQL(c);
    }
    

    private  static String encodeCharacterMySQL( Character c ) {
        char ch = c.charValue();
        if ( ch == 0x00 ) return "\\0";
        if ( ch == 0x08 ) return "\\b";
        if ( ch == 0x09 ) return "\\t";
        if ( ch == 0x0a ) return "\\n";
        if ( ch == 0x0d ) return "\\r";
        if ( ch == 0x1a ) return "\\Z";
        if ( ch == 0x22 ) return "\\\"";
        if ( ch == 0x25 ) return "\\%";
        if ( ch == 0x27 ) return "\\'";
        if ( ch == 0x5c ) return "\\\\";
        if ( ch == 0x5f ) return "\\_";
        return "\\" + c;
    }
    
    
    static public String encodeForSQL(String input) {
        if( input == null ) {
            return null;
        }
        return encode(IMMUNE_SQL, input);
    }

	public static void checkSQLSafeField(String fieldName) {
		if(!isSQLSafeField(fieldName)) {
			throw new DbmException("sql field is unsafe : " + fieldName);
		}
    }
	
	public static boolean isSQLSafeField(String fieldName) {
        return ALLOWED_FIELD_NAME_PATTERN.matcher(fieldName).matches();
    }

	public static String checkSqlValue(String sqlValue){
		if(StringUtils.isBlank(sqlValue)){
			return sqlValue;
		}
		
		sqlValue = encodeForSQL(sqlValue);
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
