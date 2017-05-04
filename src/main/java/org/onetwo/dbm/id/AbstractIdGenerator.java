package org.onetwo.dbm.id;

import java.io.Serializable;

import org.onetwo.common.propconf.JFishProperties;
import org.onetwo.common.utils.StringUtils;

/**
 * @author wayshall
 * <br/>
 */
abstract public class AbstractIdGenerator<T extends Serializable> implements IdGenerator<T>{
	static final public String ATTR_NAME = "name";
	
	final private JFishProperties attrs;
	final private String name;
	
	public AbstractIdGenerator(JFishProperties attrs, String name) {
		super();
		this.attrs = attrs;
		if(StringUtils.isNotBlank(name)){
			this.name = name;
		}else{
			this.name = attrs.getAndThrowIfEmpty(ATTR_NAME);
		}
	}

	public JFishProperties getAttrs() {
		return attrs;
	}

	public String getName() {
		return name;
	}
	
}
