package org.onetwo.common.db.filequery;

import java.util.Date;

import org.onetwo.common.convert.Types;
import org.onetwo.common.date.DateUtils;
import org.onetwo.common.db.filequery.SqlParamterPostfixFunctions.SqlPostfixFunctionInfo;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.exception.DbmException;

/**
 * @author weishao zeng
 * <br/>
 */
public class DateTypeFuncSet {
	
	public static String yyyyMMdd(Date date) {
		return DateUtils.format(DateUtils.DATEONLY, date);
	}
	
	public static String dateString(Date date) {
		return DateUtils.format(DateUtils.DATE_ONLY, date);
	}
	
	public static String dateTimeString(Date date) {
		return DateUtils.format(DateUtils.DATE_TIME, date);
	}
	
	public static Date minutes_ago(Date date, SqlPostfixFunctionInfo funcInfo) {
		if (LangUtils.isEmpty(funcInfo.getArgumentNames())) {
			throw new DbmException("sql postfix function argument not found!");
		}
		int numb = Types.asInteger(funcInfo.getArgumentNames().get(0));
		return DateUtils.addMinutes(date, -numb);
	}

	
	public static Date minutes_later(Date date, SqlPostfixFunctionInfo funcInfo) {
		if (LangUtils.isEmpty(funcInfo.getArgumentNames())) {
			throw new DbmException("sql postfix function argument not found!");
		}
		int numb = Types.asInteger(funcInfo.getArgumentNames().get(0));
		return DateUtils.addMinutes(date, numb);
	}
	
	private DateTypeFuncSet() {
	}

}
