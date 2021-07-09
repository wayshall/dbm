package org.onetwo.dbm.druid;
/**
 * @author weishao zeng
 * <br/>
 */

public class LexerVar {
	
	final String name;
	final int index;
	
	public LexerVar(String name, int index) {
		super();
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}
	
}
