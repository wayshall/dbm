package org.onetwo.common.db.builder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.onetwo.common.db.InnerBaseEntityManager;
import org.onetwo.common.db.RawSqlWrapper;
import org.onetwo.common.db.sqlext.ExtQuery;
import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.dialet.DBDialect.LockInfo;
import org.onetwo.dbm.exception.DbmException;

/*********
 * 提供简易有明确api的查询构造器
 * 
 * @author wayshall
 *
 */
public class QueryBuilderImpl<E> implements QueryBuilder<E> {
	
	public static class SubQueryBuilder<SE> extends QueryBuilderImpl<SE> {

		public SubQueryBuilder() {
			super();
		}

	}

	/*public static QueryBuilderImpl where(){
		QueryBuilderImpl q = new QueryBuilderImpl(null);
		return q;
	}*/

	/*public static QueryBuilder<QueryBuilderImpl> from(Class<?> entityClass){
		return QueryBuilderCreator.from(entityClass);
	}*/

	/*public static <SE> SubQueryBuilder<SE> sub(){
		SubQueryBuilder<SE> q = new SubQueryBuilder<SE>();
		return q;
	}*/

	protected InnerBaseEntityManager baseEntityManager;
	protected String alias;
	protected Map<Object, Object> params = new LinkedHashMap<Object, Object>();
	protected Class<?> entityClass;
	protected List<QueryBuilderJoin> leftJoins = LangUtils.newArrayList();
//	private SQLSymbolManager sqlSymbolManager = SQLSymbolManagerFactory.getInstance().getJdbc();
//	private ExtQuery extQuery;
	
//	private List<SQField> fields = new ArrayList<SQField>();
//	private ExtQuery extQuery;
	
	protected QueryBuilderImpl(){
	}

	protected QueryBuilderImpl(InnerBaseEntityManager baseEntityManager, Class<?> entityClass){
		if (entityClass==null) {
			throw new DbmException("entity class can not be null");
		}
		this.entityClass = entityClass;
		this.alias = StringUtils.uncapitalize(entityClass.getSimpleName());
		this.baseEntityManager = baseEntityManager;
	}
	
	
	
