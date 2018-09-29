package org.onetwo.common.db.generator.utils;

import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.utils.CUtils;


import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

public class DbGeneratorUtills {
	
	/****
	 * sample:
	 * 列名称
	 * 类型：字典类型
	 * SEX_MALE：男
	 * SEX_FEMALE：女
	 * 
	 * 类型：数据库字典
	 * 字典代码：SEX
	 * 
	 * 文件类型
	 * 
	 * 关联类型
	 * idField: id
	 * textField: name
	 * 
	 * @author wayshall
	 * @param comment
	 * @return
	 */
	public static Map<String, String> parse(String comment){
		Map<String, String> commentInfo = Maps.newLinkedHashMap();
		if(StringUtils.isBlank(comment))
			return commentInfo;
		List<String> contents = CUtils.iterableToList(Splitter.on('\n').trimResults().omitEmptyStrings().split(comment));
		contents.stream().forEach(line->{
			List<String> datas = CUtils.iterableToList(Splitter.on(Pattern.compile(":|：")).trimResults().omitEmptyStrings().split(line));
			if(datas.isEmpty())
				return;
			if(datas.size()==1){
				commentInfo.put(datas.get(0), "");
			}else if(datas.size()==2){
				commentInfo.put(datas.get(0), datas.get(1));
			}else{
				commentInfo.put(datas.get(0), Joiner.on("").join(datas.subList(1, datas.size())));
			}
		});
		return commentInfo;
	}

}
