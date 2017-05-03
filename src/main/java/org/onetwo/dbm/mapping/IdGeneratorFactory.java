package org.onetwo.dbm.mapping;

import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;

import org.onetwo.dbm.id.IdGenerator;
import org.onetwo.dbm.id.SequenceGeneratorAttrs;
import org.onetwo.dbm.id.SequenceIdGenerator;
import org.springframework.util.Assert;

/**
 * @author wayshall
 * <br/>
 */
@SuppressWarnings("rawtypes")
public class IdGeneratorFactory {
	
	public static IdGenerator<Long> create(SequenceGenerator sg){
		Assert.notNull(sg);
		SequenceGeneratorAttrs sgAttrs = new SequenceGeneratorAttrs(sg.name(), sg.sequenceName(), sg.initialValue(), sg.allocationSize());
		SequenceIdGenerator generator = new SequenceIdGenerator(sgAttrs);
		return generator;
	}
	
	public static IdGenerator<Long> create(TableGenerator tg){
		Assert.notNull(tg);
		SequenceGeneratorAttrs sgAttrs = new SequenceGeneratorAttrs(sg.name(), sg.sequenceName(), sg.initialValue(), sg.allocationSize());
		SequenceIdGenerator generator = new SequenceIdGenerator(sgAttrs);
		return generator;
	}

}
