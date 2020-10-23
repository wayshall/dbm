package org.onetwo.dbm.mapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import org.onetwo.common.annotation.AnnotationInfo;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.JFishFieldInfoImpl;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.common.utils.JFishPropertyInfoImpl;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.RegisterManager;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.annotation.DbmColumn;
import org.onetwo.dbm.annotation.DbmEntity;
import org.onetwo.dbm.annotation.DbmGeneratedValue;
import org.onetwo.dbm.annotation.DbmQueryable;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.id.StrategyType;
import org.onetwo.dbm.jpa.GeneratedValueIAttrs;
import org.onetwo.dbm.query.DbmQueryableMappedEntryImpl;
import org.onetwo.dbm.utils.DBUtils;
import org.onetwo.dbm.utils.SpringAnnotationFinder;
import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;

@SuppressWarnings("rawtypes")
public class DbmMappedEntryBuilder implements MappedEntryBuilder, RegisterManager<String, MappedEntryBuilderListener> {

	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
//	private Map<String, JFishMappedEntry> entryCache = new HashMap<String, JFishMappedEntry>();
	private boolean byProperty = true;
	private DBDialect dialect;
	
	private int order = Ordered.LOWEST_PRECEDENCE;
	
	private MappedEntryBuilderListenerManager listenerManager;
	
	final private Map<String, MappedEntryBuilderListener> builderListeners = new LinkedHashMap<>();
	protected final DbmInnerServiceRegistry serviceRegistry;

	public Map<String, MappedEntryBuilderListener> getRegister() {
		return builderListeners;
	}

	public DbmMappedEntryBuilder(DbmInnerServiceRegistry serviceRegistry){
		this.serviceRegistry = serviceRegistry;
		this.dialect = this.serviceRegistry.getDialect();
	}
	
	@Override
	public void initialize() {
		Assert.notNull(dialect);
		listenerManager = new MappedEntryBuilderListenerManager(builderListeners.values());
	}

	protected DBDialect getDialect() {
		return dialect;
	}


	public boolean isSupported(Object entity){
		if(MetadataReader.class.isInstance(entity)){
			MetadataReader metadataReader = (MetadataReader) entity;
			AnnotationMetadata am = metadataReader.getAnnotationMetadata();
			return am.hasAnnotation(DbmEntity.class.getName()) || am.hasAnnotation(DbmQueryable.class.getName());
		}else{
			Class<?> entityClass = ReflectUtils.getObjectClass(entity);
			return entityClass.getAnnotation(DbmEntity.class)!=null || entityClass.getAnnotation(DbmQueryable.class)!=null;
		}
	}
	
//	@Override
//	public JFishMappedEntry findEntry(Object object) {
//		try {
//			return getEntry(object);
//		} catch (Exception e) {
//			logger.error("can find the entry : " + object);
//		}
//		return null;
//	}

	/* (non-Javadoc)
	 * @see org.onetwo.common.db.wheel.MappedEntityManager#getEntry(java.lang.Object)
	 */
	public DbmMappedEntry getEntry(Object objects){
		Assert.notNull(objects, "entity can not be null");
		DbmMappedEntry entry = null;
		
		Object object = LangUtils.getFirst(objects);
		if(!isSupported(object)){
			LangUtils.throwBaseException("don't support this object : " + object);
//			return null;
		}
		
		if(object instanceof Map){
			Map mapEntity = (Map)object;
//			boolean removeMeta = true;
			/*if(mapEntity.containsKey(TMeta.entity_meta)){
				mapEntity = (Map)mapEntity.get(TMeta.entity_meta);
//				removeMeta = false;
			}*/
			
			entry = buildMappedEntry(mapEntity);
		}else if(object instanceof Class){
			Class entityClass = (Class<?>)object;
			
			/*key = entityClass.getName();
			entry = getFromCache(key);
			if(entry!=null)
				return entry;*/
			
			entry = buildMappedEntry(entityClass, byProperty);
		}else{
			Class entityClass = object.getClass();
			
			/*key = entityClass.getName();
			entry = getFromCache(key);
			if(entry!=null)
				return entry;*/
			
			entry = buildMappedEntry(entityClass, byProperty);
		}
		
		if(entry==null)
			LangUtils.throwBaseException("no entry created : "+object);
		
//		putInCache(key, entry);
		
		return entry;
	}
	

