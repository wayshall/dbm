package org.onetwo.dbm.dialet;

import org.onetwo.common.db.DataBase;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.event.internal.DefaultCoreEventListenerManager;
import org.onetwo.dbm.id.StrategyType;

public class MySQLDialect extends AbstractDBDialect {

	public MySQLDialect(){
		super(DBMeta.create(DataBase.MySQL));
	}


	public void registerIdStrategy(){
		this.getIdStrategy().add(StrategyType.IDENTITY);
		this.getIdStrategy().add(StrategyType.TABLE);
		this.getIdStrategy().add(StrategyType.DBM);
	}
	
	public String getLimitString(String sql, String firstName, String maxResultName) {
		StringBuilder sb = new StringBuilder();
		sb.append( sql );
		if(StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(maxResultName))
			sb.append(" limit :").append(firstName).append(", :").append(maxResultName);
		else{
			sb.append(" limit ?, ?");
		}
		return sb.toString();
	}
	

	protected String getReadLockString(int timeoutInMillis) {
		return "lock in share mode";
	}

	@Override
	protected void onDefaultDbEventListenerManager(DefaultCoreEventListenerManager listMg){
		super.onDefaultDbEventListenerManager(listMg);
//		listMg.registerListeners(DbmEventAction.insertOrUpdate, new MySQLInsertOrUpdateListener());
	}

	/*protected DbEventListenerManager createDefaultDbEventListenerManager() {
		JFishdbEventListenerManager listenerManager = new JFishdbEventListenerManager();
		return listenerManager;
	}
	*/
	

}
