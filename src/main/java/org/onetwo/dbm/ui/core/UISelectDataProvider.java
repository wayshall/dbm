package org.onetwo.dbm.ui.core;
/**
 * @author weishao zeng
 * <br/>
 */

public interface UISelectDataProvider<T> {
	
	T findDatas(String query);

}
