package org.onetwo.dbm.id;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
abstract public class AbstractIdentifierGenerator implements IdentifierGenerator<Long>{
	
	final private String name;
	
	/*private Long currentId;
	private Long maxId;*/
	private Queue<Long> idQueue;
	
	public AbstractIdentifierGenerator(String name) {
		this.name = name;
		this.idQueue = new LinkedList<Long>();
	}

	abstract protected int getAllocationSize();
	
	@Override
	public synchronized Long generate(DbmSessionImplementor session) {
		Long currentId = idQueue.poll();
		if(currentId==null){
			List<Long> ids = batchGenerate(session, getAllocationSize());
			idQueue.addAll(ids);
			currentId = idQueue.poll();
		}
		return currentId;
	}
	/*public synchronized Long generate2(DbmSessionImplementor session) {
		Long id = currentId;
		if(id==null || id>maxId){
			Pair<Long, Long> seqs = batchGenerate(session, getAllocationSize());
			currentId = seqs.getFirst();
			maxId = seqs.getSecond();
			id = currentId;
		}else{
			id = currentId++;
		}
		return id;
	}*/

	public String getName() {
		return name;
	}
	
}
