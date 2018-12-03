package org.onetwo.dbm.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.IdClass;

import org.onetwo.common.annotation.AnnotationInfo;
import org.onetwo.common.db.TimeRecordableEntity;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.utils.ArrayUtils;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.annotation.DbmEntityListeners;
import org.onetwo.dbm.annotation.DbmFieldListeners;
import org.onetwo.dbm.annotation.DbmValidatorEnabled;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.event.spi.DbmEventAction;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.id.IdentifierGenerator;
import org.onetwo.dbm.mapping.SQLBuilderFactory.SqlBuilderType;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.beans.ConfigurablePropertyAccessor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings({"unchecked", "rawtypes"})
abstract public class AbstractDbmMappedEntryImpl implements DbmMappedEntry {
	public static final Comparator SORT_BY_LENGTH = new Comparator<AbstractMappedField>() {

		@Override
		public int compare(AbstractMappedField o1, AbstractMappedField o2) {
			return (o1.getName().length()-o2.getName().length());
		}
		
	};
	
	private Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	private Class entityClass;
	private String entityName;
	private boolean dynamic;
	private TableInfo tableInfo;
//	private boolean queryableOnly;
	private AnnotationInfo annotationInfo;

	protected Map<String, AbstractMappedField> mappedFields = new LinkedHashMap<String, AbstractMappedField>();
	protected Map<String, AbstractMappedField> mappedColumns = new HashMap<String, AbstractMappedField>();
	private Class<?> idClass = null;
	private List<DbmMappedField> identifyFields = Lists.newArrayList();
	private DbmMappedField versionField;
	
	private SQLBuilderFactory sqlBuilderFactory;
	
//	private DataHolder<String, Object> dataHolder = new DataHolder<String, Object>();

	private boolean freezing;
	
	private List<DbmEntityListener> entityListeners = Collections.EMPTY_LIST;
	private List<DbmEntityFieldListener> fieldListeners = Collections.EMPTY_LIST;

	private final boolean enabledEntithyValidator;
//	private final SimpleDbmInnserServiceRegistry serviceRegistry;
	private EntityValidator entityValidator;
	
	private DbmTypeMapping sqlTypeMapping;
	
	private Map<String, IdentifierGenerator<?>> idGenerators = Maps.newHashMap();
	
	private final DBDialect dbDialect;
	
	private boolean idMappingOnField = true;
	
//	private Map<SqlBuilderType, EntrySQLBuilder> entrySQLBuilderMap = Maps.newHashMap();
	/*public AbstractJFishMappedEntryImpl(AnnotationInfo annotationInfo) {
		this(annotationInfo, null);
	}*/
	
	public AbstractDbmMappedEntryImpl(AnnotationInfo annotationInfo, TableInfo tableInfo, DbmInnerServiceRegistry serviceRegistry) {
		this.dbDialect = serviceRegistry.getDialect();
		this.entityClass = annotationInfo.getSourceClass();
		this.annotationInfo = annotationInfo;
		this.sqlTypeMapping = serviceRegistry.getTypeMapping();
//		this.serviceRegistry = serviceRegistry;
		this.entityName = this.entityClass.getName();
		this.tableInfo = tableInfo;
		IdClass idclass = annotationInfo.getAnnotation(IdClass.class);
		if (idclass!=null) {
			this.idClass = idclass.value();
		}
		if(Map.class.isAssignableFrom(entityClass)){
			this.setDynamic(true);
		}
		DbmEntityListeners listenersAnntation = annotationInfo.getAnnotation(DbmEntityListeners.class);
		if(listenersAnntation!=null){
			Class<? extends DbmEntityListener>[] listenerClasses = listenersAnntation.value();
			entityListeners = LangUtils.newArrayList(listenerClasses.length);
			for(Class<? extends DbmEntityListener> lcs : listenerClasses){
				DbmEntityListener listener = ReflectUtils.newInstance(lcs);
				entityListeners.add(listener);
			}
		}

		DbmFieldListeners fieldListenersAnntation = annotationInfo.getAnnotation(DbmFieldListeners.class);
		if(fieldListenersAnntation!=null){
			this.fieldListeners = DbmUtils.initDbmEntityFieldListeners(fieldListenersAnntation);
		}
		this.enabledEntithyValidator = annotationInfo.hasAnnotation(DbmValidatorEnabled.class);
		if(enabledEntithyValidator){
			this.entityValidator = serviceRegistry.getEntityValidator();
			Assert.notNull(entityValidator, "no entity validator config!");
		}
//		this.buildIdGenerators();
	}
	
