package org.onetwo.dbm.dialet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.onetwo.common.db.DataBase;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.RegisterManager;
import org.onetwo.dbm.exception.DbmException;

public class DefaultDatabaseDialetManager implements RegisterManager<String, DBDialect>{
	
	final private Map<String, DBDialect> dialetRegister = new ConcurrentHashMap<>();
	
	public DefaultDatabaseDialetManager(){
		register(DataBase.MySQL.getName(), new MySQLDialect());
		register(DataBase.Oracle.getName(), new OracleDialect());
		register(DataBase.PostgreSQL.getName(), new PostgreSQLDialet());
		register(DataBase.H2.getName(), new H2Dialet());
	}
	
	final public DefaultDatabaseDialetManager register(DBDialect dialet){
		Assert.notNull(dialet);
		getRegister().put(dialet.getDbmeta().getDbName(), dialet);
		return this;
	}

	public Map<String, DBDialect> getRegister() {
		return dialetRegister;
	}
	
	public DBDialect getRegistered(String name){
		Map<String, DBDialect> register = getRegister();
		if(!register.containsKey(name)){
			throw new DbmException("can not find register DBDialect: " + name);
		}
		return register.get(name);
	}
	
}