	@Override
	public DbmMappedEntry buildMappedEntry(Object object) {
		Object entity = LangUtils.getFirst(object);
		Class<?> entityClass = ReflectUtils.getObjectClass(entity);
		return buildMappedEntry(entityClass, byProperty);
	}
	
	protected void afterBuildMappedEntry(DBDialect dbDialect, DbmMappedEntry entry){
		this.checkIdStrategy(dbDialect, entry); 
	}
	
	/********
	 * 自动检查更正id策略
	 * @param dbDialect
	 * @param entry
	 */
	protected void checkIdStrategy(DBDialect dbDialect, DbmMappedEntry entry){
		entry.getIdentifyFields().forEach(identifyField -> {
			checkIdStrategy(dbDialect, identifyField);
		});
	}
	protected void checkIdStrategy(DBDialect dbDialect, DbmMappedField identifyField){
		if(identifyField==null)
			return ;
		if(identifyField.getStrategyType()!=null && !dbDialect.isSupportedIdStrategy(identifyField.getStrategyType())){
			if(!dbDialect.isAutoDetectIdStrategy()){
				throw new DbmException("database["+dbDialect.getDbmeta().getDbName()+"] do not support this strategy : " + identifyField.getStrategyType());
			}else{
				identifyField.setStrategyType(dbDialect.getIdStrategy().get(0));
			}
		}
	}
	
	/*********
	 * 1. 创建一个实体映射对象
	 * 2. 把所有实体的注解添加到对象
	 * @param entityClass
	 * @return
	 */
	@SuppressWarnings("deprecation")
	protected DbmMappedEntry createDbmMappedEntry(AnnotationInfo annotationInfo){
		AbstractDbmMappedEntryImpl entry = null;
		Class<?> entityClass = annotationInfo.getSourceClass();
		DbmEntity jentity = entityClass.getAnnotation(DbmEntity.class);
		DbmQueryable jqueryable = entityClass.getAnnotation(DbmQueryable.class);

		if(jentity==null && jqueryable==null)
			throw new DbmException("it's not a valid entity : " + entityClass);

		TableInfo tableInfo = newTableInfo(annotationInfo);
		if(jqueryable!=null){
			entry = new DbmQueryableMappedEntryImpl(jentity.name(), annotationInfo, tableInfo, serviceRegistry);
		}else{
			if(jentity.type()==MappedType.QUERYABLE_ONLY){
				entry = new DbmQueryableMappedEntryImpl(jentity.name(), annotationInfo, tableInfo, serviceRegistry);
			}/*else if(jentity.type()==MappedType.JOINED){
				entry = new JFishJoinedMappedEntryImpl(annotationInfo, tableInfo);
			}*/else{
				entry = new DbmMappedEntryImpl(jentity.name(), annotationInfo, tableInfo, serviceRegistry);
			}
		}
		entry.setSqlBuilderFactory(this.dialect.getSqlBuilderFactory());
		return entry;
	}
	
	protected void buildIdGeneratorsOnClass(DbmMappedEntry entry){
		AnnotationInfo annotationInfo = entry.getAnnotationInfo();
		IdGeneratorFactory.createDbmIdGenerator(annotationInfo)
							.ifPresent(idGenerator->entry.addIdGenerator(idGenerator));
	}

	/*********
	 * build entity mapped info and put it into cache
	 * @param entityClass
	 * @param byProperty
	 * @return
	 */
	public DbmMappedEntry buildMappedEntry(Class<?> entityClass, boolean byProperty) {
		AnnotationInfo annotationInfo = new AnnotationInfo(entityClass);
		DbmMappedEntry entry = createDbmMappedEntry(annotationInfo);
		this.listenerManager.notifyAfterCreatedMappedEntry(entry);

		this.buildIdGeneratorsOnClass(entry);
		if(byProperty){
			entry = buildMappedEntryByProperty(entry);
		}else{
			entry = buildMappedEntryByField(entry);
		}
//		buildMappedEntryProperties(entry, byProperty);
		
//		TableInfo tableInfo = entry.getTableInfo();
//		entry.build(tableInfo);
		afterBuildMappedEntry(dialect, entry);
		this.listenerManager.notifyAfterBuildMappedEntry(entry);
		
		return entry;
	}
	