	InnerBaseEntityManager getBaseEntityManager() {
		return baseEntityManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T as(Class<T> queryClass){
		return (T) this;
	}
	@Override
	public Class<?> getEntityClass() {
		return entityClass;
	}
	
	protected QueryBuilderImpl<E> self(){
		return (QueryBuilderImpl<E>)this;
	}
	
	public WhereCauseBuilder<E> where(){
		return new DefaultWhereCauseBuilder<>(this);
	}

	/*@Override
	public QueryBuilderImpl debug(){
		this.params.put(K.DEBUG, true);
		return self();
	}
	
	@Override
	public QueryBuilderImpl or(QueryBuilder subQuery){
		this.checkSubQuery(subQuery);
		this.params.put(K.OR, subQuery.getParams());
		return self();
	}*/
	
	protected void checkSubQuery(QueryBuilder<E> subQuery){
		if(!(subQuery instanceof SubQueryBuilder)){
			LangUtils.throwBaseException("please use SQuery.sub() method to create sub query .");
		}
	}
	

	@Override
	public QueryBuilder<E> lock(LockInfo lock) {
		this.params.put(K.FOR_UPDATE, lock);
		return self();
	}

	@Override
	public QueryBuilderImpl<E> select(String...fields){
		this.params.put(K.SELECT, fields);
		return self();
	}
	@Override
	public QueryBuilderImpl<E> unselect(String...fields){
		this.params.put(K.UNSELECT, fields);
		return self();
	}
	
	/***
	 * @param first from 0
	 */
	@Override
	public QueryBuilderImpl<E> limit(int first, int size){
		this.params.put(K.FIRST_RESULT, first);
		this.params.put(K.MAX_RESULTS, size);
		return self();
	}
	
	@Override
	public QueryBuilderImpl<E> asc(String...fields){
		this.params.put(K.ASC, fields);
		return self();
	}
	
	@Override
	public QueryBuilderImpl<E> ascRand(Object seed){
		this.params.put(K.ASC, K.RAND.withkey(seed));
		return self();
	}
	
	@Override
	public QueryBuilderImpl<E> descRand(Object seed){
		this.params.put(K.DESC, K.RAND.withkey(seed));
		return self();
	}
	
	@Override
	public QueryBuilderImpl<E> desc(String...fields){
		this.params.put(K.DESC, fields);
		return self();
	}
	
	@Override
	public QueryBuilderImpl<E> distinct(String...fields){
		this.params.put(K.DISTINCT, fields);
		return self();
	}

	/*@Override
	public QueryBuilderImpl addField(QueryBuilderField field){
		this.params.put(field.getOPFields(), field.getValues());
		return self();
	}*/

	@Override
	public QueryBuilderJoin leftJoin(String table, String alias){
		QueryBuilderJoin join = new QueryBuilderJoin(this, table, alias);
		leftJoins.add(join);
		return join;
	}

	@Override
	public Map<Object, Object> getParams() {
		return params;
	}
	
	protected SQLSymbolManager getSQLSymbolManager(){
		InnerBaseEntityManager em = (InnerBaseEntityManager) baseEntityManager;
		return em.getSQLSymbolManager();
		/*SQLSymbolManager symbolManager = SQLSymbolManagerFactory.getInstance().getJdbc();
		return symbolManager;*/
	}
	
	protected String buildLeftJoin(){
		if(LangUtils.isEmpty(leftJoins))
			return "";
		StringBuilder leftJoinSql = new StringBuilder();
		int index = 0;
		for(QueryBuilderJoin join : leftJoins){
			if(index!=0)
				leftJoinSql.append(" ");
			leftJoinSql.append("left join ").append(join.toSql());
			index++;
		}
		return leftJoinSql.toString();
	}

	@Override
	public QueryAction<E> toQuery(){
		return createQueryAction();
	}

	@Override
	public QueryAction<E> toSelect(){
		return createQueryAction();
	}
	
	@Override
	public ExecuteAction toExecute() {
		ExecuteAction executeAction = new ExecuteActionImpl(this);
		return executeAction;
	}

//	public int delete(){
//		InnerBaseEntityManager em = (InnerBaseEntityManager) baseEntityManager;
//		ExtQueryInner query = em.getSQLSymbolManager().createDeleteQuery(entityClass, params);
//		ExtQuery q = query.build();
//		return em.createQuery(q.getSql(), q.getParamsValue().asMap()).executeUpdate();
//	}
	
	/*public ParamValues getParamValues(){
		return extQuery.getParamsValue();
	}
	
	public String getSql(){
		return extQuery.getSql();
	}*/
	
	protected QueryAction<E> createQueryAction(){
		String leftJoinSql = buildLeftJoin();
		if(StringUtils.isNotBlank(leftJoinSql)){
			params.put(K.SQL_JOIN, RawSqlWrapper.wrap(leftJoinSql));
		}
		/*ExtQuery extQuery = null;//new ExtQueryImpl(entityClass, null, params, getSQLSymbolManager());
		extQuery = createExtQuery(entityClass, alias, params);
		extQuery.build();*/
		
//		QueryActionImpl<E> queryAction = new QueryActionImpl<E>(this, entityClass, alias, params);
		QueryActionImpl<E> queryAction = new QueryActionImpl<E>(this);
		
		/*JFishQueryValue qv = JFishQueryValue.create(getSQLSymbolManager().getPlaceHolder(), extQuery.getSql());
		qv.setResultClass(extQuery.getEntityClass());
		if(extQuery.getParamsValue().isList()){
			qv.setValue(extQuery.getParamsValue().asList());
		}else{
			qv.setValue(extQuery.getParamsValue().asMap());
		}*/
		
		return queryAction;
	}
	
	protected ExtQuery createExtQuery(Class<?> entityClass, String alias, Map<Object, Object> properties){
		return getSQLSymbolManager().createSelectQuery(entityClass, alias, properties);
	}

	public String getAlias() {
		return alias;
	}

	/*protected void throwIfHasBuild(){
		if(extQuery!=null){
			throw new UnsupportedOperationException("query has build!");
		}
	}*/

	/*public ExtQuery getExtQuery() {
		throwIfHasNotBuild();
		return extQuery;
	}

	protected void throwIfHasNotBuild(){
		if(extQuery==null){
			throw new UnsupportedOperationException("query has not build!");
		}
	}*/
	
}
