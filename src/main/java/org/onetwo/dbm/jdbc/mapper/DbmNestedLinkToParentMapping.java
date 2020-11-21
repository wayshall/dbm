package org.onetwo.dbm.jdbc.mapper;
/**
 * @author weishao zeng
 * <br/>
 */
public interface DbmNestedLinkToParentMapping {
	
	/***
	 * 返回true，表示linkParent方法里已自行设置到父对象对应属性
	 * 否则，dbm会自动把对象添加到parent对应属性
	 * 一般返回false即可，有时实现此接口只需要在子对象里获取父对象
	 * @author weishao zeng
	 * @param parent
	 * @return
	 */
	boolean linkParent(Object parent);

}
