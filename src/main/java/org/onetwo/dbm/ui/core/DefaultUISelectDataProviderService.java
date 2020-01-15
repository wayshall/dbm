package org.onetwo.dbm.ui.core;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.onetwo.common.spring.SpringUtils;
import org.onetwo.dbm.ui.exception.DbmUIException;
import org.onetwo.dbm.ui.meta.DUIEntityMeta;
import org.onetwo.dbm.ui.meta.DUIFieldMeta;
import org.onetwo.dbm.ui.meta.DUIFieldMeta.UISelectMeta;
import org.onetwo.dbm.ui.spi.DUIMetaManager;
import org.onetwo.dbm.ui.spi.DUISelectDataProviderService;
import org.onetwo.dbm.ui.vo.EnumDataVO;
import org.onetwo.dbm.ui.vo.UISelectDataRequest;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author weishao zeng
 * <br/>
 */

public class DefaultUISelectDataProviderService implements DUISelectDataProviderService {
	
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private DUIMetaManager uiclassMetaManager;
	
	public Object getDatas(UISelectDataRequest request) {
		DUIEntityMeta meta = uiclassMetaManager.get(request.getEntity());
		DUIFieldMeta uifield = meta.getField(request.getField());
		UISelectMeta uiselect = uifield.getSelect();
		if (uiselect==null) {
			throw new DbmUIException("ui select not found, entity name: " + request.getEntity() + ", field: " + request.getField());
		}
		return getDatas(uiselect, request.getQuery());
	}
	
	public Object getDatas(UISelectMeta uiselect, String query) {
		if (uiselect.useEnumData()) {
			Enum<?>[] values = (Enum<?>[]) uiselect.getDataEnumClass().getEnumConstants();
//			DataBase[] vals = DataBase.class.getEnumConstants();
			List<EnumDataVO> list = Stream.of(values).map(ev -> {
				EnumDataVO data = new EnumDataVO();
				BeanWrapper bw = SpringUtils.newBeanWrapper(ev);
				String label = (String)bw.getPropertyValue(uiselect.getLabelField());
				if (bw.isReadableProperty(uiselect.getValueField())) {
					Object value = bw.getPropertyValue(uiselect.getValueField());
					data.setValue(value);
				} else {
					data.setValue(ev.name());
				}
				data.setLabel(label);
				return data;
			}).collect(Collectors.toList());
			return list;
		} else if (uiselect.useDataProvider()) {
			Class<? extends UISelectDataProvider<?>> dataProviderClass = uiselect.getDataProvider();
			UISelectDataProvider<?> dataProvider = (UISelectDataProvider<?>)SpringUtils.getBean(applicationContext, dataProviderClass);
			return dataProvider.findDatas(query);
		} else {
			throw new DbmUIException("Neither enum nor dataProvider, field: " + uiselect.getField().getName());
		}
	}

}
