package org.onetwo.dbm.id;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.Test;
import org.onetwo.common.concurrent.ConcurrentRunnable;

/**
 * @author wayshall
 * <br/>
 */
public class SnowflakeIdGeneratorTest {
	
	@Test
	public void testId(){
		SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1);
		int count = 100;
		Set<Long> idSet = new ConcurrentSkipListSet<>();
		for (int i = 0; i < count; i++) {
			long id = idGenerator.nextId();
			if(idSet.contains(id)){
				throw new RuntimeException("same id:"+id);
			}
			idSet.add(id);
			System.out.println("id length:"+String.valueOf(id).length()+", id:"+id+", sid:"+Long.toString(id, 36));
		}
	}
	
	@Test
	public void testConcurrentGenerateIds(){
//		SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1/32, 1%32); //不会抛错
		int count = 10000;
		Set<Long> idSet = new ConcurrentSkipListSet<>();
		
		ConcurrentRunnable cr = ConcurrentRunnable.create(10, () -> {
			SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1/32, 1%32);//会抛错
			for (int i = 0; i < count; i++) {
				long id = idGenerator.nextId();
				if(idSet.contains(id)){
					throw new RuntimeException("same id:"+id);
				}
				idSet.add(id);
				System.out.println("id length:"+String.valueOf(id).length()+", id:"+id+", sid:"+Long.toString(id, 36));
			}
		});
		
		cr.start();
		cr.await();
	}

}
