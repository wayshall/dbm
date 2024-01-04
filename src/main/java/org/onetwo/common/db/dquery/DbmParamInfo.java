package org.onetwo.common.db.dquery;

import jakarta.persistence.EnumType;

import lombok.Data;

/**
 * 对应 org.onetwo.common.db.dquery.annotation.Param 注解
 * @author weishao zeng
 * <br/>
 */
@Data
public class DbmParamInfo {

	private String name;
	
	/***
	 * 如果参数是数组和列表，使用name+index重新生成参数名称
	 * cardNo in ( ${_func.inParams('cardNo', cardNos.size())} )
	 * 
	 * @deprecated 已直接支持List参数
	 * @return
	 */
	@Deprecated
	private boolean renamedUseIndex = false;
	
	private boolean isLikeQuery = false;
	
	private EnumType enumType = EnumType.STRING;
	
	public EnumType enumType() {
		return enumType;
	}
	
	public boolean renamedUseIndex() {
		return renamedUseIndex;
	}
}
