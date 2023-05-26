package org.onetwo.dbm.jdbc.mapper.nested;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.convert.Types;
import org.onetwo.common.reflect.Intro;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.mapper.nested.AbstractNestedBeanMapper.DbmNestedResultData;
import org.springframework.beans.BeanWrapper;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapPropertyResultClassMapper extends PropertyResultClassMapper {
	private Intro<? extends Map> mapClassIntro;
	private Class<?> keyClass;
	public MapPropertyResultClassMapper(
			ResultClassMapper parentMapper, 
			DbmNestedResultData nestedResultData, String columnPrefix,
			JFishProperty belongToProperty) {
		super(parentMapper, nestedResultData, columnPrefix, belongToProperty, (Class<?>)belongToProperty.getParameterType(1));
		if(StringUtils.isBlank(nestedResultData.getId())){
			throw new DbmException("you must configure the id property for map : " + belongToProperty.getName());
		}
		this.keyClass = (Class<?>)belongToProperty.getParameterType(0);
		if(keyClass==null || getResultClass()==null){
			throw new DbmException("the Map property must be a parameterType: " + belongToProperty.getName());
		}
		mapClassIntro = (Intro<? extends Map>)belongToProperty.getTypeClassWrapper();
		if(!mapClassIntro.isMap()){
			throw new DbmException("the nested property ["+belongToProperty.getName()+"] must be Map Type: " + belongToProperty.getName());
		}
	}
	
	@Override
	public void initialize() {
		super.initialize();
		if(getIdProperty()==null){
			throw new DbmException("the configured id property["+getIdPropertyName()+"] not found in : " + getResultClass());
		}
	}
	
	public void linkToParent(BeanWrapper parent, Object propertyValue){
		if(propertyValue==null){
			return ;
		}
		if(!hasIdField()){
			throw new DbmException("no id configured for map : " + this.getBelongToProperty().getName());
		}
		Object id = ReflectUtils.getPropertyValue(propertyValue, getIdProperty().getName());
		if(id==null){
			throw new DbmException("id value can not be null for map : " + this.getBelongToProperty().getName());
		}
		String propName = getBelongToProperty().getName();
		id = Types.convertValue(id, keyClass);
		Map values = (Map)parent.getPropertyValue(getBelongToProperty().getName());
		if(values==null){
			values = mapClassIntro.newInstance();
			parent.setPropertyValue(propName, values);
		}
		values.put(id, propertyValue);
	}
}
