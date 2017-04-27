package org.onetwo.dbm.event;

import java.io.Serializable;

import org.onetwo.dbm.mapping.DbmMappedEntry;

/**
 * @author wayshall
 * <br/>
 */
public interface IdGenerator {
	
	Serializable generate(DbmSessionImplementor session, DbmMappedEntry entry);

}
