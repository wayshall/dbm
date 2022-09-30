	package org.onetwo.dbm.event.internal;

import java.util.Collection;

import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.RegisterManager;
import org.onetwo.common.utils.map.CollectionMap;
import org.onetwo.dbm.event.spi.DbmCoreEventListenerManager;
import org.onetwo.dbm.event.spi.DbmEvent;
import org.onetwo.dbm.event.spi.DbmEventAction;
import org.onetwo.dbm.event.spi.DbmEventListener;

@SuppressWarnings("rawtypes")
public class DefaultCoreEventListenerManager implements RegisterManager<DbmEventAction, Collection<DbmEventListener>> /*DbEventListenerManager*/, DbmCoreEventListenerManager {

//	private Map<JFishEventAction, ?> registerMap = ArrayListMultimap.create();
	private static final DbmEventListener[] EMPTY_LISTENERS = new DbmEventListener[]{};
	private CollectionMap<DbmEventAction, DbmEventListener> registerMap = CollectionMap.newLinkedListMap();
	
	public void freezed(){
		registerMap.freezed();
	}
	
	@Override
	public CollectionMap<DbmEventAction, DbmEventListener> getRegister() {
		return registerMap;
	}
	
	/****
	 * 覆盖已存在的listerner
	 */
	@Override
	public DefaultCoreEventListenerManager register(DbmEventAction action, Collection<DbmEventListener> eventListeners){
		getRegister().put(action, eventListeners);
		return this;
	}
	
	/****
	 * 不覆盖已存在的listerner
	 * @param action
	 * @param eventListeners
	 * @return
	 */
	@Override
	public DbmCoreEventListenerManager registerListeners(DbmEventAction action, DbmEventListener...eventListeners){
		registerMap.putElements(action, eventListeners);
		return this;
	}
	
	@Override
	public DbmEventListener[] getListeners(DbmEventAction action){
		Collection<DbmEventListener> listenerList = getRegistered(action);
		if(LangUtils.isEmpty(listenerList)){
			return EMPTY_LISTENERS;
		}
		return listenerList.toArray(new DbmEventListener[listenerList.size()]);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void fireEvents(DbmEvent event){
		DbmEventListener[] listeners = getListeners(event.getAction());
		for(DbmEventListener listern : listeners){
			listern.doEvent(event);
		}
	}
	
}
	