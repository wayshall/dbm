package org.onetwo.common.db.spi;
/**
 * @author wayshall
 * <br/>
 */
public class CreateQueryCmd {
	
	final private String sql;
	final private Class<?> mappedClass;
	final private boolean nativeSql;
	
	public CreateQueryCmd(String sql, Class<?> mappedClass) {
		this(sql, mappedClass, true);
	}
	
	public CreateQueryCmd(String sql, Class<?> mappedClass, boolean nativeSql) {
		super();
		this.sql = sql;
		this.mappedClass = mappedClass;
		this.nativeSql = nativeSql;
	}

	public String getSql() {
		return sql;
	}

	public Class<?> getMappedClass() {
		return mappedClass;
	}

	public boolean isNativeSql() {
		return nativeSql;
	}

}
