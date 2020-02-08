package org.onetwo.dbm.exception;


@SuppressWarnings("serial")
public class NoMappedEntryException extends DbmException{

	public NoMappedEntryException() {
		super("entry not found!");
	}

	public NoMappedEntryException(Class<?> clazz) {
		super("entry not found for class: " + clazz);
	}

	public NoMappedEntryException(String msg) {
		super(msg);
	}


}
