package org.onetwo.dbm.event;

import org.onetwo.dbm.dialet.DBDialect.LockInfo;
import org.onetwo.dbm.utils.DbmLock;

public class DbmLockEvent extends DbmSessionEvent {
	final private DbmLock lock;
	final private int timeInMillis;

	public DbmLockEvent(Object object, DbmLock lock, Integer timeInMillis, DbmSessionEventSource eventSource) {
		super(object, DbmEventAction.lock, eventSource);
		this.lock = lock;
		this.timeInMillis = timeInMillis==null?LockInfo.WAIT_FOREVER:timeInMillis;
	}

	private Object resultObject;

	public Object getResultObject() {
		return resultObject;
	}

	public void setResultObject(Object resultObject) {
		this.resultObject = resultObject;
	}

	public DbmLock getLock() {
		return lock;
	}

	public int getTimeInMillis() {
		return timeInMillis;
	}

}
