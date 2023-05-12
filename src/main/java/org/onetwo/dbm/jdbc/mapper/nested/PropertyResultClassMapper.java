package org.onetwo.dbm.jdbc.mapper.nested;

import org.onetwo.common.utils.JFishProperty;
import org.onetwo.dbm.jdbc.mapper.nested.AbstractNestedBeanMapper.DbmNestedResultData;
import org.springframework.beans.BeanWrapper;

public class PropertyResultClassMapper extends ResultClassMapper {
	final private ResultClassMapper parentMapper;
	/***
	 * 映射对象所属的父对象的属性
	 * 比如：
	 * class Parent {
	 * 		List<Children> children;
	 * }
	 * 则Children对应的PropertyResultClassMapper的belongToProperty为children属性
	 */
	final private JFishProperty belongToProperty;
	private DbmNestedResultData nestedResultData;
	
	public PropertyResultClassMapper(ResultClassMapper parentMapper, DbmNestedResultData nestedResultData, String columnPrefix, JFishProperty belongToProperty) {
		this(parentMapper, nestedResultData, columnPrefix, belongToProperty, belongToProperty.getType());
	}
	public PropertyResultClassMapper(ResultClassMapper parentMapper, DbmNestedResultData nestedResultData, String columnPrefix, JFishProperty belongToProperty, Class<?> propertyType) {
		super(parentMapper.context, nestedResultData.getId(), columnPrefix, propertyType);
		this.nestedResultData = nestedResultData;
		this.belongToProperty = belongToProperty;
		this.parentMapper = parentMapper;
		this.accessPathPrefix = getAcessPath(parentMapper.accessPathPrefix, belongToProperty.getName());
	}
	public JFishProperty getBelongToProperty() {
		return belongToProperty;
	}
	public void linkToParent(BeanWrapper parent, Object propertyValue){
		if(propertyValue!=null){
			parent.setPropertyValue(belongToProperty.getName(), propertyValue);
		}
	}
	
	protected boolean isIgnoreIfIdColumnNotFound() {
		return nestedResultData.isIgnoreIfIdColumnNotFound();
	}
	
	public ResultClassMapper getParentMapper() {
		return parentMapper;
	}
	public Object getPropertyValue(Object propertyValue){
		if(propertyValue instanceof SimpleValueNestedMappingHoder){
			propertyValue = ((SimpleValueNestedMappingHoder)propertyValue).value;
		}
		return propertyValue;
	}
}
