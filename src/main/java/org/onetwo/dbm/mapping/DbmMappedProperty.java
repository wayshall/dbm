package org.onetwo.dbm.mapping;

import java.util.Map;

import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;

import org.onetwo.common.utils.JFishProperty;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.id.IdentifierGenerator;
import org.onetwo.dbm.jpa.GeneratedValueIAttrs;

import com.google.common.collect.Maps;


public class DbmMappedProperty extends AbstractMappedField {

	private DbmEnumType enumType;
	private GeneratedValueIAttrs generatedValueIAttrs;
	private Map<String, IdentifierGenerator<?>> idGenerators = Maps.newHashMap();
	
	public DbmMappedProperty(DbmMappedEntry entry, JFishProperty prop){
		super(entry, prop);
		if(prop.hasAnnotation(Enumerated.class)){
			Enumerated enumerated = prop.getAnnotation(Enumerated.class);
			this.enumType = DbmEnumType.valueOf(enumerated.value().name());
		}
		this.idGenerators.putAll(entry.getIdGenerators());
//		this.buildIdMappedField(entry);
	}
	
	/*private void buildIdMappedField(DbmMappedEntry entry){
		DbmMappedField mfield = this;
		GeneratedValue g = mfield.getPropertyInfo().getAnnotation(GeneratedValue.class);
		if(g==null){
			return ;
		}
		GenerationType type = g.strategy();
		this.generatedValueIAttrs = new GeneratedValueIAttrs(type, g.generator());
		
		SequenceGenerator sg = mfield.getPropertyInfo().getAnnotation(SequenceGenerator.class);
		if(sg!=null){
			IdGenerator<Long> idGenerator = IdGeneratorFactory.create(sg);
			this.idGenerators.put(idGenerator.getName(), idGenerator);
		}
		TableGenerator tg = mfield.getPropertyInfo().getAnnotation(TableGenerator.class);
		if(sg!=null){
			IdGenerator<Long> idGenerator = IdGeneratorFactory.create(tg);
			this.idGenerators.put(idGenerator.getName(), idGenerator);
		}
	}*/
	
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
		IdentifierGenerator<?> idGenerator = this.idGenerators.get(generatedValueIAttrs.getGenerator());
		if(idGenerator==null){
			throw new DbmException("can not find IdGenerator for name: " + generatedValueIAttrs.getGenerator());
		}
		return idGenerator;
	}

	public GeneratedValueIAttrs getGeneratedValueIAttrs() {
		return generatedValueIAttrs;
	}

	@Override
	public boolean isEnumerated() {
		return enumType!=null;
	}

	public DbmEnumType getEnumType() {
		return enumType;
	}
	
}
