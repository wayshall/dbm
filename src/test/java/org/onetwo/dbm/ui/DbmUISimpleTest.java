package org.onetwo.dbm.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserStatus;
import org.onetwo.dbm.ui.entity.UserUIEntity;
import org.onetwo.dbm.ui.meta.UIClassMeta;
import org.onetwo.dbm.ui.spi.UIClassMetaManager;
import org.onetwo.dbm.ui.spi.UISelectDataProviderService;
import org.onetwo.dbm.ui.vo.EnumDataVO;
import org.onetwo.dbm.ui.vo.UISelectDataRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author weishao zeng
 * <br/>
 */

public class DbmUISimpleTest extends DbmBaseTest {
	
	@Autowired
	private UIClassMetaManager uiClassMetaManager;
	@Autowired
	private UISelectDataProviderService selectDataProviderService;
	
	@Test
	public void testUIClass() {
		UIClassMeta classMeta = uiClassMetaManager.get(UserUIEntity.class);
		assertThat(classMeta).isNotNull();
		assertThat(classMeta.getFieldMap().size()).isEqualTo(3);
	}

	@Test
	public void testUIClassWithTable() {
		UIClassMeta classMeta = uiClassMetaManager.getByTable("TEST_USER");
		assertThat(classMeta).isNotNull();
		assertThat(classMeta.getFieldMap().size()).isEqualTo(3);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSelectData() {
		UISelectDataRequest request = UISelectDataRequest.builder()
													.uiname("TestUser")
													.field("status")
													.build();
		List<EnumDataVO> enumDatas = (List<EnumDataVO>)selectDataProviderService.getDatas(request);
		assertThat(enumDatas.size()).isEqualTo(3);
		Optional<EnumDataVO> normal = enumDatas.stream().filter(d -> {
			return d.getValue().equals(UserStatus.NORMAL.getValue());
		}).findAny();
		assertThat(normal).isPresent();
	}
	

}
