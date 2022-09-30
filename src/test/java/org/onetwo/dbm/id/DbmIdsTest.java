package org.onetwo.dbm.id;
/**
 * @author weishao zeng
 * <br/>
 */

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DbmIdsTest {
	
	@Test
	public void testCreateIdGenerator() {
		SnowflakeIdGenerator sig1 = DbmIds.createIdGenerator(1, 2);
		SnowflakeIdGenerator sig2 = DbmIds.createIdGenerator(1, 2);
		SnowflakeIdGenerator sig3 = DbmIds.createIdGenerator(1, 3);
		assertTrue(sig1==sig2);
		assertTrue(sig1!=sig3);
		assertTrue(sig2!=sig3);
	}

}
