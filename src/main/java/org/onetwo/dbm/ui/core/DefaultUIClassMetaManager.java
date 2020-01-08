package org.onetwo.dbm.ui.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.db.generator.dialet.DatabaseMetaDialet;
import org.onetwo.common.db.generator.dialet.DelegateDatabaseMetaDialet;
import org.onetwo.common.db.generator.meta.TableMeta;
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
	private DatabaseMetaDialet databaseMetaDialet;
	private MappedEntryManager mappedEntryManager;
	private Cache<Class<?>, UIClassMeta> entryCaches = CacheBuilder.newBuilder().build();

	private JFishResourcesScanner resourcesScanner = new JFishResourcesScanner();
	private String[] packagesToScan;
	private Map<String, String> uiclassMap = Maps.newConcurrentMap();
	private Map<String, String> uiclassTableMap = Maps.newConcurrentMap();
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(packagesToScan, "packagesToScan can not be empty");
		this.mappedEntryManager = dbmSessionFactory.getMappedEntryManager();
		this.databaseMetaDialet = new DelegateDatabaseMetaDialet(dbmSessionFactory.getDataSource());
		
		resourcesScanner.scan((metadataReader, res, index)->{
			if( metadataReader.getAnnotationMetadata().hasAnnotation(UIClass.class.getName()) ){
				Map<String, Object> uiclassAttrs = metadataReader.getAnnotationMetadata().getAnnotationAttributes(UIClass.class.getName());
				String name = (String)uiclassAttrs.get("name");
				if (StringUtils.isBlank(name)) {
					name = metadataReader.getClassMetadata().getClassName();
				}
//				Class<?> cls = ReflectUtils.loadClass(metadataReader.getClassMetadata().getClassName(), false);
				if (uiclassMap.containsKey(name)) {
					throw new DbmUIException("duplicate ui name: " + name);
				}
				uiclassMap.put(name, metadataReader.getClassMetadata().getClassName());
				

				Map<String, Object> tableAttrs = metadataReader.getAnnotationMetadata().getAnnotationAttributes(Table.class.getName());
				String tableName = (String)tableAttrs.get("name");
				if (StringUtils.isNotBlank(tableName)) {
					uiclassTableMap.put(tableName.toLowerCase(), metadataReader.getClassMetadata().getClassName());
				}
			}
			return null;
		}, packagesToScan);
	}
	

	public UIClassMeta getByTable(String tableName) {
		tableName = tableName.toLowerCase();
		if (!uiclassTableMap.containsKey(tableName)) {
			throw new DbmUIException("ui class not found for table: " + tableName);
		}
		String className = uiclassTableMap.get(tableName);
		Class<?> uiclass = ReflectUtils.loadClass(className);
		return get(uiclass);
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
		
		UIClass uiclassAnno = entry.getAnnotationInfo().getAnnotation(UIClass.class);
		
		TableMeta table = databaseMetaDialet.getTableMeta(entry.getTableInfo().getName());
		UIClassMeta entityMeta = new UIClassMeta();
		entityMeta.setLabel(uiclassAnno.label());
		entityMeta.setName(entry.getEntityName());
		entityMeta.setMappedEntry(entry);
		entityMeta.setTable(table);
		
		entry.getFields().forEach(field -> {
			buildField(field).ifPresent(uifield -> {
				uifield.setClassMeta(entityMeta);
				uifield.setColumn(table.getColumn(field.getColumn().getName()));
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
										.dbmField(field)
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
