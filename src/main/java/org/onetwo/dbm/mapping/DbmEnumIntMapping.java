package org.onetwo.dbm.mapping;
/**
 * 使用枚举类型，又不想使用枚举的ORDINAL作为存储值的时候，可以实现这个接口做映射
 * @author weishao zeng
 * <br/>
 */
public interface DbmEnumIntMapping extends DbmEnumValueMapping<Integer>{
	

	default Integer getEnumMappingValue() {
		return getMappingValue();
	}
	int getMappingValue();

}

