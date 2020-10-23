package org.onetwo.common.db.generator.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.common.utils.map.CaseInsensitiveMap;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;


public class TableMeta {

	private String name;
	private ColumnMeta primaryKey;

	private Map<String, ColumnMeta> columnMap = new CaseInsensitiveMap<String, ColumnMeta>();
	
	private String comment;
	
	private String stripPrefix;

	
	public TableMeta(String name, String comment){
//		this.entry = entry;
		Assert.hasText(name, "table name must has text");
		this.name = name;
		this.comment = comment;
	}
	
	public List<ColumnMeta> getAssociationTypeColumns(){
		return columnMap.values()
						.stream()
						.filter(col->col.isAssociationType())
						.collect(Collectors.toList());
	}
	
	public String getShortName(){
		return tableNameStripStart(stripPrefix);
	}
	
	public String getTableName(){
		return getName();
	}
	
	@Deprecated
	public String getTableClassName(){
		return getClassName();
	}
	
	public String getClassName(){
		return StringUtils.toClassName(getShortName());
	}
	
	public String getPropertyName(){
		String shortName = getShortName();
		return StringUtils.toPropertyName(shortName);
	}
	
	/***
	 * 横杠分割的名称
	 * @author weishao zeng
	 * @return
	 */
	public String getHorizontalBarName(){
		return StringUtils.convertWithSeperator(getPropertyName(), "-");
	}

	
	public String getComment() {
		return comment;
	}


	public String getName() {
		return this.name;
	}
	
	public String tableNameStripStart(String stripChars){
		if(StringUtils.isBlank(stripChars))
			return name;
//		return StringUtils.stripStart(name.toLowerCase(), stripChars.toLowerCase());
		return name.toLowerCase().substring(stripChars.length());
	}

	public Map<String, ColumnMeta> getColumnMap() {
		return ImmutableMap.copyOf(columnMap);
	}

	public Collection<ColumnMeta> getColumns() {
		return columnMap.values();
	}

	public Collection<ColumnMeta> getColumnCollection() {
		return new ArrayList<ColumnMeta>(columnMap.values());
	}

	public Collection<ColumnMeta> getSelectableColumns() {
		List<ColumnMeta> cols = LangUtils.newArrayList();
		for(ColumnMeta col : this.columnMap.values()){
			cols.add(col);
		}
		return cols;
	}

	public ColumnMeta getColumn(String name) {
		return columnMap.get(name);
	}

	public boolean hasColumn(String name) {
		return columnMap.containsKey(name);
	}

	public void setColumns(Map<String, ColumnMeta> columns) {
		columns.values().forEach(col->{
			addColumn(col);
		});
	}

	public TableMeta addColumn(ColumnMeta column) {
		column.setTable(this);
		if(column.isPrimaryKey()){
			column.setPrimaryKey(true);
		}
		this.columnMap.put(column.getName(), column);
		return this;
	}

	public ColumnMeta getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(ColumnMeta primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	public List<String> getComments() {
		Iterable<String> it = Splitter.on('\n').trimResults().omitEmptyStrings().split(comment);
		return CUtils.iterableToList(it);
	}

	public String getStripPrefix() {
		return stripPrefix;
	}

	public void setStripPrefix(String stripPrefix) {
		this.stripPrefix = stripPrefix;
	}

	@Override
	public String toString() {
		return "TableMeta [name=" + name + ", primaryKey=" + primaryKey
				+ ", columns=" + columnMap + ", comment=" + comment + "]";
	}

}
