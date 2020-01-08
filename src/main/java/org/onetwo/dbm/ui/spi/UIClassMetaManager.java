package org.onetwo.dbm.ui.spi;

import org.onetwo.dbm.ui.meta.UIClassMeta;

/**
 * @author weishao zeng
 * <br/>
 */

public interface UIClassMetaManager {

	UIClassMeta getByTable(String tableName);
	UIClassMeta get(String uiname);
	UIClassMeta get(Class<?> uiclass);
}
