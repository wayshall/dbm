package org.onetwo.dbm.utils;

import java.beans.PropertyDescriptor;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.onetwo.common.date.DateUtils;
import org.onetwo.common.date.Dates;
import org.onetwo.common.date.NiceDate;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.common.utils.JFishPropertyInfoImpl;
import org.onetwo.dbm.jdbc.spi.JdbcParameterValue;
import org.onetwo.dbm.mapping.DbmEnumValueMapping;
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
			Enum<?> enumValue = (Enum<?>)value;
			if(!Map.class.isInstance(paramBean.getWrappedInstance()) && paramBean.isReadableProperty(property)){
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
				if(enumerated!=null){
					value = enumerated.value()==EnumType.ORDINAL?enumValue.ordinal():enumValue.name();
				}else{
					value = enumValue.name();
				}
			}else{
//				value = enumValue.name();
			}
		}
		return value;
	}
	
	public static Object getActualValue(Object value){
		if(SqlParameterValue.class.isInstance(value)){
			return ((SqlParameterValue)value).getValue();
		} else if (value instanceof DbmEnumValueMapping) {
			DbmEnumValueMapping<?> dvm = (DbmEnumValueMapping<?>) value;
			return dvm.getEnumMappingValue();
		} else if(Enum.class.isInstance(value)){
			return ((Enum<?>)value).name();
		}else if(value instanceof LocalDate){
			final LocalDate localDate = (LocalDate) value;
			return new DateSqlTypeValue<java.sql.Date>(new java.sql.Date(Dates.toDate(localDate).getTime())){

				@Override
				public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName) throws SQLException {
//					ps.setDate(paramIndex, new java.sql.Date(Dates.toDate(localDate).getTime()));
					ps.setDate(paramIndex, getValue());
				}
				
			};
		}else if(value instanceof LocalDateTime){
			final LocalDateTime localDateTime = (LocalDateTime) value;
			return new DateSqlTypeValue<Timestamp>(new Timestamp(Dates.toDate(localDateTime).getTime())){
				@Override
				public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName) throws SQLException {
					ps.setTimestamp(paramIndex, getValue());
				}
				
			};
			/*return new SqlTypeValue(){

				@Override
				public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName) throws SQLException {
					ps.setTimestamp(paramIndex, new Timestamp(Dates.toDate(localDateTime).getTime()));
				}
				
			};*/
			
		} else if (value instanceof JdbcParameterValue){
			return ((JdbcParameterValue)value).toParameterValue();
		 }else if (value instanceof NiceDate){
			return ((NiceDate)value).getTime();
		}
		return value;
	}
	
	abstract public static class DateSqlTypeValue<T extends Date> implements SqlTypeValue {
		final private T value;

		public DateSqlTypeValue(T value) {
			super();
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "" + DateUtils.formatDateTime(value) + "";
		}
		
	}
	
	private JdbcParamValueConvers(){
	}

}
