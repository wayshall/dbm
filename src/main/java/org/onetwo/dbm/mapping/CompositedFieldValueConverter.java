package org.onetwo.dbm.mapping;

import java.util.Arrays;
import java.util.List;

import org.onetwo.common.convert.Types;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.exception.DbmException;

import com.google.common.collect.Lists;

/**
 * @author wayshall
 * <br/>
 */
public class CompositedFieldValueConverter implements DbmFieldValueConverter {
	
	public static DbmFieldValueConverter ENUM_CONVERTER = new DbmFieldValueConverter() {
		
		@Override
		public Object forJava(DbmMappedField field, Object value) {
			if(value==null){
				return value;
			}
			Object actualValue = null;
			DbmEnumType etype = field.getEnumType();
			if(etype==DbmEnumType.ORDINAL){
//				actualValue = Types.convertValue(field.getColumnType().cast(value), field.getPropertyInfo().getType());
				actualValue = Types.convertValue((Integer)value, field.getPropertyInfo().getType());
			}else if(etype==DbmEnumType.STRING){
				actualValue = Types.convertValue(value.toString(), field.getPropertyInfo().getType());
			}else{
				throw new DbmException("error enum type: " + etype);
			}
			return actualValue;
		}
		
		@Override
		public Object forStore(DbmMappedField field, Object value) {
			if(value==null){
				return value;
			}
			Object actualValue = null;
			DbmEnumType etype = field.getEnumType();
			Enum<?> enumValue = (Enum<?>) value;
			if(etype==DbmEnumType.ORDINAL){
				actualValue = enumValue.ordinal();
			}else if(etype==DbmEnumType.STRING){
				actualValue = enumValue.name();
			}else{
				throw new DbmException("error enum type: " + etype);
			}
			return actualValue;
		}
	};

	public static CompositedFieldValueConverter composited(DbmFieldValueConverter... converters) {
		List<DbmFieldValueConverter> list = Lists.newArrayList();
//		list.add(ENUM_CONVERTER);
		if(!LangUtils.isEmpty(converters)){
			list.addAll(Arrays.asList(converters));
		}
		return new CompositedFieldValueConverter(list);
	}
	
	private List<DbmFieldValueConverter> converters;
	
	private CompositedFieldValueConverter(List<DbmFieldValueConverter> converters) {
		super();
		this.converters = converters;
	}
	
	CompositedFieldValueConverter addFieldValueConverter(DbmFieldValueConverter converter){
		this.converters.add(converter);
		return this;
	}

	@Override
	public Object forJava(DbmMappedField field, Object fieldValue) {
		List<DbmFieldValueConverter> converters = this.converters;
		if(converters.isEmpty()){
			return fieldValue;
		}
		Object newValue = fieldValue;
		for(DbmFieldValueConverter converter : converters){
			newValue = converter.forJava(field, newValue);
		}
		return newValue;
	}

	@Override
	public Object forStore(DbmMappedField field, Object fieldValue) {
		List<DbmFieldValueConverter> converters = this.converters;
		if(converters.isEmpty()){
			return fieldValue;
		}
		Object newValue = fieldValue;
		for(DbmFieldValueConverter converter : converters){
			newValue = converter.forStore(field, newValue);
		}
		return newValue;
	}
	
	

}
