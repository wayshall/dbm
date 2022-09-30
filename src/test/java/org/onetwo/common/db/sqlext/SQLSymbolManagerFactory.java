package org.onetwo.common.db.sqlext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.onetwo.common.db.EntityManagerProvider;
import org.onetwo.common.db.filter.annotation.DataQueryFilterListener;
import org.onetwo.common.dbm.MockDbmInnerServiceRegistry;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.query.JFishSQLSymbolManagerImpl;

/****
 * 用于测试
 * 
 * @author way
 *
 */
public class SQLSymbolManagerFactory {
//	private static SQLSymbolManager JPA;
//	private static SQLSymbolManager HIBERNATE;
//	private static SQLSymbolManager JDBC;
	
	private static final SQLSymbolManagerFactory instance = new SQLSymbolManagerFactory();
	
	public static SQLSymbolManagerFactory getInstance() {
		return instance;
	}

	private Map<EntityManagerProvider, SQLSymbolManager> providers;
	
	{
//		 SQLSymbolManager jpa = new DefaultSQLSymbolManagerImpl(new JPQLDialetImpl());
//		 SQLSymbolManager hibernate = new DefaultSQLSymbolManagerImpl(new DefaultExtQueryDialetImpl());
//		 SQLSymbolManager jdbc = new DefaultSQLSymbolManagerImpl(new DefaultExtQueryDialetImpl());
		JFishSQLSymbolManagerImpl jdbc = new JFishSQLSymbolManagerImpl(new DefaultExtQueryDialetImpl());
		 jdbc.setListeners(Arrays.asList(new DataQueryFilterListener()));
		 // just for test
		 DbmInnerServiceRegistry sr = new MockDbmInnerServiceRegistry();
		 sr.initialize(null);
		 jdbc.setMappedEntryManager(sr.getMappedEntryManager());
		 
		 Map<EntityManagerProvider, SQLSymbolManager> temp = new HashMap<EntityManagerProvider, SQLSymbolManager>();
//		 temp.put(EntityManagerProvider.JPA, jpa);
//		 temp.put(EntityManagerProvider.Hibernate, hibernate);
		 temp.put(EntityManagerProvider.JDBC, jdbc);
		 
//		 PROVIDERS = Collections.unmodifiableMap(temp);
		 providers = temp;
	}
	
	public SQLSymbolManagerFactory register(EntityManagerProvider type, SQLSymbolManager symbolMgr){
		Assert.notNull(type);
		Assert.notNull(symbolMgr);
		providers.put(type, symbolMgr);
		return this;
	}

	public SQLSymbolManager get(EntityManagerProvider type){
		SQLSymbolManager symbol = providers.get(type);
		if(symbol==null){
			LangUtils.throwBaseException("can not find the SQLSymbolManager for provider : " + type);
		}
		return symbol;
	}
	
	public SQLSymbolManager getJdbc(){
		return get(EntityManagerProvider.JDBC);
	}
	
	public SQLSymbolManager getJPA(){
		return get(EntityManagerProvider.JPA);
	}
	/*
	public SQLSymbolManager getHibernate(){
		return get(EntityManagerProvider.Hibernate);
	}*/
}
