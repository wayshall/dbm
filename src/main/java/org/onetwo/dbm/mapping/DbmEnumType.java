package org.onetwo.dbm.mapping;

import org.onetwo.dbm.mapping.enums.OrdinalEnumType;
import org.onetwo.dbm.mapping.enums.StringEnumType;

public interface DbmEnumType {
	DbmEnumType ORDINAL = new OrdinalEnumType();
    DbmEnumType STRING = new StringEnumType();
    
    /***
     * 获取枚举实际映射的java类型
     * @author weishao zeng
     * @return
     */
    Class<?> getJavaType();
    
	Object forJava(DbmMappedField field, Object value);
	
	Object forStore(DbmMappedField field, Object value);
    
}