	/*protected void putEntrySQLBuilder(SqlBuilderType type, EntrySQLBuilder builder){
		this.entrySQLBuilderMap.put(type, builder);
	}*/
	
	public boolean isIdMappingOnField() {
		return idMappingOnField;
	}

	public void setIdMappingOnField(boolean idMappingOnField) {
		this.idMappingOnField = idMappingOnField;
	}

	public DBDialect getDbDialect() {
		return dbDialect;
	}

	@Override
	public void addIdGenerator(IdentifierGenerator<?> idGenerator){
		this.idGenerators.put(idGenerator.getName(), idGenerator);
	}
	

	/*private void buildIdGenerators(){
		SequenceGenerator sg = annotationInfo.getAnnotation(SequenceGenerator.class);
		if(sg!=null){
			IdGenerator<Long> idGenerator = IdGeneratorFactory.create(sg);
			this.idGenerators.put(idGenerator.getName(), idGenerator);
		}
		TableGenerator tg = annotationInfo.getAnnotation(TableGenerator.class);
		if(sg!=null){
			IdGenerator<Long> idGenerator = IdGeneratorFactory.create(tg);
			this.idGenerators.put(idGenerator.getName(), idGenerator);
		}
	}*/

	/*@Override
	public String getStaticSeqSql() {
		throw new UnsupportedOperationException("the queryable entity unsupported this operation!");
	}
	@Override
	public String getStaticCreateSeqSql() {
		throw new UnsupportedOperationException("the queryable entity unsupported this operation!");
	}*/
	
	public Class<?> getIdClass() {
		return idClass;
	}
	
	public boolean isCompositePK() {
		return this.idClass!=null;
	}

	public Map<String, IdentifierGenerator<?>> getIdGenerators() {
		return idGenerators;
	}

	public DbmTypeMapping getSqlTypeMapping() {
		return sqlTypeMapping;
	}

	public Collection<AbstractMappedField> getFields(){
		return this.mappedFields.values();
	}
	
	public Collection<AbstractMappedField> getFields(DbmMappedFieldType... types){
		List<AbstractMappedField> flist = new ArrayList<AbstractMappedField>(mappedFields.values().size());
		if(LangUtils.isEmpty(types)){
			Collections.sort(flist, SORT_BY_LENGTH);
			return flist;
		}
		for(AbstractMappedField field : mappedFields.values()){
			if(ArrayUtils.contains(types, field.getMappedFieldType())){
				flist.add(field);
			}
		}
		Collections.sort(flist, SORT_BY_LENGTH);
		return flist;
	}
	
	public boolean isInstance(Object entity){
		return entityClass.isInstance(entity);
	}
	
	public boolean hasIdentifyValue(Object entity){
		return getId(entity)!=null;
	}
	
	void setTableInfo(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}
	
	public void checkIdType(Object value) {
		if (!isCompositePK()) {
			return ;
		}
		if (!idClass.isInstance(value)) {
			throw new DbmException("the id value must be a instance of " + this.idClass);
		}
	}

	@Override
	public void setId(Object entity, Object value) {
//		this.checkIdType(value);
//		getIdentifyField().setValue(entity, value);
		if (!isCompositePK()) {
			this.getIdentifyFields().get(0).setValue(entity, value);
			return ;
		}
		if (!idClass.isInstance(value)) {
			throw new DbmException("the id value must be a instance of " + this.idClass);
		}
		for (DbmMappedField field : this.getIdentifyFields()) {
			Object fieldValue = field.getValue(value);
			field.setValue(entity, fieldValue);
		}
	}
	
