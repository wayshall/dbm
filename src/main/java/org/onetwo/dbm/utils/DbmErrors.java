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
	ERR_LOCK_TIMEOUT("locker was timed-out!"),
	ERR_DUPLICATE_ENTITY_NAME("duplicate entity name!"),
	ERR_SESSION_IS_CLOSED("Session is closed!")
	;
	
	@Getter
	private final String errorMessage;

	@Override
	public String getErrorCode() {
		return name();
	}

}

