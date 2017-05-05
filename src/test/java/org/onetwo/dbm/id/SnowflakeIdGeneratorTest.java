package org.onetwo.dbm.id;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * @author wayshall
 * <br/>
 */
public class SnowflakeIdGeneratorTest {
	
	@Test
	public void testId(){
		SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1);
		int count = 1000000;
		Set<Long> idSet = new HashSet<Long>(count);
		for (int i = 0; i < count; i++) {
			long id = idGenerator.nextId();
			if(idSet.contains(id)){
				throw new RuntimeException("same id:"+id);
			}
			idSet.add(id);
		}
	}

}
