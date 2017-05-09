package org.onetwo.common.db.spi;

import java.util.Map;

import org.onetwo.common.propconf.ResourceAdapter;

public interface NamedQueryFileListener {

	public void afterBuild(Map<String, NamedQueryFile> namespaceInfos, ResourceAdapter<?>... sqlfileArray);
	void afterReload(ResourceAdapter<?> file, NamedQueryFile namepsaceInfo);

}
