package org.onetwo.common.db.generator.excel;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import lombok.Data;

/**
 * @author weishao zeng
 * <br/>
 */
@Data
public class TableExportParam {
	
	final List<String> tableNames = Lists.newArrayList();
	String exportFilePath;
	ExportTableShemaConfigurer configurer;
	
	String columnWidth = "0:50;1:100";
	
	public TableExportParam addTable(String... tables) {
		tableNames.addAll(Arrays.asList(tables));
		return this;
	}

}
