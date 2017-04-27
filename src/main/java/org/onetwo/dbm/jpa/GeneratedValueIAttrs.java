package org.onetwo.dbm.jpa;

import javax.persistence.GenerationType;

/**
 * @author wayshall
 * <br/>
 */
public class GeneratedValueIAttrs {
	
	private GenerationType generationType;
	private String generator;
	
	public GeneratedValueIAttrs(GenerationType generationType, String generator) {
		super();
		this.generationType = generationType;
		this.generator = generator;
	}
	public GenerationType getGenerationType() {
		return generationType;
	}
	public void setGenerationType(GenerationType generationType) {
		this.generationType = generationType;
	}
	public String getGenerator() {
		return generator;
	}
	public void setGenerator(String generator) {
		this.generator = generator;
	}

}
