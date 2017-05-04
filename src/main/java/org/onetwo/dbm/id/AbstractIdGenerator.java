package org.onetwo.dbm.id;

import org.onetwo.dbm.core.spi.DbmSessionImplementor;
import org.springframework.data.util.Pair;

/**
 * @author wayshall
 * <br/>
 */
abstract public class AbstractIdGenerator implements IdGenerator<Long>{
	
	final private String name;
	
	private Long currentId;
	private Long maxId;
	
	public AbstractIdGenerator(String name) {
		this.name = name;
	}

	abstract protected int getAllocationSize();
	
	@Override
	public synchronized Long generate(DbmSessionImplementor session) {
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
	}

	public String getName() {
		return name;
	}
	
}