	/***
	 * 如果是复合主键，只要有一个主键为null，即返回null
	 */
	@Override
	public Object getId(Object entity){
//		if(getIdentifyField()==null)
//			return null;
//		return getIdentifyField().getValue(entity);
		if (!isCompositePK()) {
			Object idValue = this.getIdentifyFields().get(0).getValue(entity);
			return idValue;
		}
		Object idValue = ReflectUtils.newInstance(idClass);
		ConfigurablePropertyAccessor accesor = SpringUtils.newPropertyAccessor(idValue, true);
		for (DbmMappedField field : this.getIdentifyFields()) {
			Object fieldValue = field.getValue(entity);
			// 如果是复合主键，只要有一个主键为null，即返回null
			if (fieldValue==null) {
				return null;
			}
			accesor.setPropertyValue(field.getName(), fieldValue);
		}
		return idValue;
	}
	
	@Override
	public void setFieldValue(Object entity, String fieldName, Object value){
		getField(fieldName).setValue(entity, value);
	}
	
	@Override
	public DbmMappedField getField(String fieldName){
		DbmMappedField field = mappedFields.get(fieldName);
		if(field==null)
			throw new DbmException(getEntityName()+" no field mapped : " + fieldName);
		return field;
	}
	
	@Override
	public boolean contains(String field){
		return this.mappedFields.containsKey(field);
	}
	
	@Override
	public boolean containsColumn(String col){
		Assert.hasText(col, "column name can not be empty");
		return this.mappedColumns.containsKey(col.toLowerCase());
	}
	
	@Override
	public String getColumnName(String field){
		return getField(field).getColumn().getName();
	}
	
	@Override
	public DbmMappedField getFieldByColumnName(String columnName){
		columnName = columnName.toLowerCase();
		DbmMappedField field = mappedColumns.get(columnName);
		if(field==null)
			throw new DbmException(getEntityName() + " no column mapped : " + columnName);
		return field;
	}
	
	@Override
	public Object getFieldValue(Object entity, String fieldName){
		return getField(fieldName).getValue(entity);
	}
	
	protected void checkEntry(){
		if(getMappedType()!=MappedType.ENTITY)
			return ;
		if(getIdentifyFields().isEmpty()){
			throw new DbmException("the entity must has a identify : " + getEntityClass());
		}
	}

	/*@Override
	public void build(TableInfo tableInfo) {
		this.checkEntry();
//		buildTableInfo();
		this.tableInfo = tableInfo;
		buildStaticSQL(tableInfo);
		
		for(AbstractMappedField field : this.mappedFields.values()){
			this.mappedColumns.put(field.getColumn().getName().toLowerCase(), field);
		}

		this.mappedFields = Collections.unmodifiableMap(this.mappedFields);
		this.mappedColumns = Collections.unmodifiableMap(this.mappedColumns);
	}*/

	/**********
	 * 此方法会延迟调用，会设置各种属性和manager的事件回调后，才会调用，
	 * 所以，如果没有实现扫描和构建所有实体，而在运行时才build，就要注意多线程的问题
	 */
	@Override
	public void buildEntry() {
		this.checkEntry();
		
		for(AbstractMappedField field : this.mappedFields.values()){
			if(field.getColumn()!=null)
				this.mappedColumns.put(field.getColumn().getName().toLowerCase(), field);
		}

		this.mappedFields = Collections.unmodifiableMap(this.mappedFields);
		this.mappedColumns = Collections.unmodifiableMap(this.mappedColumns);
		
//		buildTableInfo();
		buildStaticSQL(tableInfo);
		
//		freezing();
	}

	protected void buildStaticSQL(TableInfo taboleInfo){
	}
	
	public EntrySQLBuilderImpl createSQLBuilder(SqlBuilderType type){
//		SQLBuilder sqlb = SQLBuilder.createNamed(tableInfo.getName(), tableInfo.getAlias(), type);
		EntrySQLBuilderImpl sqlb = sqlBuilderFactory.createQMark(this, tableInfo.getAlias(), type);
		return sqlb;
	}
	
