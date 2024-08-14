package org.onetwo.common.db.filequery;

/***
 * @deprecated 此枚举主要是用于通过param对象传入特定处理的参数，一般用于早期的IgnoreNull模板
 * 4.8版本后去掉了IgnoreNull模板，后面版本会移除这个枚举
 * @see DefaultFileQueryWrapper#processQueryKey(JNamedQueryKey, Object)
 * @author way
 *
 */
@Deprecated
public enum JNamedQueryKey {

//	ParserContext,
	ResultClass,
	ASC,
	DESC;
//	countClass;
	
	public static JNamedQueryKey ofKey(Object qkey){
		if(JNamedQueryKey.class.isInstance(qkey))
			return (JNamedQueryKey)qkey;
		String key = qkey.toString();
		if(!key.startsWith(":"))
			return null;
		String keyStr = key.substring(1);
		JNamedQueryKey queryKey = null;
		try {
			queryKey = JNamedQueryKey.valueOf(keyStr);
		} catch (Exception e) {
			System.out.println("no JNamedQueryKey: " + key);
		}
		return queryKey;
	}
}
