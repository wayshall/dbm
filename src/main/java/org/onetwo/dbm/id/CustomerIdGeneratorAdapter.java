package org.onetwo.dbm.id;

import java.io.Serializable;
import java.util.List;

import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
public class CustomerIdGeneratorAdapter<T extends Serializable> implements IdentifierGenerator<T>{

	final private String name;
	final private CustomIdGenerator<T> customIdGenerator;
	
	public CustomerIdGeneratorAdapter(String name, CustomIdGenerator<T> customIdGenerator) {
		super();
		this.name = name;
		this.customIdGenerator = customIdGenerator;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public T generate(DbmSessionImplementor session) {
		return customIdGenerator.generate(session);
	}

	@Override
	public List<T> batchGenerate(DbmSessionImplementor session, int batchSize) {
		throw new UnsupportedOperationException("batch generate");
	}

}
