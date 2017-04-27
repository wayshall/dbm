package org.onetwo.dbm.mapping;

import java.util.Collections;
import java.util.List;

import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;

import org.onetwo.common.utils.JFishProperty;
import org.onetwo.dbm.event.IdGenerator;
import org.onetwo.dbm.jpa.GeneratedValueIAttrs;


public class DbmMappedProperty extends AbstractMappedField {

	private DbmEnumType enumType;
	private GeneratedValueIAttrs generatedValueIAttrs;
	private List<IdGenerator> idGenerators = Collections.emptyList();
	
	public DbmMappedProperty(DbmMappedEntry entry, JFishProperty prop){
		super(entry, prop);
		if(prop.hasAnnotation(Enumerated.class)){
			Enumerated enumerated = prop.getAnnotation(Enumerated.class);
			this.enumType = DbmEnumType.valueOf(enumerated.value().name());
		}
	}
	
	private void buildIdMappedField(DbmMappedEntry entry){
		DbmMappedField mfield = this;
		GeneratedValue g = mfield.getPropertyInfo().getAnnotation(GeneratedValue.class);
		if(g==null){
			return ;
		}
		GenerationType type = g.strategy();
		this.generatedValueIAttrs = new GeneratedValueIAttrs(type, g.generator());
		
		TableGenerator tg = mfield.getPropertyInfo().getAnnotation(TableGenerator.class);
		SequenceGenerator sg = mfield.getPropertyInfo().getAnnotation(SequenceGenerator.class);a
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