	/*public JdbcStatementContextBuilder createJdbcStatementContextBuilder(SqlBuilderType type){
		EntrySQLBuilderImpl sb = sqlBuilderFactory.createQMark(this, this.getTableInfo().getAlias(), type);
		JdbcStatementContextBuilder sqlb = JdbcStatementContextBuilder.create(null, this, sb);
		return sqlb;
	}*/
	
	@Override
	public DbmMappedEntry addMappedField(AbstractMappedField field){
		Assert.notNull(field);
		
		this.mappedFields.put(field.getName(), field);
		
		if (field.isIdentify()) {
			this.identifyFields.add(field);
		} else if (field.isVersionControll()) {
			if(versionField!=null)
				throw new DbmException("a version field has already exist : " + versionField.getName());
			this.versionField = field;
		} else if (field.isMappingGenerated()) {
			//TODO add to generatedFieldMapOnInsert or generatedFieldMapOnUpdate
		}

		/*if(field.isJoinTable()){
			addJoinableField((JoinableMappedField)field);
		}*/
		/*if(field.getMappedFieldType()==JFishMappedFieldType.TO_ONE){
			Assert.isInstanceOf(CascadeMappedField.class, field);
			addToOneField((CascadeMappedField)field);
			
		}else if(field.getMappedFieldType()==JFishMappedFieldType.ONE_TO_MANY){
			Assert.isInstanceOf(CascadeCollectionMappedField.class, field);
			addOneToManyField((CascadeCollectionMappedField)field);
			
		}else if(field.getMappedFieldType()==JFishMappedFieldType.MANY_TO_MANY){
			Assert.isInstanceOf(CascadeCollectionMappedField.class, field);
			addManyToManyField((CascadeCollectionMappedField)field);
		}*/
		
		return this;
	}
	
	/*public void addToOneField(CascadeMappedField manyToOne){
		throw new UnsupportedOperationException("unsupported this operation : " + getClass());
	}
	
	public void addOneToManyField(CascadeCollectionMappedField oneToMany){
		throw new UnsupportedOperationException("unsupported this operation : " + getClass());
	}
	
	public void addManyToManyField(CascadeCollectionMappedField oneToMany){
		throw new UnsupportedOperationException("unsupported this operation : " + getClass());
	}*/
	
	/*public void addManyToManyField(AbstractMappedField oneToMany){
		throw new UnsupportedOperationException("unsupported this operation : " + getClass());
	}*/
	
	/*public void addJoinableField(JoinableMappedField joinalbe){
		throw new UnsupportedOperationException("unsupported this operation : " + getClass());
	}*/

	@Override
	public Class<?> getEntityClass() {
		return entityClass;
	}

	@Override
	public <T> T newInstance() {
		return (T)ReflectUtils.newInstance(entityClass);
	}

	@Override
	public boolean isDynamic() {
		return dynamic;
	}

	@Override
	public void setDynamic(boolean dynamic) {
		this.checkFreezing("setDynamic");
		this.dynamic = dynamic;
	}

	@Override
	public TableInfo getTableInfo() {
		return tableInfo;
	}

	@Override
	public List<DbmMappedField> getIdentifyFields() {
		return identifyFields;
	}

	@Override
	public DbmMappedField getVersionField() {
		return versionField;
	}

	@Override
	public Object getVersionValue(Object entity) {
		return versionField.getValue(entity);
	}

	protected void throwIfQueryableOnly(){
		if(isQueryableOnly()){
			LangUtils.throwBaseException("this entity is only for query  : " + this.entityClass);
		}
	}

	abstract protected EntrySQLBuilderImpl getStaticInsertSqlBuilder();
	abstract protected EntrySQLBuilderImpl getStaticUpdateSqlBuilder();
	abstract protected EntrySQLBuilderImpl getStaticDeleteSqlBuilder();
	abstract protected EntrySQLBuilderImpl getStaticFetchSqlBuilder();
	/*protected EntrySQLBuilderImpl getStaticSelectLockSqlBuilder(){
		throw new UnsupportedOperationException("getStaticSelectLockSqlBuilder");
	}*/
	abstract protected EntrySQLBuilder getStaticFetchAllSqlBuilder();
	abstract protected EntrySQLBuilder getStaticSelectVersionSqlBuilder();
	
