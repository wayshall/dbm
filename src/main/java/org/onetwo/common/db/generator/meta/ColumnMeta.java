package org.onetwo.common.db.generator.meta;

import java.sql.Time;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.onetwo.common.db.generator.mapping.ColumnMapping;
import org.onetwo.common.db.generator.utils.DbGeneratorUtills;
import org.onetwo.common.db.generator.utils.UITypes;
import org.onetwo.common.jackson.JsonMapper;
import org.onetwo.common.utils.GuavaUtils;
import org.onetwo.common.utils.StringUtils;

import com.google.common.collect.Lists;

public class ColumnMeta {

	protected TableMeta table;
	private String name;
//	protected int sqlType = DBUtils.TYPE_UNKNOW;

	protected boolean primaryKey;
//	protected boolean referencedKey;
	
	protected boolean nullable;
	private String comment;
	private List<String> comments;
	private Map<String, String> commentsInfo = Collections.emptyMap();
	private String commentName;
	protected int columnSize;
	

	protected String javaName;
//	protected Class<?> javaType;
	
	protected ColumnMapping mapping;
	
	public ColumnMeta(TableMeta tableInfo, String name, ColumnMapping mapping) {
		setName(name);
//		setSqlType(sqlType);
//		setJavaType(javaType);
		this.mapping = mapping;
		this.mapping.setColumnMeta(this);
	}
	
	public void init(){
		this.commentsInfo = DbGeneratorUtills.parse(comment);
		this.comments = Arrays.asList(GuavaUtils.split(comment, '\n'));
		if(!comments.isEmpty()){
			this.commentName = comments.get(0);
		}
	}
	
	public Class<?> getMappingJavaClass(){
		Class<?> mappingClass = getJavaType();
		if(mapping.isDateType() || mapping.isSqlTimestamp()){
			mappingClass = Date.class;
		}else if(mapping.isSqlDate()){
			mappingClass = java.sql.Date.class;
		}else if(mapping.isSqlTime()){
			mappingClass = Time.class;
		}else if(mapping.isBooleanType()){
			mappingClass = Boolean.class;
		}else if(mapping.isSqlFloat()){
			mappingClass = Float.class;
		}
		return mappingClass;
	}
	
	public String getCommentName(){
		return commentName;
	}

	public String getUiType(){
		String type = this.commentsInfo.get(UITypes.KEY_TYPE);
		return type==null?"":type;
	}
	
	public boolean isUiType(String key){
		return this.commentsInfo.containsKey(key) || getUiType().equals(key);
	}
	
	public boolean isDictType(){
		//<#elseif column.commentsInfo['字典类型']??>
		return isUiType(UITypes.KEY_DICT);
	}
	public boolean isDbDictType(){
		//<#elseif column.commentsInfo['字典类型']??>
		return isUiType(UITypes.KEY_DB_DICT);
	}

	public List<OptionData> getDictData(){
		List<OptionData> datas = Lists.newArrayList();
		boolean optionStart = false;
		for(Entry<String, String> entry : this.commentsInfo.entrySet()){
			if(optionStart){
				datas.add(new OptionData(entry.getKey(), entry.getValue()));
			}else if(entry.getKey().contains(UITypes.KEY_DICT) || entry.getKey().equals(UITypes.KEY_TYPE)){
				optionStart = true;
			}
		}
		return datas;
	}
	
	public String getDictDataJson(){
		return JsonMapper.ignoreNull()
						.singleQuotes()
						.unquotedFieldNames()
						.toJson(getDictData())
						//TODO: jackson不支持单引号输出，只能这样先fixed……
						.replace("\"", "'");
	}
	
	public boolean isFileType(){
		return isUiType(UITypes.KEY_FILE);
	}
	
	public boolean isTextAreaType(){
		return this.isUiType(UITypes.KEY_LONG_TEXT);
	}
	
	public boolean isRichTextType(){
		return this.isUiType(UITypes.KEY_RICH_TEXT);
	}
	
	public boolean isEmailType(){
//		return this.commentsInfo.containsKey("email类型");
		return this.isUiType(UITypes.KEY_EMAIL);
	}
	
	public boolean isUrlType(){
//		return this.commentsInfo.containsKey("url类型");
		return this.isUiType(UITypes.KEY_URL);
	}
	
	public boolean isAssociationType(){
//		return this.commentsInfo.containsKey("关联类型");
		return this.isUiType(UITypes.KEY_ASSOCIATION);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.javaName = StringUtils.toPropertyName(name);
	}

	public String getCapitalizeName() {
		return StringUtils.capitalize(getJavaName());
	}
	
	public String getComment() {
		return comment;
	}
	
	public List<String> getComments() {
		return comments;
	}
	
	public Map<String, String> getCommentsInfo() {
		return commentsInfo;
	}

	public String getIndexComment(int index) {
		List<String> comments = getComments();
		if(comments.size()<index){
			return comments.get(index);
		}else{
			return "";
		}
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	/*public boolean isReferencedKey() {
		return referencedKey;
	}

	public void setReferencedKey(boolean referencedKey) {
		this.referencedKey = referencedKey;
	}*/
	
	public boolean isDateType(){
		return this.mapping.isDateType();
	}

	public TableMeta getTable() {
		return table;
	}

	public void setTable(TableMeta table) {
		this.table = table;
	}

	public int getSqlType() {
		return mapping.getSqlType();
	}

	public String getJavaName() {
		return javaName;
	}

	public Class<?> getJavaType() {
		return this.mapping.getJavaType(); 
	}

	public void setJavaName(String javaName) {
		this.javaName = javaName;
	}
	
	public String getPropertyName(){
		return getJavaName();
	}
	
	public String getCapitalizePropertyName(){
		return StringUtils.capitalize(getPropertyName());
	}

	public String getReadMethodName() {
		return getReadMethodName(true);
	}

	public String getReadMethodName(boolean convertBooleanMethod) {
		String prefix = "get";
		String name = this.getJavaName();
		if(getJavaType()==Boolean.class && convertBooleanMethod){
			prefix = "is";
			if(name.startsWith(prefix))
				name = name.substring(prefix.length());
		}
		name = name.substring(0, 1).toUpperCase()+this.getJavaName().substring(1);
		return prefix+name;
	}

	public String getWriteMethodName() {
		String prefix = "set";
		String name = this.getJavaName().substring(0, 1).toUpperCase()+this.getJavaName().substring(1);
		return prefix+name;
	}

	@Override
	public String toString() {
		return "ColumnMeta [name=" + name + ", comment=" + comment + "]";
	}

	public ColumnMapping getMapping() {
		return mapping;
	}

	public void setMapping(ColumnMapping mapping) {
		this.mapping = mapping;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}

	public static class OptionData {
		private final String label;
		private final String value;
		public OptionData(String value, String text) {
			super();
			this.label = text;
			this.value = value;
		}
		public String getValue() {
			return value;
		}
		public String getLabel() {
			return label;
		}
	}
}
