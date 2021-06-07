package org.onetwo.common.db.generator.excel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.onetwo.common.db.generator.dialet.DatabaseMetaDialet;
import org.onetwo.common.db.generator.dialet.DelegateDatabaseMetaDialet;
import org.onetwo.common.db.generator.meta.TableMeta;
import org.onetwo.ext.poi.excel.generator.ExcelGenerators;
import org.onetwo.ext.poi.excel.generator.FieldModel;
import org.onetwo.ext.poi.excel.generator.RowModel;
import org.onetwo.ext.poi.excel.generator.TemplateModel;
import org.onetwo.ext.poi.excel.generator.WorkbookModel;
import org.onetwo.ext.poi.excel.interfaces.TemplateGenerator;

/**
 * @author weishao zeng
 * <br/>
 */

public class ExcelExporter {
	
	public static ExcelExporter create(DataSource dataSource) {
		return new ExcelExporter(dataSource);
	}

	private DataSource dataSource;
	private DatabaseMetaDialet dialet;

	private ExcelExporter(DataSource dataSource) {
		this.dataSource = dataSource;
		this.dialet = new DelegateDatabaseMetaDialet(this.dataSource);
	}
	
	public File exportTableShema(String exportPath, String... tableNames) {
		TableExportParam params = new TableExportParam();
		params.setExportFilePath(exportPath);
		params.addTable(tableNames);
		return exportTableShema(params);
	}
	
	/***
	 * 
				<row name="column" type="iterator" datasource="#table1.columns" renderHeader="true"> 
					<fields>
						<field name=":name" label="字段名称" />
						<field name=":comment" label="注释" />
					</fields>
				</row>
				
	 * @author weishao zeng
	 * @param params
	 * @return
	 */
	public File exportTableShema(TableExportParam params) {
		Map<String, Object> context = new HashMap<>();
		WorkbookModel workbook = new WorkbookModel();

		ExportTableShemaConfigurer configurer = params.getConfigurer();
		int index = 0;
		for (String tableName : params.getTableNames()) {
			TableMeta tableMeta = dialet.getTableMeta(tableName);
			String varName = "table" + index;
			context.put(varName, tableMeta);
			
			TemplateModel sheetModel = new TemplateModel();
			sheetModel.setName(tableMeta.getName());
			sheetModel.setLabel(tableMeta.getName());
			sheetModel.setColumnWidth(params.getColumnWidth());
			if (configurer!=null) {
				configurer.config(sheetModel);
			}
			

			RowModel titleRow = new RowModel();
			titleRow.setFieldHeaderStyle("alignment:@ALIGN_CENTER;verticalAlignment:@VERTICAL_CENTER;");
			titleRow.setFieldHeaderFont("boldweight:@BOLDWEIGHT_BOLD");
			titleRow.setSpan("2");

			FieldModel field = new FieldModel();
			field.setValue("'" + tableMeta.getName() + "\n" + tableMeta.getComment() + "'");
			field.setColspan("2");
			field.setRowspan("2");
//			if (configurer!=null) {
//				configurer.configTitleField(field);
//			}
			titleRow.addField(field);

			if (configurer!=null) {
				configurer.configTitleRow(titleRow);
			}
			sheetModel.addRow(titleRow);
			
			
			RowModel itrow = new RowModel();
			itrow.setName("column"); // varname
			itrow.setFieldHeaderStyle("alignment:@ALIGN_CENTER;verticalAlignment:@VERTICAL_CENTER;");
			itrow.setFieldHeaderFont("boldweight:@BOLDWEIGHT_BOLD");
			itrow.setRenderHeader(true);
			itrow.setType("iterator");
			itrow.setDatasource("#" + varName + ".columns");
			
			field = new FieldModel();
			field.setLabel("字段名称");
			field.setName("name");
			if (configurer!=null) {
				configurer.configNameField(field);
			}
			itrow.addField(field);
			
			field = new FieldModel();
			field.setLabel("注释");
			field.setName("comment");
			if (configurer!=null) {
				configurer.configCommentField(field);
			}
			itrow.addField(field);
			

			if (configurer!=null) {
				configurer.configIteratorRow(itrow);
			}
			sheetModel.addRow(itrow);
			workbook.addSheet(sheetModel);
			
			index++;
		}
		
		if (configurer!=null) {
			configurer.config(workbook);
		}
		
		TemplateGenerator g = ExcelGenerators.createWorkbookGenerator(workbook, context);
//		if (StringUtils.isNotBlank(params.getExportFilePath())) {
//			g.write(params.getExportFilePath());
//		}
		return g.write(params.getExportFilePath());
	}
	
}
