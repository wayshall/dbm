package org.onetwo.dbm.mapping;

public enum DbmEnumType {
    ORDINAL(int.class),
    STRING(String.class);
    
    private final Class<?> javaType;

	private DbmEnumType(Class<?> javaType) {
		this.javaType = javaType;
	}

	public Class<?> getJavaType() {
		return javaType;
	}
    
}
