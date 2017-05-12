package org.onetwo.jpa.hibernate;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.sql.Clob;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.transform.AliasedTupleSubsetResultTransformer;
import org.onetwo.common.spring.converter.IntegerToEnumConverterFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.util.Assert;

import com.google.common.collect.Sets;

/**
 * @author weishao zeng
 * <br/>
 */
@SuppressWarnings("serial")
public class HibernateRowToBeanTransformer extends AliasedTupleSubsetResultTransformer {
	private static final FormattingConversionService conversionService;
	
	static {
		FormattingConversionServiceFactoryBean factoryBean = new FormattingConversionServiceFactoryBean();
		factoryBean.setConverters(Sets.newHashSet(new IntegerToEnumConverterFactory()));
	    factoryBean.afterPropertiesSet();
	    conversionService = factoryBean.getObject();
	}
    
	/**
	 * 
	 */
	private final Class<?> resultClass;
	private boolean isInitialized;
	private String[] aliases;
	private String[] propNames;
	private boolean checkAlias;

	private boolean tupleResult;

	public HibernateRowToBeanTransformer(Class<?> resultClass) {
		this(resultClass, true);
	}
	public HibernateRowToBeanTransformer(Class<?> resultClass, boolean checkAlias) {
		if ( resultClass == null ) {
			throw new IllegalArgumentException( "resultClass cannot be null" );
		}
		isInitialized = false;
		this.resultClass = resultClass;
		this.checkAlias = checkAlias;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
		return false;
	}	

	public Object transformTuple(Object[] tuple, String[] aliases) {
		if(tupleResult)
			return tuple;
		
		Object result;

		String propName = "";
		try {
			if ( ! isInitialized ) {
				initialize( aliases );
			}
			else {
				if(checkAlias)
					check( aliases );
			}
			
			try {
				result = resultClass.newInstance();
			} catch (Exception e) {
				//direct return tuple result if error.
				tupleResult = true;
				return tuple;
			}
			
			BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(result);

			Object val;
			for ( int i = 0; i < aliases.length; i++ ) {
				propName = propNames[i];
				if(propName==null)
					continue;
				val = tuple[i];
				
				if(!Map.class.isInstance(result) && !bw.isWritableProperty(propName))
					continue;
				
				Class<?> propertyType = bw.getPropertyType(propName);
				if(propertyType!=null && !Clob.class.isInstance(val))
					val = conversionService.convert(val, propertyType);//Types.convertValue(val, propertyType);
				bw.setPropertyValue(propName, val);
			}
		}
		catch ( Exception e ) {
			throw new RuntimeException( "set bean["+resultClass.getName()+"] property["+propName+"] value error: " + e.getMessage(), e );
		}

		return result;
	}

	private void initialize(String[] aliases) {
		Assert.notEmpty(aliases, "aliases is emtpy!");

		if(Map.class.isAssignableFrom(resultClass)){
			this.propNames = aliases.clone();
			this.aliases = aliases.clone();
			return ;
		}
		
		this.aliases = new String[ aliases.length ];
		this.propNames = new String[ aliases.length ];
		
		Map<String, PropertyDescriptor> props = desribProperties(resultClass);
		Set<String> resultPropNames = props.keySet();
		
		for ( int i = 0; i < aliases.length; i++ ) {
			String alias = aliases[ i ];
			if ( alias != null ) {
				this.aliases[i] = alias;
				String propName = alias; 
				if(resultPropNames.contains(propName)){
					this.propNames[i] = propName;
				}
			}
		}
		isInitialized = true;
	}
	

	static private Map<String, PropertyDescriptor> desribProperties(Class<?> clazz){
		if(clazz==Object.class || clazz.isInterface() || clazz.isPrimitive())
			return Collections.emptyMap();
		
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(clazz, Object.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
		
		Map<String, PropertyDescriptor> maps = new LinkedHashMap<String, PropertyDescriptor>();
		for(PropertyDescriptor prop : props){
			maps.put(prop.getName(), prop);
		}
		
		return Collections.unmodifiableMap(maps);
	}

	private void check(String[] aliases) {
		if ( ! Arrays.equals( aliases, this.aliases ) ) {
			throw new IllegalStateException(
					"aliases are different from what is cached; aliases=" + Arrays.asList( aliases ) +
							" cached=" + Arrays.asList( this.aliases ) );
		}
	}

	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		HibernateRowToBeanTransformer that = ( HibernateRowToBeanTransformer ) o;

		if ( ! resultClass.equals( that.resultClass ) ) {
			return false;
		}
		if ( ! Arrays.equals( aliases, that.aliases ) ) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result = resultClass.hashCode();
		result = 31 * result + ( aliases != null ? Arrays.hashCode( aliases ) : 0 );
		return result;
	}

	public void setCheckAlias(boolean checkAlias) {
		this.checkAlias = checkAlias;
	}
	
}

