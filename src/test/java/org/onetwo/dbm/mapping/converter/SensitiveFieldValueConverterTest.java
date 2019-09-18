package org.onetwo.dbm.mapping.converter;
/**
 * @author weishao zeng
 * <br/>
 */

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.onetwo.dbm.mapping.converter.SensitiveFieldValueConverter.SensitiveFieldInfo;

public class SensitiveFieldValueConverterTest {
	
	@Test
	public void test() {
		SensitiveFieldValueConverter converter = new SensitiveFieldValueConverter();
		
		SensitiveFieldInfo info = new SensitiveFieldInfo();
		info.setReplacementString("*");
		info.setLeftPlainTextSize(1);
		info.setSensitiveEndOf("@");
		
		String sensitive = "test@gmail.com";
		String unsensitive = converter.unsensitiveString(info, sensitive);
		assertThat(unsensitive).isEqualTo("t***@gmail.com");
		
		sensitive = "test123@gmail.com";
		info.setLeftPlainTextSize(2);
		unsensitive = converter.unsensitiveString(info, sensitive);
		assertThat(unsensitive).isEqualTo("te*****@gmail.com");
		

		sensitive = "13666661234";
		info.setLeftPlainTextSize(0);
		info.setRightPlainTextSize(4);
		info.setSensitiveEndOf("");
		unsensitive = converter.unsensitiveString(info, sensitive);
		assertThat(unsensitive).isEqualTo("*******1234");
		

		sensitive = "441827198802027777";
		info.setLeftPlainTextSize(4);
		info.setRightPlainTextSize(4);
		info.setSensitiveEndOf("");
		unsensitive = converter.unsensitiveString(info, sensitive);
		assertThat(unsensitive).isEqualTo("4418**********7777");

		sensitive = "441777";
		info.setLeftPlainTextSize(4);
		info.setRightPlainTextSize(4);
		info.setSensitiveEndOf("");
		unsensitive = converter.unsensitiveString(info, sensitive);
		assertThat(unsensitive).isEqualTo("441777");
	}

}

