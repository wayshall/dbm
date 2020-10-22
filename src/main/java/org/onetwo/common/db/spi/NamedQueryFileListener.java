package org.onetwo.common.db.spi;

import org.onetwo.common.propconf.ResourceAdapter;

public interface NamedQueryFileListener {

//	public void afterBuild(Map<String, NamedQueryFile> namespaceInfos, ResourceAdapter<?>... sqlfileArray);
	void afterBuild(ResourceAdapter<?> file, NamedQueryFile namepsaceInfo);
	void afterReload(ResourceAdapter<?> file, NamedQueryFile namepsaceInfo);

}
