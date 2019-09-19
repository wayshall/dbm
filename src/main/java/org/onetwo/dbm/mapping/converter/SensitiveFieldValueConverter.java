package org.onetwo.dbm.mapping.converter;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.annotation.DbmSensitiveField;
import org.onetwo.dbm.annotation.DbmSensitiveField.SensitiveOns;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.mapping.DbmFieldValueConverter;
import org.onetwo.dbm.mapping.DbmMappedField;

import lombok.Data;

/**
 * @author wayshall
 * <br/>
 */
public class SensitiveFieldValueConverter implements DbmFieldValueConverter {
	
	public SensitiveFieldValueConverter() {
		super();
	}

	@Override
	public Object forJava(DbmMappedField field, Object fieldValue) {
		if (fieldValue==null) {
			return fieldValue;
		}
		checkType(field);
		DbmSensitiveField sensitiveField = field.getPropertyInfo().getAnnotation(DbmSensitiveField.class);
		if (sensitiveField.on()==SensitiveOns.SELECT) {
			fieldValue = unsensitiveField(sensitiveField, fieldValue.toString());
		}
		return fieldValue;
	}

	@Override
	public Object forStore(DbmMappedField field, Object fieldValue) {
		checkType(field);
		if (fieldValue==null) {
			return fieldValue;
		}
		DbmSensitiveField sensitiveField = field.getPropertyInfo().getAnnotation(DbmSensitiveField.class);
		if (sensitiveField.on()==SensitiveOns.STORE) {
			fieldValue = unsensitiveField(sensitiveField, fieldValue.toString());
		}
		return fieldValue;
	}
	
	private void checkType(DbmMappedField field) {
		if (field.getColumnType()!=String.class) {
			throw new DbmException("the sensitive field[" + field.getName() + "] must be String type!");
		}
	}
	
	private String unsensitiveField(DbmSensitiveField sensitiveField, String sensitive) {
		SensitiveFieldInfo info = new SensitiveFieldInfo();
		info.setReplacementString(sensitiveField.replacementString());
		info.setLeftPlainTextSize(sensitiveField.leftPlainTextSize());
		info.setRightPlainTextSize(sensitiveField.rightPlainTextSize());
		info.setSensitiveEndOf(sensitiveField.sensitiveIndexOf());
		
		return unsensitiveString(info, sensitive);
	}
	
	protected String unsensitiveString(SensitiveFieldInfo sensitiveFieldInfo, String sensitive) {
		String unsensitive = null;
		if (StringUtils.isNotBlank(sensitiveFieldInfo.getSensitiveEndOf())) {
			int endIndex = StringUtils.indexOf(sensitive, sensitiveFieldInfo.getSensitiveEndOf());
			unsensitive = StringUtils.left(sensitive, endIndex);
			unsensitive = unsensitiveSurround(sensitiveFieldInfo, unsensitive);
			unsensitive += StringUtils.mid(sensitive, endIndex, sensitive.length());
		} else {
			unsensitive = unsensitiveSurround(sensitiveFieldInfo, sensitive);
		}
		return unsensitive;
	}

	/***
	 * 对字符两边脱敏
	 * @author weishao zeng
	 * @param sensitiveFieldInfo
	 * @param sensitive
	 * @return
	 */
	protected String unsensitiveSurround(SensitiveFieldInfo sensitiveFieldInfo, String sensitive) {
		if (sensitiveFieldInfo.getLeftPlainTextSize()<0 || sensitiveFieldInfo.getRightPlainTextSize()<0) {
			throw new DbmException("leftPlainTextSize or rightPlainTextSize can not be negative");
		}
		if (sensitiveFieldInfo.getLeftPlainTextSize() + sensitiveFieldInfo.getRightPlainTextSize() >= sensitive.length()) {
			return sensitive;
		}
		int padSize = sensitive.length() - sensitiveFieldInfo.getLeftPlainTextSize() - sensitiveFieldInfo.getRightPlainTextSize();
		String unsensitive = StringUtils.left(sensitive, sensitiveFieldInfo.getLeftPlainTextSize()) + 
				LangUtils.repeatString(padSize, sensitiveFieldInfo.getReplacementString()) + 
				StringUtils.right(sensitive, sensitiveFieldInfo.getRightPlainTextSize());
		return unsensitive;
	}
	
	@Data
	protected static class SensitiveFieldInfo {
		/***
		 *  左边保留明文的长度
		 */
		int leftPlainTextSize;
		int rightPlainTextSize;
		
		/***
		 * 当不想整个字段进行脱敏的时候，此属性表示某个指定的字符索引作为脱敏的结束索引，当这个属性不为空的时候，sensitiveIndex属性值只表示脱敏方向
		 * @author weishao zeng
		 * @return
		 */
		String sensitiveEndOf;
		
		/****
		 * 替换敏感数据的字符串
		 * @author weishao zeng
		 * @return
		 */
		String replacementString;
	}

}
