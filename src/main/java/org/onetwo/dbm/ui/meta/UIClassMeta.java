package org.onetwo.dbm.ui.meta;
/**
 * @author weishao zeng
 * <br/>
 */

import java.util.Map;

import org.onetwo.dbm.ui.exception.DbmUIException;

import com.google.common.collect.Maps;

import lombok.Data;

@Data
public class UIClassMeta {
	
	private String name;
	final private Map<String, UIFieldMeta> fieldMap = Maps.newHashMap();
	
	public void addField(UIFieldMeta field) {
		fieldMap.put(field.getName(), field);
	}
	
	public UIFieldMeta getField(String fieldName) {
		UIFieldMeta field =  fieldMap.get(fieldName);
		if (field==null) {
			throw new DbmUIException("ui field not found for name: " + fieldName);
		}
		return field;
	}

}