	protected EntrySQLBuilder getStaticDeleteAllSqlBuilder(){
		throw new UnsupportedOperationException("the "+getMappedType()+" entity unsupported this operation!");
	}

	@Override
	public JdbcStatementContext<Object[]> makeFetchAll(){
		EntrySQLBuilder sqlb = getStaticFetchAllSqlBuilder();
		sqlb.build();
//		KVEntry<String, Object[]> kv = new KVEntry<String, Object[]>(sql, (Object[])null);
		JdbcStatementContext<Object[]> kv = SimpleJdbcStatementContext.create(sqlb, (Object[])null);
		return kv;
	}

	@Override
	public JdbcStatementContext<Object[]> makeDeleteAll(){
		EntrySQLBuilder sqlb = getStaticDeleteAllSqlBuilder();
		sqlb.build();
//		KVEntry<String, Object[]> kv = new KVEntry<String, Object[]>(sql, (Object[])null);
		JdbcStatementContext<Object[]> kv = SimpleJdbcStatementContext.create(sqlb, (Object[])null);
		return kv;
	}
	

	@Override
	public JdbcStatementContext<Object[]> makeSelectVersion(Object object){
		if(LangUtils.isMultiple(object)){
			throw new DbmException("not a entity: " + object);
		}
		EntrySQLBuilder sqlb = getStaticSelectVersionSqlBuilder();
		sqlb.build();
		JdbcStatementContext<Object[]> kv = SimpleJdbcStatementContext.create(sqlb, new Object[]{getId(object), getVersionField().getValue(object)});
		return kv;
	}
	
	/*****
	 * @param objects 
	 * @param isIdentify 传入的objects是否是实体id
	 */
	@Override
	public JdbcStatementContext<List<Object[]>> makeFetch(Object objects, boolean isIdentify){
		JdbcStatementContextBuilder dsb = JdbcStatementContextBuilder.create(DbmEventAction.find, this, getStaticFetchSqlBuilder());
		
		if(LangUtils.isMultiple(objects)){
			//actual not support
			List<Object> list = LangUtils.asList(objects);
			for(Object id : list){
				if (isIdentify) {
					this.addIdCauseValue(dsb, id);
				} else {
					dsb.processWhereCauseValuesFromEntity(id);
				}
				dsb.addBatch();
			}
		}else{
			if (isIdentify) {
//				dsb.addCauseValue(objects);
				this.addIdCauseValue(dsb, objects);
			} else {
				dsb.processWhereCauseValuesFromEntity(objects);
			}
		}
		
		dsb.build();
//		KVEntry<String, List<Object[]>> kv = new KVEntry<String, List<Object[]>>(dsb.getSql(), dsb.getValues());
		JdbcStatementContext<List<Object[]>> kv = SimpleJdbcStatementContext.create(dsb.getSqlBuilder(), dsb.getValue());
		return kv;
	}
	
	private void addIdCauseValue(JdbcStatementContextBuilder dsb, Object idObject) {
		if (!isCompositePK()) {
			dsb.addCauseValue(idObject);
			return ;
		}
		// 复合主键存在多个id的情况
		EntrySQLBuilder sqlBuilder = dsb.getSqlBuilder();
		for (DbmMappedField idField : sqlBuilder.getWhereCauseFields()) {
			Object idValue = idField.getValue(idObject);
			dsb.addCauseValue(idValue);
		}
	}
	

	/*@Override
	public JdbcStatementContext<Object[]> makeLockSelect(Object object, LockInfo lock){
		if(LangUtils.isMultiple(object)){
			throw new DbmException("can not lock multiple object: " + object.getClass());
		}
		JdbcStatementContextBuilder dsb = JdbcStatementContextBuilder.create(DbmEventAction.lock, this, getStaticFetchSqlBuilder());
		dsb.addCauseValue(object);
		dsb.build();
		String sql = dsb.getSqlBuilder().getSql() + this.getd;
		JdbcStatementContext<Object[]> kv = new SimpleJdbcStatementContext<Object[]>(sql, dsb.getValue().get(0));
		//SimpleJdbcStatementContext.create(dsb.getSqlBuilder(), dsb.getValue().get(0));
		return kv;
	}*/
	
