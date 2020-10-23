package org.onetwo.dbm.dialet;

import org.onetwo.common.db.DataBase;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.id.StrategyType;

/**
 * TODO: 先直接使用MySQLDialect,未做任何适配
 * @author weishao zeng
 * <br/>
 */
public class H2Dialet extends AbstractDBDialect {

	public H2Dialet(){
		super(DBMeta.create(DataBase.H2));
	}

	@Override
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
	
}

