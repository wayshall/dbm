package org.onetwo.dbm.jdbc.mapper;

import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;

import lombok.Data;

/**
 * @author weishao zeng
 * <br/>
 */
@Data
public class RowResultContext {
	
	final ResultSetWrappingSqlRowSet rowSet;
	final BeanWrapper parent;
	final Integer parentHash;
	
	public RowResultContext(ResultSetWrappingSqlRowSet rowSet, BeanWrapper parent, Integer parentHash) {
		super();
		this.rowSet = rowSet;
		this.parent = parent;
		this.parentHash = parentHash;
	}

}