	@Override
	public DbmMappedEntry buildMappedFields(DbmMappedEntry entry) {
		return buildMappedEntryByField(entry);
	}

	protected DbmMappedEntry buildMappedEntryByProperty(DbmMappedEntry entry) {
		Class<?> entityClass = entry.getEntityClass();
		PropertyDescriptor[] props = ReflectUtils.desribProperties(entityClass);
		
		AbstractMappedField mfield = null;
//		List<AbstractMappedField> mfieldList = new ArrayList<AbstractMappedField>(props.length);
		for(PropertyDescriptor prop : props){
			mfield = createAndBuildMappedField(entry, new JFishPropertyInfoImpl(entityClass, prop));
			if(mfield==null)
				continue;
			this.listenerManager.notifyAfterBuildMappedField(entry, mfield);
			entry.addMappedField(mfield);
		}
		return entry;
	}

	protected DbmMappedEntry buildMappedEntryByField(DbmMappedEntry entry) {
		Class<?> entityClass = entry.getEntityClass();
		Collection<Field> fields = ReflectUtils.findFieldsFilterStatic(entityClass);
		
		AbstractMappedField mfield = null;
		for(Field field : fields){
			mfield = createAndBuildMappedField(entry, new JFishFieldInfoImpl(entityClass, field));
			if(mfield==null)
				continue;
			entry.addMappedField(mfield);
		}

		return entry;
	}
	
