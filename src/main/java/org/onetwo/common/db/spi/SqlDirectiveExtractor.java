package org.onetwo.common.db.spi;

public interface SqlDirectiveExtractor {

	public boolean isDirective(String str);
	public String extractDirective(String str);
}
