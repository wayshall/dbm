package org.onetwo.dbm.ui.spi;

import org.onetwo.dbm.ui.meta.DUICrudPageMeta;

/**
 * @author weishao zeng
 * <br/>
 */

public interface DUIClassMetaManager {

//	UIClassMeta getByTable(String tableName);
	DUICrudPageMeta get(String pageName);
	DUICrudPageMeta get(Class<?> pageClass);
}
