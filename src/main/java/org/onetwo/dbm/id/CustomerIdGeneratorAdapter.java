package org.onetwo.dbm.id;

import java.io.Serializable;
import java.util.List;

import org.onetwo.common.convert.Types;
import org.onetwo.dbm.core.spi.DbmSessionImplementor;

/**
 * @author wayshall
 * <br/>
 */
public class CustomerIdGeneratorAdapter<T extends Serializable> implements IdentifierGenerator<Serializable>{

	final private String name;
	final private CustomIdGenerator<T> customIdGenerator;
	final private Class<?> valueType;
	
	public CustomerIdGeneratorAdapter(String name, CustomIdGenerator<T> customIdGenerator, Class<?> valueType) {
		super();
		this.name = name;
		this.customIdGenerator = customIdGenerator;
		this.valueType = valueType;
	}

	@Override
	public StrategyType getStrategyType() {
		return StrategyType.DBM;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Serializable generate(DbmSessionImplementor session) {
		T value = customIdGenerator.generate(session);
		if (valueType==Object.class) {
			return value;
		}
		return (Serializable)Types.convertValue(value, valueType);
	}

	@Override
	public List<Serializable> batchGenerate(DbmSessionImplementor session, int batchSize) {
		throw new UnsupportedOperationException("batch generate");
	}

}
