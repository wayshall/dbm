package org.onetwo.dbm.mapping.converter;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.mapping.DbmFieldValueConverter;
import org.onetwo.dbm.mapping.DbmMappedField;

/**
 * @author wayshall
 * <br/>
 */
public class EncryptFieldValueConverter implements DbmFieldValueConverter {
	
	private StandardPBEStringEncryptor encryptor;
	
	public EncryptFieldValueConverter() {
		super();
	}

	@Override
	public Object forJava(DbmMappedField field, Object fieldValue) {
		if (fieldValue==null) {
			return fieldValue;
		}
		String decrypted = encryptor.decrypt(fieldValue.toString());
		return decrypted;
	}

	@Override
	public Object forStore(DbmMappedField field, Object fieldValue) {
		if (field.getColumnType()!=String.class) {
			throw new DbmException("the encrypt field[" + field.getName() + "] must be String type!");
		}
		if (fieldValue==null) {
			return fieldValue;
		}
		String encrypted = encryptor.encrypt(fieldValue.toString());
		return encrypted;
	}

	public void setEncryptor(StandardPBEStringEncryptor encryptor) {
		this.encryptor = encryptor;
	}
	

}
