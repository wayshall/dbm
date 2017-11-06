package org.onetwo.dbm.utils;

import java.beans.PropertyDescriptor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.onetwo.common.date.Dates;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.common.utils.JFishPropertyInfoImpl;
import org.onetwo.dbm.jdbc.spi.JdbcParameterValue;
import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;

/**
 * @author wayshall
 * <br/>
 */
final public class JdbcParamValueConvers {
	

	public static Object getParamterValue(BeanWrapper paramBean, String property){
		Object value = paramBean.getPropertyValue(property);
		if(value instanceof Enum){
			PropertyDescriptor pd = paramBean.getPropertyDescriptor(property);
			JFishProperty jproperty = new JFishPropertyInfoImpl(pd);
			Enumerated enumerated = null;
			if(jproperty.hasAnnotation(Enumerated.class)){
				enumerated = jproperty.getAnnotation(Enumerated.class);
			}else{
				enumerated = jproperty.getCorrespondingJFishProperty()
										.map(jp->jp.getAnnotation(Enumerated.class))
										.orElse(null);
			}
			Enum<?> enumValue = (Enum<?>)value;
			if(enumerated!=null){
				value = enumerated.value()==EnumType.ORDINAL?enumValue.ordinal():enumValue.name();
			}else{
				value = enumValue.name();
			}
		}
		return value;
	}
	
	public static Object getActualValue(Object value){
		if(SqlParameterValue.class.isInstance(value)){
			return ((SqlParameterValue)value).getValue();
		}else if(Enum.class.isInstance(value)){
			return ((Enum<?>)value).name();
		}else if(value instanceof LocalDate){
			final LocalDate localDate = (LocalDate) value;
			return new SqlTypeValue(){

				@Override
				public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName) throws SQLException {
					ps.setDate(paramIndex, new java.sql.Date(Dates.toDate(localDate).getTime()));
				}
				
			};
		}else if(value instanceof LocalDateTime){
			final LocalDateTime localDateTime = (LocalDateTime) value;
			return new SqlTypeValue(){

				@Override
				public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName) throws SQLException {
					ps.setTimestamp(paramIndex, new Timestamp(Dates.toDate(localDateTime).getTime()));
				}
				
			};
			
		}else if(value instanceof JdbcParameterValue){
			return ((JdbcParameterValue)value).toParameterValue();
		}
		return value;
	}
	
	private JdbcParamValueConvers(){
	}

}
