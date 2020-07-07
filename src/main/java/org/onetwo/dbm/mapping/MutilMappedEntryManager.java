package org.onetwo.dbm.mapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.onetwo.common.annotation.AnnotationInfo;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.utils.ResourcesScanner;
import org.onetwo.common.spring.utils.ScanResourcesCallback;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.common.utils.list.JFishList;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.exception.NoMappedEntryException;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

public class MutilMappedEntryManager implements MappedEntryBuilder, MappedEntryManager {
	
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());

//	private Map<String, JFishMappedEntry> entryCache = new ConcurrentHashMap<String, JFishMappedEntry>();
	private List<MappedEntryBuilder> mappedEntryBuilders;
//	private MappedEntryListenerManager mappedEntryListenerManager;

	private ResourcesScanner scanner = ResourcesScanner.CLASS_CANNER;
//	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private Map<String, DbmMappedEntry> nameEntryMapping = Maps.newConcurrentMap();
	private Cache<String, DbmMappedEntry> entryCaches = CacheBuilder.newBuilder().build();
	private Cache<String, DbmMappedEntry> readOnlyEntryCaches = CacheBuilder.newBuilder().build();
//	final private SimpleInnserServiceRegistry serviceRegistry;
//	private DBDialect dialet;
	private MappedEntryManagerListener mappedEntryManagerListener;
	private DbmInnerServiceRegistry serviceRegistry;


	public MutilMappedEntryManager(DbmInnerServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	
	public void setMappedEntryManagerListener(MappedEntryManagerListener mappedEntryManagerListener) {
		this.mappedEntryManagerListener = mappedEntryManagerListener;
	}


	/***
	 * first
	 */
	@Override
	public void initialize() {
	}


	/***
	 * second
	 */
	@Override
	public void scanPackages(String... packagesToScan) {
		CompletableFuture.runAsync(() -> {
			scanPackages0(packagesToScan);
		});
	}
	
	private void scanPackages0(String... packagesToScan) {
		Assert.notEmpty(mappedEntryBuilders, "no mapped entry builders ...");
		
		StringBuilder log = new StringBuilder();
		if (!LangUtils.isEmpty(packagesToScan)) {
			log.append("scan model package: ").append(Arrays.asList(packagesToScan)).append("\n");
			
			Collection<ScanedClassContext> entryClassNameList = scanner.scan(new ScanResourcesCallback<ScanedClassContext>() {

				@Override
				public ScanedClassContext doWithCandidate(MetadataReader metadataReader, Resource resource, int count) {
					if(!isSupported(metadataReader))
						return null;
					return new ScanedClassContext(metadataReader);
				}

			}, packagesToScan);
			

			if(mappedEntryManagerListener!=null){
				mappedEntryManagerListener.beforeBuild(this, entryClassNameList);
			}
			
			JFishList<DbmMappedEntry> entryList = JFishList.create();
			int count = 0;
			for(ScanedClassContext ctx : entryClassNameList){
				String clsName = ctx.getClassName();
				Class<?> clazz = ReflectUtils.loadClass(clsName);
				
				DbmMappedEntry entry = buildMappedEntry(clazz);
//				if(entry==null)
//					throw new DbmException("can not build the entity : " + clazz);
//				buildEntry(entry);
				log.append("build entity entry[").append(count++).append("]: ").append(entry.getEntityName()).append("\n");
				entryList.add(entry);
				
				String key = getCacheKey(entry.getEntityClass());
				if(StringUtils.isNotBlank(key))
					putInCache(key, entry);
				
			}

			//锁定
			for(DbmMappedEntry entry : entryList){
				entry.freezing();
			}
			
			if(logger.isInfoEnabled()){
				logger.info(log.toString());
			}
		}

	}
	
	/***
	 * 检查缓存
	 * @author weishao zeng
	 * @param entry
	 */
	private void buildEntry(DbmMappedEntry entry){
		buildEntry(entry, true);
	}
	
	private void buildEntry(DbmMappedEntry entry, boolean putToNameCache){
		if (putToNameCache && nameEntryMapping.containsKey(entry.getEntityName())) {
			throw new DbmException("duplicate entity name: " + entry.getEntityName())
						.put("exist entity", nameEntryMapping.get(entry.getEntityName()).getEntityClass())
						.put("conflicted entity", entry.getEntityClass());
		}
		try {
			entry.buildEntry();
		} catch (Exception e) {
			throw new DbmException("build entry["+entry.getEntityName()+"] error: "+e.getMessage(), e);
		}
		if (putToNameCache) {
			nameEntryMapping.put(entry.getEntityName(), entry);
		}
	}

	private void putInCache(String key, DbmMappedEntry entry) {
		this.entryCaches.put(key, entry);
	}

	@Override
	public boolean isSupported(Object entity) {
		for (MappedEntryBuilder em : mappedEntryBuilders) {
			if (em.isSupported(entity))
				return true;
		}
		return false;
	}

	@Override
	public DbmMappedEntry findEntry(Object object) {
		try {
			return getEntry(object);
		} catch (NoMappedEntryException e) {
			//ignore
		} catch (DbmException e) {
			throw e;
		} catch (Exception e) {
			throw new DbmException("find entry error: " + e.getMessage(), e);
		}
		return null;
	}

	@Override
	public DbmMappedEntry getEntry(Object objects) {
		Assert.notNull(objects, "the object arg can not be null!");
		DbmMappedEntry entry = null;
		
		Object object = LangUtils.getFirst(objects);
		if(object==null)
			throw new NoMappedEntryException("object can not null or emtpty, objects:"+objects);
		
		if(String.class.isInstance(object) && object.toString().indexOf('.')!=-1){
			object = ReflectUtils.loadClass(object.toString());
		}
		
		String key = getCacheKey(object);
		
		if (StringUtils.isBlank(key)) {
			//for map
			entry = buildMappedEntry(object);
//			mappedEntryListenerManager.fireAfterBuildEvents(entry);
//			buildEntry(entry);
//			mappedEntryListenerManager.fireAfterBuildEvents(entry);
			entry.freezing();
			return entry;
		}
		
		try {
			final Object entityObject = object;
			entry = entryCaches.get(key, ()->{
				DbmMappedEntry value = buildMappedEntry(entityObject);

//				if (value == null)
//					throw new NoMappedEntryException("can find build entry for this object, may be no mapping : " + entityObject.getClass());

//				buildEntry(value);
				value.freezing();
				return value;
			});
		} catch (ExecutionException e) {
			throw new DbmException("create entry error for entity: " + object, e);
		}
		return entry;
		
	}

	@Override
	public DbmMappedEntry buildMappedEntry(Object object) {
		DbmMappedEntry entry = null;
		for (MappedEntryBuilder em : mappedEntryBuilders) {
			if (!em.isSupported(object))
				continue;
			entry = em.buildMappedEntry(object);
			if (entry != null){
				buildEntry(entry);
				return entry;
			}
		}
		throw new NoMappedEntryException("the class["+ReflectUtils.getObjectClass(object)+"] is not a dbm entity, please use @DbmEntity or @Entity!");
//		return entry;
	}
	
	
	/***
	 * 
	 */
	@Override
	public DbmMappedEntry getReadOnlyEntry(final Class<?> clazz) {
		Assert.notNull(clazz, "the class arg can not be null!");
		DbmMappedEntry entry = null;
		String key = getCacheKey(clazz);
		try {
			entry = readOnlyEntryCaches.get(key, ()->{
				AnnotationInfo annotationInfo = new AnnotationInfo(clazz);
				JdbcRowEntryImpl rowEntry = new JdbcRowEntryImpl(annotationInfo, serviceRegistry);
				DbmMappedEntry value = buildMappedFields(rowEntry);

				if (value == null)
					throw new NoMappedEntryException("can find build entry for class : " + clazz);

				buildEntry(value, false);
				value.freezing();
				return value;
			});
		} catch (ExecutionException e) {
			throw new DbmException("create entry error for class: " + clazz, e);
		}
		return entry;
	}


	@Override
	public DbmMappedEntry buildMappedFields(DbmMappedEntry entry) {
		for (MappedEntryBuilder em : mappedEntryBuilders) {
			entry = em.buildMappedFields(entry);
			if (entry != null){
				return entry;
			}
		}
		return entry;
	}


	public boolean isSupportedMappedEntry(Object entity){
		if(mappedEntryBuilders.isEmpty())
			return false;
		return mappedEntryBuilders.stream().filter(b->b.isSupported(entity))
											.findAny()
											.isPresent();
	}


	public String getCacheKey(Object object) {
		String key = null;
		if (Map.class.isInstance(object)) {
			//no cache
		} else {
			Class<?> entityClass = ReflectUtils.getObjectClass(LangUtils.getFirst(object));
			key = entityClass.getName()+"#"+entityClass.hashCode();
		}
		return key;
	}

	public void setMappedEntryBuilder(List<MappedEntryBuilder> managers) {
		this.mappedEntryBuilders = managers;
	}

}
