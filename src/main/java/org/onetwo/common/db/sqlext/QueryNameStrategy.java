package org.onetwo.common.db.sqlext;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;

/**
 * @author wayshall
 * <br/>
 */
public class QueryNameStrategy {
	public static final String[] SQL_KEY_WORKDS = new String[]{" ", ";", ",", "(", ")", "'", "\"\"", "/", "+", "-"};

	protected String alias;
	protected boolean aliasMainTableName = true;
	
	public QueryNameStrategy(String alias) {
		this(alias, true);
	}

	public QueryNameStrategy(String alias, boolean aliasMainTableName) {
		super();
		this.alias = alias;
		this.aliasMainTableName = aliasMainTableName;
	}


	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setAliasMainTableName(boolean aliasMainTableName) {
		this.aliasMainTableName = aliasMainTableName;
	}

	public boolean isAliasMainTableName() {
		return aliasMainTableName;
	}

	public String getFromName(Class<?> entityClass){
		return entityClass.getName();
	}
	
	public String getFieldName(String f) {
		Assert.hasText(f);
		f = appendAlias(translateAt(f));
		checkFieldNameValid(f);
		return f;
	}
	
	
	public String getJoinFieldName(String f) {
		Assert.hasText(f);
		f = translateAt(f);
		checkFieldNameValid(f);
		return f;
	}

	public String translateAt(String f){
		if(f.indexOf(K.PREFIX_REF)!=-1){
			f = f.replace(K.PREFIX_REF, this.alias+".");
		}
		return f;
	}

	public String appendAlias(String f){
		if(StringUtils.isBlank(this.alias)){
			return f;
		}
		String newf = f;
		if(f.startsWith(K.NO_PREFIX)){
			newf = f.substring(K.NO_PREFIX.length());
		}else{
			if(isAliasMainTableName()){
				if(!f.startsWith(this.alias + "."))
					f = this.alias + "." + f;
			}
			
			newf = f;
		}
		return newf;
	}

	protected String checkFieldNameValid(String field){
		Assert.hasText(field);
		for(String str : SQL_KEY_WORKDS){
			if(field.indexOf(str)!=-1)
				LangUtils.throwBaseException("the field is inValid : " + field);
		}
		return field;
	}
}
