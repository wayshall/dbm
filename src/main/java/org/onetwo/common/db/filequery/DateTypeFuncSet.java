package org.onetwo.common.db.filequery;

import java.util.Date;

import org.onetwo.common.date.DateUtils;

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
	
	private DateTypeFuncSet() {
	}

}
