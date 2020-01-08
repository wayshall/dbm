package org.onetwo.dbm.ui.meta;

import org.onetwo.common.db.generator.meta.ColumnMeta;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.onetwo.dbm.ui.annotation.UISelect.NoEnums;
import org.onetwo.dbm.ui.annotation.UISelect.NoProvider;
import org.onetwo.dbm.ui.core.UISelectDataProvider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author weishao zeng
 * <br/>
 */
@Data
@AllArgsConstructor
@Builder
public class UIFieldMeta {
	
	UIClassMeta classMeta;
	DbmMappedField dbmField;
	ColumnMeta column;
	
	String name;
	String label;
	boolean listable;
	boolean insertable;
	boolean updatable;
	boolean searchable;
	
	UISelectMeta select;
	
	public String getFormDisabledValue() {
		if (insertable && updatable) {
			return "false";
		} else if (insertable) {
			return "statusMode!=='Add'";
		} else if (updatable) {
			return "statusMode!=='Edit'";
		} else {
			return "true";
		}
	}
	

	@Data
	public class UISelectMeta {
		Class<? extends Enum<?>> dataEnumClass;
		Class<? extends UISelectDataProvider> dataProvider;
		String labelField;
		String valueField;
		
		public boolean useEnumData() {
			return dataEnumClass!=null && dataEnumClass!=NoEnums.class;
		}
		
		public boolean useDataProvider() {
			return dataProvider!=null && dataProvider!=NoProvider.class;
		}
		
		public UIFieldMeta getField() {
			return UIFieldMeta.this;
		}
	}

}
