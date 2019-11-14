package org.onetwo.dbm.utils;

import org.onetwo.common.exception.ErrorType;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author weishao zeng
 * <br/>
 */
@AllArgsConstructor
public enum DbmErrors implements ErrorType {
	ERR_DBM("dbm error"),
	ERR_LOCK_ID_NOT_FOUND("locker id not found!"),
	ERR_LOCK_TIMEOUT("locker was timed-out!")
	;
	
	@Getter
	private final String errorMessage;

	@Override
	public String getErrorCode() {
		return name();
	}

}

