package org.onetwo.dbm.mapping;

import java.util.Map;

import org.onetwo.common.utils.JFishProperty;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.id.IdentifierGenerator;
import org.onetwo.dbm.jpa.GeneratedValueIAttrs;

import com.google.common.collect.Maps;


public class DbmMappedProperty extends AbstractMappedField {

	private GeneratedValueIAttrs generatedValueIAttrs;
	private Map<String, IdentifierGenerator<?>> idGenerators = Maps.newHashMap();
	
	public DbmMappedProperty(DbmMappedEntry entry, JFishProperty prop){
		super(entry, prop);
		this.idGenerators.putAll(entry.getIdGenerators());
//		this.buildIdMappedField(entry);
	}
	
	public void addIdGenerator(IdentifierGenerator<?> idGenerator){
		this.idGenerators.put(idGenerator.getName(), idGenerator);
	}
	
	public void setGeneratedValueIAttrs(GeneratedValueIAttrs generatedValueIAttrs) {
		this.generatedValueIAttrs = generatedValueIAttrs;
	}

	public Map<String, IdentifierGenerator<?>> getIdGenerators() {
		return idGenerators;
	}
	
	public IdentifierGenerator<?> getIdGenerator(){
		if(getGeneratedValueIAttrs()==null){
			throw new DbmException("field not supported generated value: " + getName());
		}
		IdentifierGenerator<?> idGenerator = this.idGenerators.get(generatedValueIAttrs.getGenerator());
		if(idGenerator==null){
			throw new DbmException("can not find IdGenerator for name: " + generatedValueIAttrs.getGenerator()+", entity: " + getEntry().getEntityName());
		}
		if(!idGenerator.getStrategyType().equals(getStrategyType())){
			throw new DbmException("the id generator GenerationType["+idGenerator.getStrategyType().getGenerationType()+"] "
					+ "not match config type["+getGeneratedValueIAttrs().getGenerationType()+"] of field : " + getName());
		}
		return idGenerator;
	}

	public GeneratedValueIAttrs getGeneratedValueIAttrs() {
		return generatedValueIAttrs;
	}

}
