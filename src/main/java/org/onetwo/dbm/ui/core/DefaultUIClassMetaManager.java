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
import org.onetwo.dbm.ui.annotation.DUICrudPage;
import org.onetwo.dbm.ui.annotation.DUIField;
import org.onetwo.dbm.ui.annotation.DUISelect;
import org.onetwo.dbm.ui.exception.DbmUIException;
import org.onetwo.dbm.ui.meta.DUICrudPageMeta;
import org.onetwo.dbm.ui.meta.DUIFieldMeta;
import org.onetwo.dbm.ui.meta.DUIFieldMeta.UISelectMeta;
import org.onetwo.dbm.ui.spi.DUIClassMetaManager;
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

public class DefaultUIClassMetaManager implements InitializingBean, DUIClassMetaManager {
	
	@Autowired
	private DbmSessionFactory dbmSessionFactory;
	private DatabaseMetaDialet databaseMetaDialet;
	private MappedEntryManager mappedEntryManager;
	private Cache<Class<?>, DUICrudPageMeta> entryCaches = CacheBuilder.newBuilder().build();

	private JFishResourcesScanner resourcesScanner = new JFishResourcesScanner();
	private String[] packagesToScan;
	private Map<String, String> uiclassMap = Maps.newConcurrentMap();
//	private Map<String, String> uiclassTableMap = Maps.newConcurrentMap();
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(packagesToScan, "packagesToScan can not be empty");
		this.mappedEntryManager = dbmSessionFactory.getMappedEntryManager();
		this.databaseMetaDialet = new DelegateDatabaseMetaDialet(dbmSessionFactory.getDataSource());
		
		resourcesScanner.scan((metadataReader, res, index)->{
			if( metadataReader.getAnnotationMetadata().hasAnnotation(DUICrudPage.class.getName()) ){
				Map<String, Object> uiclassAttrs = metadataReader.getAnnotationMetadata().getAnnotationAttributes(DUICrudPage.class.getName());
				String name = (String)uiclassAttrs.get("name");
				if (StringUtils.isBlank(name)) {
					name = metadataReader.getClassMetadata().getClassName();
				}
//				Class<?> cls = ReflectUtils.loadClass(metadataReader.getClassMetadata().getClassName(), false);
				if (uiclassMap.containsKey(name)) {
					throw new DbmUIException("duplicate ui name: " + name);
				}
				uiclassMap.put(name, metadataReader.getClassMetadata().getClassName());
				

				/*Map<String, Object> tableAttrs = metadataReader.getAnnotationMetadata().getAnnotationAttributes(Table.class.getName());
				String tableName = (String)tableAttrs.get("name");
				if (StringUtils.isNotBlank(tableName)) {
					uiclassTableMap.put(tableName.toLowerCase(), metadataReader.getClassMetadata().getClassName());
				}*/
			}
			return null;
		}, packagesToScan);
	}
	

	/*public UIClassMeta getByTable(String tableName) {
		tableName = tableName.toLowerCase();
		if (!uiclassTableMap.containsKey(tableName)) {
			throw new DbmUIException("ui class not found for table: " + tableName);
		}
		String className = uiclassTableMap.get(tableName);
		Class<?> uiclass = ReflectUtils.loadClass(className);
		return get(uiclass);
	}*/

	public DUICrudPageMeta get(String uiname) {
		if (!uiclassMap.containsKey(uiname)) {
			throw new DbmUIException("ui class not found for name: " + uiname);
		}
		String className = uiclassMap.get(uiname);
		Class<?> uiclass = ReflectUtils.loadClass(className);
		return get(uiclass);
	}
	
	public DUICrudPageMeta get(Class<?> uiclass) {
		try {
			return entryCaches.get(uiclass, () -> {
				return buildUIClassMeta(uiclass);
			});
		} catch (ExecutionException e) {
			throw new DbmUIException("get EntityUIMeta error", e);
		}
	}
	
	protected DUICrudPageMeta buildUIClassMeta(Class<?> uiclass) {
		DUICrudPage uiclassAnno = uiclass.getAnnotation(DUICrudPage.class);
		
		DbmMappedEntry entry = mappedEntryManager.getEntry(uiclassAnno.entityClass());
		if (entry==null) {
//			return null;
			throw new DbmUIException("ui class must be a dbm entity: " + uiclass);
		}
		
		String entityName = uiclassAnno.name();
		if (StringUtils.isBlank(entityName)) {
			entityName = entry.getEntityName();
		}
		
		TableMeta table = databaseMetaDialet.getTableMeta(entry.getTableInfo().getName());
		DUICrudPageMeta entityMeta = new DUICrudPageMeta();
		entityMeta.setLabel(uiclassAnno.label());
		entityMeta.setName(entityName);
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
	
	protected Optional<DUIFieldMeta> buildField(DbmMappedField field) {
		DUIField uifield = field.getPropertyInfo().getAnnotation(DUIField.class);
		if (uifield==null) {
			return Optional.empty();
		}
		DUIFieldMeta uifieldMeta = DUIFieldMeta.builder()
										.name(field.getName())
										.label(uifield.label())
										.insertable(uifield.insertable())
										.listable(uifield.listable())
										.updatable(uifield.updatable())
										.dbmField(field)
										.order(uifield.order())
										.build();

		DUISelect uiselect = field.getPropertyInfo().getAnnotation(DUISelect.class);
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
