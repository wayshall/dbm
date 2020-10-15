package org.onetwo.common.db.spi;

import java.util.Collection;
import java.util.Map;

import org.onetwo.common.propconf.ResourceAdapter;

public interface NamedQueryFile {
	public String getKey();
	public String getNamespace();
	public Collection<FileBaseNamedQueryInfo> getNamedProperties();
	public FileBaseNamedQueryInfo getNamedProperty(String name);
	public void addAll(Map<String, FileBaseNamedQueryInfo> namedInfos, boolean throwIfExist);
	public void put(String name, FileBaseNamedQueryInfo info, boolean throwIfExist);
	boolean isGlobal();
	
	public ResourceAdapter<?> getSource();
}