	protected boolean ignoreMappedField(JFishProperty field){
		return Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers());
	}

	protected TableInfo newTableInfo(AnnotationInfo entry){
		TableInfo tableInfo = new TableInfo(buildTableName(entry));
		tableInfo.setSeqName(buildSeqName(entry, tableInfo));
		return tableInfo;
	}
	
	protected String buildSeqName(AnnotationInfo entry, TableInfo tableInfo){
//		String sname = entry.getEntityClass().getSimpleName();
		String sname = "SEQ_" + tableInfo.getName().toUpperCase();
		return sname;
	}
	
	protected String buildTableName(AnnotationInfo entry){
		String tname = null;
		if(entry.getAnnotation(DbmQueryable.class)!=null){
			DbmQueryable queryable = entry.getAnnotation(DbmQueryable.class);
			tname = queryable.table();
		}
		
		//JFishEntity's table name will be override JFishQueryable
		if(entry.getAnnotation(DbmEntity.class)!=null){
			DbmEntity jfishEntity = entry.getAnnotation(DbmEntity.class);
			tname = jfishEntity.table();
		}
		if(StringUtils.isBlank(tname))
			tname = StringUtils.convert2UnderLineName(entry.getSourceClass().getSimpleName());
		/*if(StringUtils.isBlank(tname)){
			LangUtils.throwBaseException("table can not be empty: " + entry.getEntityClass());
		}*/
		return tname;
	}

	protected BaseColumnInfo buildColumnInfo(TableInfo tableInfo, DbmMappedField field){
//		Method method = ReflectUtils.getReadMethod(entityClass, field.getProperty());
		
		String colName = null;
		DbmColumn jc = field.getPropertyInfo().getAnnotation(DbmColumn.class);
		int sqlType = DBUtils.TYPE_UNKNOW;
		if (jc!=null) {
			colName = jc.name();
			sqlType = jc.sqlType();
		}
		
		colName = convertColumnName(field, colName);

		if (sqlType==DBUtils.TYPE_UNKNOW) {
			sqlType = dialect.getTypeMapping().getType(field.getPropertyInfo().getType());
		}
		ColumnInfo col = new ColumnInfo(tableInfo, colName, sqlType);
		col.setJavaType(field.getPropertyInfo().getType());
		col.setPrimaryKey(field.isIdentify());
		if (field.isIdentify()) {
			col.setInsertable(!field.isIdentityStrategy());
			col.setUpdatable(!field.isIdentityStrategy());
		}
		
		return col;
	}
	
	protected String convertColumnName(DbmMappedField field, String colName) {
		if (StringUtils.isBlank(colName)) {
			colName = field.getName();
			colName = StringUtils.convert2UnderLineName(colName);
		} 
		return colName;
	}
	
	/*protected void buildPKColumnInfo(AbstractMappedField field, ColumnInfo col){
		col.setPrimaryKey(true);
	}*/

	protected DbmMappedEntry buildMappedEntry(Map entity){
		throw new IllegalArgumentException("unsupported map entity : " + entity);
		/*JFishMappedEntry entry = new JFishMappedEntryImpl(entity.getClass());
		//TODO
		return entry;*/
	}

	protected Object getValueFromMapEntity(Map entity, String key, boolean remove){
		if(!entity.containsKey(key))
			return null;
		Object val = entity.get(key);
		if(remove)
			entity.remove(key);
		return val;
	}
	
	/*******
	 * 通过类的属性
	 * 1. 创建字段映射对象
	 * 2. 添加字段的所有注解到对象
	 * 3. 设置对象是否为identify
	 * @param entry
	 * @param prop
	 * @return
	 */

	protected AbstractMappedField createAndBuildMappedField(DbmMappedEntry entry, JFishProperty prop){
		/*if(ignoreMappedField(prop)) {
			return null;
		}*/
		// 替换注解查找策略
		prop.getAnnotationInfo().setAnnotationFinder(SpringAnnotationFinder.INSTANCE);
		
		AbstractMappedField mfield = newMappedField(entry, prop);
		this.buildMappedField(mfield);
		
		// transient
		if (!ignoreMappedField(prop)) {
			BaseColumnInfo col = this.buildColumnInfo(entry.getTableInfo(), mfield);
			
			//设置关系
			if(col!=null){
				mfield.setColumn(col);
				if(entry.getTableInfo()!=null){
					entry.getTableInfo().addColumn(col);
				}
			}
		}
		return mfield;
	}
	
	protected AbstractMappedField newMappedField(DbmMappedEntry entry, JFishProperty prop){
		DbmMappedProperty mfield = new DbmMappedProperty(entry, prop);
		return mfield;
	}

	protected void buildMappedField(DbmMappedField mfield){
		if("id".equals(mfield.getName()))
			mfield.setIdentify(true);
		if(this.getDialect().getDbmeta().isMySQL()){
			mfield.setStrategyType(StrategyType.IDENTITY);
		}else if(this.getDialect().getDbmeta().isOracle()){
			mfield.setStrategyType(StrategyType.SEQ);
		}
		
//		this.buildMappedFieldByAnnotations(mfield);
	}
	

	protected void buildIdGeneratorsOnField(DbmMappedField mfield){
		GeneratedValueIAttrs generatedValueIAttrs = null;

		AnnotationInfo annotationInfo = mfield.getPropertyInfo().getAnnotationInfo();
		
		GeneratedValue g = annotationInfo.getAnnotation(GeneratedValue.class);
		DbmGeneratedValue dg = annotationInfo.getAnnotation(DbmGeneratedValue.class);
		if (dg!=null) {
			GenerationType type = dg.strategy();
			generatedValueIAttrs = new GeneratedValueIAttrs(type, dg.generator());
		} else if (g!=null) {
			GenerationType type = g.strategy();
			generatedValueIAttrs = new GeneratedValueIAttrs(type, g.generator());
		}
		
		mfield.setGeneratedValueIAttrs(generatedValueIAttrs);
		IdGeneratorFactory.createDbmIdGenerator(annotationInfo)
							.ifPresent(idGenerator->mfield.addIdGenerator(idGenerator));
	}
	
	/*private void buildMappedFieldByAnnotations(AbstractMappedField mfield){
		//TODO
		JFishMeta jfishColumn = mfield.getPropertyInfo().getAnnotation(JFishMeta.class);
		if(mfield.getPropertyInfo().hasAnnotation(JFishMeta.class)){
			this.buildJFishGen(mfield, jfishColumn);
		}
	}
	
	private void buildJFishGen(AbstractMappedField mfield, JFishMeta gen){
		//TODO
	}*/

	public void setDialect(DBDialect dialect) {
		this.dialect = dialect;
	}


	public int getOrder() {
		return order;
	}


	public void setOrder(int order) {
		this.order = order;
	}

	public void setListenerManager(MappedEntryBuilderListenerManager listenerManager) {
		this.listenerManager = listenerManager;
	}

}
