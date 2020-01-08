package org.onetwo.dbm.ui.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.utils.JFishResourcesScanner;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.onetwo.dbm.mapping.MappedEntryManager;
import org.onetwo.dbm.ui.annotation.UIClass;
import org.onetwo.dbm.ui.annotation.UIField;
import org.onetwo.dbm.ui.annotation.UISelect;
import org.onetwo.dbm.ui.exception.DbmUIException;
import org.onetwo.dbm.ui.meta.UIClassMeta;
import org.onetwo.dbm.ui.meta.UIFieldMeta;
import org.onetwo.dbm.ui.meta.UIFieldMeta.UISelectMeta;
import org.onetwo.dbm.ui.spi.UIClassMetaManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

/**
 * @author weishao zeng
 * <br/>
 */

public class DefaultUIClassMetaManager implements InitializingBean, UIClassMetaManager {
	
	@Autowired
	private DbmSessionFactory dbmSessionFactory;
	private MappedEntryManager mappedEntryManager;
	private Cache<Class<?>, UIClassMeta> entryCaches = CacheBuilder.newBuilder().build();

	private JFishResourcesScanner resourcesScanner = new JFishResourcesScanner();
	private String[] packagesToScan;
	private Map<String, String> uiclassMap = Maps.newConcurrentMap();
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(packagesToScan, "packagesToScan can not be empty");
		this.mappedEntryManager = dbmSessionFactory.getMappedEntryManager();
		
		resourcesScanner.scan((metadataReader, res, index)->{
			if( metadataReader.getAnnotationMetadata().hasAnnotation(UIClass.class.getName()) ){
				Map<String, Object> attrs = metadataReader.getAnnotationMetadata().getAnnotationAttributes(UIClass.class.getName());
				String name = (String)attrs.get("name");
				if (StringUtils.isBlank(name)) {
					name = metadataReader.getClassMetadata().getClassName();
				}
//				Class<?> cls = ReflectUtils.loadClass(metadataReader.getClassMetadata().getClassName(), false);
				if (uiclassMap.containsKey(name)) {
					throw new DbmUIException("duplicate ui name: " + name);
				}
				uiclassMap.put(name, metadataReader.getClassMetadata().getClassName());
			}
			return null;
		}, packagesToScan);
	}

	public UIClassMeta get(String uiname) {
		if (!uiclassMap.containsKey(uiname)) {
			throw new DbmUIException("ui class not found for name: " + uiname);
		}
		String className = uiclassMap.get(uiname);
		Class<?> uiclass = ReflectUtils.loadClass(className);
		return get(uiclass);
	}
	
	public UIClassMeta get(Class<?> uiclass) {
		try {
			return entryCaches.get(uiclass, () -> {
				return buildUIClassMeta(uiclass);
			});
		} catch (ExecutionException e) {
			throw new DbmUIException("get EntityUIMeta error", e);
		}
	}
	
	protected UIClassMeta buildUIClassMeta(Class<?> uiclass) {
		DbmMappedEntry entry = mappedEntryManager.getEntry(uiclass);
		if (entry==null) {
//			return null;
			throw new DbmUIException("ui class must be a dbm entity: " + uiclass);
		}
		
		UIClassMeta entityMeta = new UIClassMeta();
		entityMeta.setName(entry.getEntityName());
		
		entry.getFields().forEach(field -> {
			buildField(field).ifPresent(uifield -> {
				uifield.setClassMeta(entityMeta);
				entityMeta.addField(uifield);
			});
		});
		
		return entityMeta;
	}
	
	protected Optional<UIFieldMeta> buildField(DbmMappedField field) {
		UIField uifield = field.getPropertyInfo().getAnnotation(UIField.class);
		if (uifield==null) {
			return Optional.empty();
		}
		UIFieldMeta uifieldMeta = UIFieldMeta.builder()
										.name(field.getName())
										.label(uifield.label())
										.insertable(uifield.insertable())
										.listable(uifield.listable())
										.updatable(uifield.updatable())
										.build();

		UISelect uiselect = field.getPropertyInfo().getAnnotation(UISelect.class);
		if (uiselect!=null) {
			UISelectMeta uiselectMeta = uifieldMeta.new UISelectMeta();
			uiselectMeta.setDataEnumClass(uiselect.dataEnumClass());
			uiselectMeta.setDataProvider(uiselect.dataProvider());
			uiselectMeta.setLabelField(uiselect.labelField());
			uiselectMeta.setValueField(uiselect.valueField());
			uifieldMeta.setSelect(uiselectMeta);
		}
		
		return Optional.of(uifieldMeta);
	}

	public void setPackagesToScan(String... packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

}
