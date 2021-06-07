package org.onetwo.common.db.generator.excel;

import org.onetwo.ext.poi.excel.generator.FieldModel;
import org.onetwo.ext.poi.excel.generator.RowModel;
import org.onetwo.ext.poi.excel.generator.TemplateModel;
import org.onetwo.ext.poi.excel.generator.WorkbookModel;

/**
 * @author weishao zeng
 * <br/>
 */

public interface ExportTableShemaConfigurer {

	default void config(TemplateModel sheet) {}

	default void configTitleRow(RowModel it) {}
	
	default void configNameField(FieldModel sheet) {}
	default void configCommentField(FieldModel sheet) {}

	void configIteratorRow(RowModel it);
	
	default void config(WorkbookModel workbook) {}
	
}