	@Override
	public JdbcStatementContext<List<Object[]>> makeInsert(Object entity){
		this.throwIfQueryableOnly();
		EntrySQLBuilderImpl insertSqlBuilder = getStaticInsertSqlBuilder();
		JdbcStatementContextBuilder dsb = JdbcStatementContextBuilder.create(DbmEventAction.insert, this, insertSqlBuilder);
		if(LangUtils.isMultiple(entity)){
			List<Object> list = LangUtils.asList(entity);
			if(LangUtils.isEmpty(list))
				return null;
			for(Object en : list){
				this.processIBaseEntity(en, true);
				this.vailidateEntity(en);
//				dsb.setColumnValuesFromEntity(en).addBatch();
//				doEveryMappedFieldInStatementContext(dsb, en).addBatch();
				dsb.processColumnValues(en).addBatch();
			}
		}else{
			this.processIBaseEntity(entity, true);
			this.vailidateEntity(entity);
//			dsb.setColumnValuesFromEntity(entity);
			dsb.processColumnValues(entity);
		}
		dsb.build();
//		KVEntry<String, List<Object[]>> kv = new KVEntry<String, List<Object[]>>(dsb.getSql(), dsb.getValues());
//		JdbcStatementContext<List<Object[]>> context = SqlBuilderJdbcStatementContext.create(dsb.getSql(), dsb); 
		return dsb;
	}
	
	private void vailidateEntity(Object entity){
		if(this.entityValidator!=null){
			this.entityValidator.validate(entity);
		}
	}
	
	private void processIBaseEntity(Object entity, boolean create){
		if(!(entity instanceof TimeRecordableEntity))
			return ;
		Date now = new Date();
		TimeRecordableEntity ib = (TimeRecordableEntity) entity;
		if(create){
			ib.setCreateAt(now);
		}
		ib.setUpdateAt(now);
	}
	
	@Override
	public JdbcStatementContext<List<Object[]>> makeDelete(Object objects){
		this.throwIfQueryableOnly();
		
		JdbcStatementContextBuilder dsb = JdbcStatementContextBuilder.create(DbmEventAction.delete, this, getStaticDeleteSqlBuilder());
		
		if(LangUtils.isMultiple(objects)){
			List<Object> list = LangUtils.asList(objects);
			for(Object obj : list){
				this.addRequiredCauseValue(dsb, obj);
				dsb.addBatch();
			}
		}else{
			this.addRequiredCauseValue(dsb, objects);
			
			/*if(isIdentify){
				Object idValue = objects;
				if(isInstance(idValue)){
					idValue = getId(idValue);
				}
				dsb.addCauseValue(idValue);
			}else{
				dsb.processWhereCauseValuesFromEntity(objects);
			}*/
		}
		
		dsb.build();
//		KVEntry<String, List<Object[]>> kv = new KVEntry<String, List<Object[]>>(dsb.getSql(), dsb.getValues());
//		JdbcStatementContext<List<Object[]>> kv = SimpleJdbcStatementContext.create(dsb.getSqlBuilder(), dsb.getValue());
		return dsb;
	}
	
	private void addRequiredCauseValue(JdbcStatementContextBuilder dsb, Object entityObject) {
		/*Object idValue = getId(entityObject);
		dsb.addCauseValue(idValue);*/
		if (!isCompositePK()) {
			Object idValue = getId(entityObject);
			dsb.addCauseValue(idValue);
		} else {
			// 复合主键存在多个id的情况
			EntrySQLBuilder sqlBuilder = dsb.getSqlBuilder();
			for (DbmMappedField idField : sqlBuilder.getWhereCauseFields()) {
				if (idField.isIdentify()) {
					Object idValue = idField.getValue(entityObject);
					dsb.addCauseValue(idValue);
				}
			}
		}
		
		if (isVersionControll()) {
			Object version = getVersionValue(entityObject);
			dsb.addCauseValue(version);
		}
	}
	
