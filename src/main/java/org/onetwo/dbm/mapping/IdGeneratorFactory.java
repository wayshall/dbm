package org.onetwo.dbm.mapping;

import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;

import org.onetwo.common.propconf.JFishProperties;
import org.onetwo.dbm.id.AbstractIdGenerator;
import org.onetwo.dbm.id.IdGenerator;
import org.onetwo.dbm.id.SequenceGeneratorAttrs;
import org.onetwo.dbm.id.SequenceIdGenerator;
import org.onetwo.dbm.id.TableGeneratorAttrs;
import org.onetwo.dbm.id.TableIdGenerator;
import org.springframework.util.Assert;

/**
 * @author wayshall
 * <br/>
 */
public class IdGeneratorFactory {
	
	public static IdGenerator<Long> create(SequenceGenerator sg){
		Assert.notNull(sg);
		SequenceGeneratorAttrs sgAttrs = new SequenceGeneratorAttrs(sg.name(), sg.sequenceName(), sg.initialValue(), sg.allocationSize());
		SequenceIdGenerator generator = new SequenceIdGenerator(sgAttrs);
		return generator;
	}
	
	public static IdGenerator<Long> create(TableGenerator tg){
		Assert.notNull(tg);
		TableGeneratorAttrs tgAttrs = new TableGeneratorAttrs(tg.name(), 
																tg.allocationSize(), 
																tg.table(), 
																tg.pkColumnName(), 
																tg.valueColumnName(), 
																tg.pkColumnValue());
		TableIdGenerator generator = new TableIdGenerator(tgAttrs);
		return generator;
	}

}
