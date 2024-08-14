package org.onetwo.dbm.dialet;

import org.onetwo.common.db.DataBase;
import org.onetwo.common.utils.StringUtils;

/**
 * @author weishao zeng
 * <br/>
 */
public class PostgreSQLDialet extends AbstractDBDialect {

	public PostgreSQLDialet(){
		super(DBMeta.create(DataBase.PostgreSQL));
	}

	@Override
	public String getLimitString(String sql, String offsetName, String maxResultSizeName) {
		StringBuilder sb = new StringBuilder();
		sb.append( sql );
		if(StringUtils.isNotBlank(offsetName) && StringUtils.isNotBlank(maxResultSizeName))
			sb.append(" limit :").append(maxResultSizeName).append(" offset :").append(offsetName);
		else{
			sb.append(" limit ? offset ?");
		}
		return sb.toString();
	}

}

