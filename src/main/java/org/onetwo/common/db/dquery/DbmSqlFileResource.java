package org.onetwo.common.db.dquery;

import java.io.File;
import java.util.List;

import org.onetwo.common.propconf.ResourceAdapter;

/**
 * @author wayshall
 * <br/>
 */
public class DbmSqlFileResource<T> implements ResourceAdapter<T> {
	
	final private ResourceAdapter<T> source;
	final private Class<T> mappedInterface;


	public DbmSqlFileResource(ResourceAdapter<T> source,
			Class<T> mappedInterface) {
		super();
		this.source = source;
		this.mappedInterface = mappedInterface;
	}

	public Class<T> getMappedInterface() {
		return mappedInterface;
	}

	public String getName() {
		return source.getName();
	}

	public T getResource() {
		return source.getResource();
	}

	public File getFile() {
		return source.getFile();
	}

	public boolean isSupportedToFile() {
		return source.isSupportedToFile();
	}

	public List<String> readAsList() {
		return source.readAsList();
	}

	public String getPostfix() {
		return source.getPostfix();
	}
	
	

}
