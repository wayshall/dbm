package org.onetwo.dbm.jdbc.mapper.nested;

import java.util.Collection;

import org.onetwo.common.reflect.Intro;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.mapper.nested.AbstractNestedBeanMapper.DbmNestedResultData;
import org.springframework.beans.BeanWrapper;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CollectionPropertyResultClassMapper extends PropertyResultClassMapper {
	
	private Intro<? extends Collection> collectionClassIntro;
	public CollectionPropertyResultClassMapper(
			ResultClassMapper parentMapper, 
			DbmNestedResultData nestedResultData, String columnPrefix,
			JFishProperty belongToProperty) {
		super(parentMapper, nestedResultData, columnPrefix, belongToProperty, (Class<?>)belongToProperty.getFirstParameterType());
		if(belongToProperty.getFirstParameterType()==null){
			throw new DbmException("the collection property must be a parameterType: " + belongToProperty.getName());
		}
		collectionClassIntro = (Intro<? extends Collection>)belongToProperty.getTypeClassWrapper();
		if(!collectionClassIntro.isCollection()){
			throw new DbmException("the nested property ["+belongToProperty.getName()+"] must be Collection Type: " + belongToProperty.getName());
		}
	}
	
	public void linkToParent(BeanWrapper parent, Object propertyValue){
		if(propertyValue==null){
			return ;
		}
		
		propertyValue = getPropertyValue(propertyValue);
		String propName = getBelongToProperty().getName();
		Collection values = (Collection)parent.getPropertyValue(propName);
		if(values==null){
			values = collectionClassIntro.newInstance();
			parent.setPropertyValue(propName, values);
		}
		if(!values.contains(propertyValue)){
			values.add(propertyValue);
		}
	}
}
