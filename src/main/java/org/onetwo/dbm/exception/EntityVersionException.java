package org.onetwo.dbm.exception;



@SuppressWarnings("serial")
public class EntityVersionException extends DbmException{

	final private Object id;
	final private Object entityVersion;
	final private Object lastVersion;
	
	public EntityVersionException(Class<?> entityClass, Object id, Object entityVersion) {
		super("entity["+entityClass+"] version has changed, id: " + id + ", entity version: " + entityVersion);
		this.id = id;
		this.entityVersion = entityVersion;
		this.lastVersion = null;
	}
	public EntityVersionException(Class<?> entityClass, Object id, Object entityVersion, Object lastVersion) {
		super("entity["+entityClass+"] version has changed, id: " + id + ", entity version: " + entityVersion + ", lasted version: " + lastVersion);
		this.id = id;
		this.entityVersion = entityVersion;
		this.lastVersion = lastVersion;
	}

	public Object getId() {
		return id;
	}

	public Object getEntityVersion() {
		return entityVersion;
	}

	public Object getLastVersion() {
		return lastVersion;
	}

	@Override
	public String toString() {
		return "EntityVersionException [id=" + id + ", entityVersion=" + entityVersion + ", lastVersion=" + lastVersion + "]";
	}
	
}