	@Override
	public JdbcStatementContext<List<Object[]>> makeUpdate(Object entity){
		this.throwIfQueryableOnly();
		
		JdbcStatementContextBuilder dsb = JdbcStatementContextBuilder.create(DbmEventAction.update, this, getStaticUpdateSqlBuilder());
		
		if(LangUtils.isMultiple(entity)){
			List<Object> list = LangUtils.asList(entity);
			for(Object en : list){
				this.checkIdValue(en);
				dsb.processWhereCauseValuesFromEntity(en); // 先处理where字段值，因为如果使用了类似updateAt的字段做版本，processIBaseEntity方法会修改updateAt字段的值
				this.processIBaseEntity(en, false);
				
				this.vailidateEntity(entity);
				
//				dsb.setColumnValuesFromEntity(en)
				dsb.processColumnValues(en)
//					.processWhereCauseValuesFromEntity(en)
					.addBatch();
			}
		}else{
			this.checkIdValue(entity);
			dsb.processWhereCauseValuesFromEntity(entity); // 先处理where字段值，因为如果使用了类似updateAt的字段做版本，processIBaseEntity方法会修改updateAt字段的值
			this.processIBaseEntity(entity, false);
			
			this.vailidateEntity(entity);
//			dsb.setColumnValuesFromEntity(entity);
			dsb.processColumnValues(entity);
//			dsb.processWhereCauseValuesFromEntity(entity); 上移到processIBaseEntity之前
		}
		
		dsb.build();
//		KVEntry<String, List<Object[]>> kv = new KVEntry<String, List<Object[]>>(dsb.getSql(), dsb.getValues());
//		JdbcStatementContext<List<Object[]>> kv = SimpleJdbcStatementContext.create(dsb.getSqlBuilder(), dsb.getValue());
		return dsb;
	}
	
	protected void checkIdValue(Object entity){
		if(!hasIdentifyValue(entity))
			throw new DbmException("entity["+entity+"] id can not be null");
	}
	
	@Override
	public JdbcStatementContext<List<Object[]>> makeDymanicUpdate(Object entity) {//single entity
		this.throwIfQueryableOnly();

		this.checkIdValue(entity);
		
		// 先处理where字段值，因为如果使用了类似updateAt的字段做版本，processIBaseEntity方法会修改updateAt字段的值
		EntrySQLBuilderImpl sb = sqlBuilderFactory.createQMark(this, this.getTableInfo().getAlias(), SqlBuilderType.update);
		JdbcStatementContextBuilder dsb = JdbcStatementContextBuilder.create(DbmEventAction.update, this, sb);
		makeWhereFieldsForDymanicUpdate(dsb, entity);
		
		this.processIBaseEntity(entity, false);
		makeUpdateFieldsForDymanicUpdate(dsb, entity);
		dsb.build();
//		KVEntry<String, List<Object[]>> kv = new KVEntry<String, List<Object[]>>(dsb.getSql(), dsb.getValues());
//		JdbcStatementContext<List<Object[]>> kv = SimpleJdbcStatementContext.create(dsb);
		return dsb;
	}

	/**
	 * where字段在processIBaseEntity之前另外处理，因为如果使用了类似updateAt的字段做版本，processIBaseEntity方法会修改updateAt字段的值
	 * 
	 * @author weishao zeng
	 * @param sqlBuilder
	 * @param entity
	 * @return
	 */
	final protected JdbcStatementContextBuilder makeWhereFieldsForDymanicUpdate(JdbcStatementContextBuilder sqlBuilder, Object entity) {
		Object val = null;
		for (DbmMappedField mfield : this.mappedColumns.values()){
			val = mfield.getValue(entity);
			if (mfield.isIdentify()){
				Assert.notNull(val, "id can not be null : " + entity);
				sqlBuilder.appendWhere(mfield, val);
			} else if(mfield.isVersionControll()){
				Assert.notNull(val, "version field["+mfield.getName()+"] can not be null : " + entity);
				sqlBuilder.appendWhere(mfield, val);
				val = mfield.getVersionableType().getVersionValule(val);
			}
		}
		return sqlBuilder;
	}

