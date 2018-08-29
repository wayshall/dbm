package org.onetwo.dbm.event.spi;

import java.util.Collection;

/**
 * @author wayshall
 * <br/>
 */
@SuppressWarnings("rawtypes")
public interface DbmCoreEventListenerManager {

	/****
	 * 覆盖已存在的listerner
	 */
	DbmCoreEventListenerManager register(DbmEventAction action,
			Collection<DbmEventListener> eventListeners);

	/****
	 * 不覆盖已存在的listerner
	 * @param action
	 * @param eventListeners
	 * @return
	 */
	DbmCoreEventListenerManager registerListeners(DbmEventAction action,
			DbmEventListener... eventListeners);

	DbmEventListener[] getListeners(DbmEventAction action);

	void fireEvents(DbmEvent event);

}