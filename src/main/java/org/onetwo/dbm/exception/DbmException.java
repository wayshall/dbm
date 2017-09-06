package org.onetwo.dbm.exception;

import org.onetwo.common.exception.BaseException;
import org.onetwo.common.exception.ErrorType;

@SuppressWarnings("serial")
public class DbmException extends BaseException{

	public DbmException() {
		super("dbm error!");
	}


	public DbmException(ErrorType exceptionType, Throwable cause) {
		this(exceptionType.getErrorMessage(), cause, exceptionType.getErrorCode());
	}

	public DbmException(String msg, Throwable cause, String code) {
		super(msg, cause, code);
	}

	public DbmException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public DbmException(String msg) {
		super(msg);
	}

	public DbmException(Throwable cause) {
		super(cause);
	}
	
	protected String getDefaultCode(){
		return JFishErrorCode.ORM_ERROR;
	}

}