	protected JdbcStatementContextBuilder makeUpdateFieldsForDymanicUpdate(JdbcStatementContextBuilder sqlBuilder, Object entity){
		/*EntrySQLBuilderImpl sb = sqlBuilderFactory.createQMark(this, this.getTableInfo().getAlias(), SqlBuilderType.update);
		JdbcStatementContextBuilder sqlBuilder = JdbcStatementContextBuilder.create(DbmEventAction.update, this, sb);*/
		Object val = null;
		for(DbmMappedField mfield : this.mappedColumns.values()){
			val = mfield.getValue(entity);
			Object newFieldValue = mfield.fireDbmEntityFieldEvents(val, DbmEventAction.update);
			if(newFieldValue!=val){
				val = newFieldValue;
				mfield.setValue(entity, val);
			}
			/*if(mfield.fireDbmEntityFieldEvents(val, DbmEventAction.update)!=val){
				mfield.setValue(entity, val);
			}*/
			if(mfield.isVersionControll()){
				// 获取新的版本值
				val = mfield.getVersionableType().getVersionValule(val);
			}
			if(val!=null)
				sqlBuilder.append(mfield, val);
			// where字段在processIBaseEntity之前另外处理，因为如果使用了类似updateAt的字段做版本，processIBaseEntity方法会修改updateAt字段的值
			/* 
			 * if(mfield.isIdentify()){
				Assert.notNull(val, "id can not be null : " + entity);
				sqlBuilder.appendWhere(mfield, val);
			}else{
				if(mfield.isVersionControll()){
					Assert.notNull(val, "version field["+mfield.getName()+"] can not be null : " + entity);
					sqlBuilder.appendWhere(mfield, val);
					val = mfield.getVersionableType().getVersionValule(val);
				}
				if(val!=null)
					sqlBuilder.append(mfield, val);
			}*/
		}
		return sqlBuilder;
	}
	
	@Override
	public Map<String, AbstractMappedField> getMappedFields() {
		return mappedFields;
	}
	

	public Map<String, AbstractMappedField> getMappedColumns() {
		return mappedColumns;
	}

	//	@Override
	public boolean isQueryableOnly() {
		return getMappedType()==MappedType.QUERYABLE_ONLY;
	}

	@Override
	public MappedType getMappedType() {
		return MappedType.ENTITY;
	}

	/*@Override
	public boolean isJoined() {
//		return getMappedType()==MappedType.JOINED;
		return false;
	}*/

	@Override
	public boolean isEntity() {
		return getMappedType()==MappedType.ENTITY;
	}

	public String getEntityName() {
		return entityName;
	}

	public AnnotationInfo getAnnotationInfo() {
		return annotationInfo;
	}

	public void freezing() {
		for(DbmMappedField field : mappedFields.values()){
			field.freezing();
		}
		freezing = true;
		if(logger.isTraceEnabled()){
			logger.trace("mapped entry["+getEntityName()+"] has built and freezing!");
		}
	}

	protected void checkFreezing(String name) {
		if(isFreezing()){
			throw new UnsupportedOperationException("the entry["+getEntityName()+"] is freezing now, don not supported this operation : " + name);
		}
	}

	public boolean isFreezing() {
		return freezing;
	}

	public SQLBuilderFactory getSqlBuilderFactory() {
		return sqlBuilderFactory;
	}

	public void setSqlBuilderFactory(SQLBuilderFactory sqlBuilderFactory) {
		this.sqlBuilderFactory = sqlBuilderFactory;
	}

	/*public DataHolder<String, Object> getDataHolder() {
		return dataHolder;
	}*/
	
	public String toString(){
		return LangUtils.append(getEntityName());
	}

	public List<DbmEntityListener> getEntityListeners() {
		return entityListeners;
	}

	public List<DbmEntityFieldListener> getFieldListeners() {
		return fieldListeners;
	}

	@Override
	public boolean isVersionControll() {
		return getVersionField()!=null;
	}

}
