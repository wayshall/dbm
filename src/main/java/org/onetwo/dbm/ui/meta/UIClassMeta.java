package org.onetwo.dbm.ui.meta;
/**
 * @author weishao zeng
 * <br/>
 */

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.onetwo.common.db.generator.meta.TableMeta;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.ui.exception.DbmUIException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UIClassMeta {
	
	private String name;
	private String label;
	
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	final private Map<String, UIFieldMeta> fieldMap = Maps.newLinkedHashMap();
	private List<UIFieldMeta> fields = Lists.newArrayList();
	 
//	private Collection<UIFieldMeta> fields = Sets.newTreeSet(Comparator.comparingInt(f -> f.getOrder()));
	private DbmMappedEntry mappedEntry;
	private TableMeta table;
	
	public void addField(UIFieldMeta field) {
		fieldMap.put(field.getName(), field);
		fields.add(field);
		Collections.sort(fields, Comparator.comparingInt(f -> f.getOrder()));
	}
	
	public UIFieldMeta getField(String fieldName) {
		UIFieldMeta field =  fieldMap.get(fieldName);
		if (field==null) {
			throw new DbmUIException("ui field not found for name: " + fieldName);
		}
		return field;
	}
	
	public Collection<UIFieldMeta> getFields() {
		return fields;
	}
	
	public Collection<UIFieldMeta> getListableFields() {
		return getFields().stream().filter(f -> f.isListable()).collect(Collectors.toList());
	}
	
	public Collection<UIFieldMeta> getSearchableFields() {
		return getFields().stream().filter(f -> f.isSearchable()).collect(Collectors.toList());
	}
	
	public Collection<UIFieldMeta> getFormFields() {
		return getFields().stream().filter(f -> f.isInsertable() || f.isUpdatable()).collect(Collectors.toList());
	}
	

}
