package org.onetwo.dbm.event.internal;

import org.onetwo.dbm.mapping.DbmMappedField;

public interface MappedFieldProcessor<T extends DbmMappedField> {
	void execute(T field);
}
