package org.onetwo.common.db.sqlext;

import java.util.Set;

import org.onetwo.dbm.exception.DbmException;

import com.google.common.collect.Sets;

public class DefaultExtQueryDialetImpl implements ExtQueryDialet {
	
	public static final char[] REPLACE_CHARS = new char[]{
		'.', ',', '(', ')', '+', '-', '*', '/'
	};
	
	final static Set<String> nullsOrderKeys = Sets.newHashSet("nulls last", "nulls first");

	public DefaultExtQueryDialetImpl() {
	}

	@Override
	public String getNamedPlaceHolder(String name, int position) {
		if(name.indexOf(' ')!=-1)
			name = name.replaceAll("[\\s']", "");
		String newName = new StringBuilder(name).append(position).toString();
//		newName = newName.replaceAll("[\\.\\(\\),]", "_");
		for(char ch : REPLACE_CHARS){
			if(newName.indexOf(ch)!=-1){
				newName = newName.replace(ch, '_');
			}
		}
		return newName.toString();
	}

	@Override
	public String getPlaceHolder(int position) {
		return "?";
	}
	
	public String getNullsOrderby(String nullsOrder){
		if (nullsOrderKeys.contains(nullsOrder.toLowerCase())) {
			return nullsOrder;
		}
		throw new DbmException("error nulls order: " + nullsOrder);
	}

	/*@Override
	public String getLockSqlString(LockInfo lock) {
		throw new UnsupportedOperationException();
	}*/

}
