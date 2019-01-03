package org.onetwo.common.db.sqlext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wayshall
 * <br/>
 */
public class SelectQueryNameStrategy extends QueryNameStrategy {
	protected Map<String, String> joinMapped = new HashMap<>();

	public SelectQueryNameStrategy(String alias, Map<String, String> joinMapped, boolean aliasMainTableName) {
		super(alias, aliasMainTableName);
		this.joinMapped = joinMapped;
	}
	public String getFieldName(String f) {
		int firstIndex = f.indexOf('.');
		if(firstIndex!=-1){
			String firstWord = f.substring(0, firstIndex);
			if(joinMapped.containsKey(firstWord))
				return f;
		}
		return super.getFieldName(f);
	}
}
