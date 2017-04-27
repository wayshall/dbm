package org.onetwo.dbm.event;

import org.onetwo.dbm.mapping.DbmMappedField;

public class DbmEntityFieldListenerAdapter implements DbmEntityFieldListener {

	@Override
	public Object beforeFieldInsert(DbmMappedField field, Object value) {
		return value;
	}

	@Override
	public Object beforeFieldUpdate(DbmMappedField field, Object value) {
		return value;
	}

}
