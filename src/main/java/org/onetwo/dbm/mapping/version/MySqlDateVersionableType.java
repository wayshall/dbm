package org.onetwo.dbm.mapping.version;

import java.util.Date;

import org.onetwo.common.db.DataBase;
import org.onetwo.dbm.dialet.DBDialect;

/***
 * fix for mysql datetime
 * 
 * @author way
 *
 */
public class MySqlDateVersionableType extends DateVersionableType {

	@Override
	public boolean isSupportType(DBDialect dbDialect, Class<?> type) {
		return Date.class.isAssignableFrom(type) && dbDialect.getDbmeta().getDataBase()==DataBase.MySQL;
	}

	/***
	 * 因为mysql的datetime字段只能精确到秒，所以保存的需要精确到秒，否则java对象的版本值会和数据库的版本值不一致，从而导致根据版本值update的时候找不到记录，抛出EntityVersionException
	 */
	@Override
	public Date getVersionValule(Date oldVersion) {
		return new Date(new Date().getTime()/1000*1000);
	}

	/*@Override
	public boolean isEquals(Date newVersion, Date lastVersion) {
		return newVersion!=null && new Date(newVersion.getTime()/1000*1000).equals(new Date(lastVersion.getTime()/1000*1000));
	}*/

}
