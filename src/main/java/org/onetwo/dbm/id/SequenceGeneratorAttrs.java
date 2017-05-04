package org.onetwo.dbm.id;
/**
 * @author wayshall
 * <br/>
 */
public class SequenceGeneratorAttrs {
	
	final private String name;
	final private int allocationSize;
	
	final private String sequenceName;
	final private int initialValue;
	
	public SequenceGeneratorAttrs(String name, String sequenceName,
			int initialValue, int allocationSize) {
		super();
		this.name = name;
		this.sequenceName = sequenceName;
		this.initialValue = initialValue;
		this.allocationSize = allocationSize;
	}

	public String getName() {
		return name;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public int getInitialValue() {
		return initialValue;
	}

	public int getAllocationSize() {
		return allocationSize;
	}
}
