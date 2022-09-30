package org.onetwo.common.db.sqlext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.onetwo.common.db.RawSqlWrapper;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.dialet.DBDialect.LockInfo;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SelectExtQueryImpl extends AbstractExtQuery implements SelectExtQuery {

	private final static int AT_LEAST_RESULTS_SIZE = 1;
	private final static int MIN_RESULT_INDEX = 0;
//	private final static int INVALID_LEAST_RESULTS = 0;
	

	protected StringBuilder select;
	protected StringBuilder join;
	protected StringBuilder orderBy;
	
	protected Integer firstResult = 0; 
	protected Integer maxResults = -1;
	
	protected boolean subQuery;

	private String countValue;
	
	private Map<Object, Object> queryConfig;
	
	/****
	 * 是否缓存查询对象，避免重复解释，暂时没实现
	 */
	private boolean cacheable;

//	protected StringBuilder couontSelect;
	protected StringBuilder couontJoin;
	
	private LockInfo lockInfo;
	protected Map<String, String> joinMapped = new HashMap<>();

	public SelectExtQueryImpl(Class<?> entityClass, String alias, Map<?, ?> params, SQLSymbolManager symbolManager) {
		super(entityClass, alias, params, symbolManager);
//		this.queryNameStrategy = new SelectQueryNameStrategy(this.alias, joinMapped, true);
		this.setQueryNameStrategy(new SelectQueryNameStrategy(this.alias, joinMapped, true));
	}
	
	public SelectExtQueryImpl(Class<?> entityClass, String alias, Map<?, ?> params, SQLSymbolManager symbolManager, List<ExtQueryListener> listeners) {
		super(entityClass, alias, params, symbolManager, listeners);
//		this.queryNameStrategy = new SelectQueryNameStrategy(this.alias, joinMapped, true);
		this.setQueryNameStrategy(new SelectQueryNameStrategy(this.alias, joinMapped, true));
	}


	public void initParams(){
//		this.hasBuilt = false;
		super.initParams();
//		this.queryNameStrategy = new SelectQueryNameStrategy(this.alias, joinMapped, true);
		
		this.firstResult = getValueAndRemoveKeyFromParams(K.FIRST_RESULT, firstResult);
		this.maxResults = getValueAndRemoveKeyFromParams(K.MAX_RESULTS, maxResults);
		this.countValue = getValueAndRemoveKeyFromParams(K.COUNT, countValue);
//		this.cacheable = getValueAndRemoveKeyFromParams(K.CACHEABLE, cacheable);
		this.lockInfo = (LockInfo)this.params.remove(K.FOR_UPDATE);
		
		//query config
		Object qc = getValueAndRemoveKeyFromParams(K.QUERY_CONFIG, queryConfig);
		if(qc instanceof Object[]){
			this.queryConfig = CUtils.asMap((Object[])qc);
		}else{
			this.queryConfig = (Map)qc;
		}
		
//		super.initQuery();
	}
	
	
	public boolean needSetRange(){
		return this.firstResult>=MIN_RESULT_INDEX && this.maxResults>=AT_LEAST_RESULTS_SIZE;
	}

	/***
	 *  from 0
	 */
	public Integer getFirstResult() {
		return firstResult;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}



	public ExtQuery build() {
		/*String fname = "build ext query";
		if(isDebug())
			UtilTimerStack.push(fname);*/
		/*if(hasBuilt()){
			return this;
		}*/
		
		beforeBuild();
		
		this.buildSelect().buildJoin().buildOrderBy();
		//build befor where
//		String lockSql = buildLockString();

		
		sql = new StringBuilder();
		sql.append(select);
		if (join != null)
			sql.append(join);
		
		if (!params.isEmpty()) {
			this.buildWhere();
			if(where!=null)
				sql.append(where);
		}
		
		if (orderBy != null){
			sql.append(orderBy);
		}
		/*if(StringUtils.isNotBlank(lockSql)){
			sql.append(lockSql);
		}*/

		if (isDebug()) {
			logger.info("generated sql : {}, params: {}", sql, this.paramsValue.getValues());
		}

		/*if(isDebug())
			UtilTimerStack.pop(fname);*/
		
		this.hasBuilt = true;
		
		afaterBuild();
		return this;
	}
	
	/*protected String buildLockString(){
		LockInfo lockInfo = (LockInfo)this.params.remove(K.FOR_UPDATE);
		if(lockInfo==null){
			return null;
		}
		return this.symbolManager.getSqlDialet().getLockSqlString(lockInfo);
	}*/

	protected SelectExtQueryImpl buildSelect() {
		select = new StringBuilder();
		
		if(getParams().containsKey(K.SQL_SELECT)){
			Object rawSqlObj = getParams().remove(K.SQL_SELECT);
			if(rawSqlObj==null)
				return this;
			if(!(rawSqlObj instanceof RawSqlWrapper)){
				LangUtils.throwBaseException("it must be a sqlwrapper : " + rawSqlObj);
			}
			RawSqlWrapper wrapper = (RawSqlWrapper)rawSqlObj;
			if(!wrapper.isBlank()){
				select.append(wrapper.getRawSql());
			}
//			params.remove(K.SQL_SELECT);
			return this;
		}

		Object selectValule = null;
		select.append("select ");
		if(hasParams(K.DISTINCT)){
			select.append("distinct ");
		}

		Object selectKey = K.SELECT;
		selectValule = params.remove(selectKey);
		if(selectValule==null){
			selectKey = K.DISTINCT;
			selectValule = params.remove(selectKey);
		}else{
			//依然移除distinct，防止select和district都写的情况
			params.remove(K.DISTINCT);
		}
		
		if (selectValule != null) {
//			params.remove(selectKey);
			Object[] selectList = null;
			if(selectValule instanceof String){
				selectList = StringUtils.split(selectValule.toString(), ",");
			}else if(selectValule.getClass().isArray()){
				selectList = (Object[])selectValule;
			}else{
				selectList = (Object[])LangUtils.asList(selectValule).toArray();
			}
			
			int fieldStartIndex = 0;
			if(selectList[0] instanceof Class){
				//仅用于 hql 写法支持
				Class returnClass = (Class) selectList[0];
				fieldStartIndex = 1;
				if(Map.class.isAssignableFrom(returnClass)){
					select.append("new map(");
				}else if(List.class.isAssignableFrom(returnClass)){
					select.append("new list(");
				}else{
//					throw new BaseException("unsupported class : " + selectList[0]);
					select.append("new ").append(returnClass.getName()).append("(");
				}
			}
			for (int i = fieldStartIndex; i < selectList.length; i++) {
				if (i != fieldStartIndex)
					select.append(", ");

				String[] sfield = StringUtils.split(selectList[i].toString(), ":");
				if(sfield.length==1){
					select.append(getSelectFieldName(sfield[0]));
				}else if(sfield.length==2){
					select.append(getSelectFieldName(sfield[0])).append(" as ").append(sfield[1]);
				}else{
					throw new BaseException("error select field: " + selectList[i]);
				}
				/*if(this.alias.equals(selectList[i]))
					select.append(selectList[i]);
				else
					select.append(getFieldName(selectList[i].toString()));*/
			}
			if(fieldStartIndex!=0)
				select.append(")");
			select.append(" ");
		} else if(StringUtils.isNotBlank(countValue)){
			select.append("count(").append(countValue).append(") ");
			
		} else if(params.containsKey(K.UNSELECT)){
			List<String> unselectFields = CUtils.tolist(params.remove(K.UNSELECT), true);
			List<String> fields = getSelectFieldsWithExclude(unselectFields);
			String str = org.apache.commons.lang3.StringUtils.join(fields, ", ");
			select.append(str).append(" ");
			
		}else {
			select.append(getDefaultSelectFields(entityClass, this.alias)).append(" ");
		}
		
		select.append("from ").append(getFromName(entityClass)).append(" ").append(this.alias).append(" ");
		return this;
	}
	
	protected List<String> getSelectFieldsWithExclude(List<String> unselectFields){
		throw new UnsupportedOperationException();
	}

	
	public String getSelectFieldName(String f) {
		if(this.alias.equals(f)){
			return f;
		}else{
			return this.getQueryNameStrategy().appendAlias(this.getQueryNameStrategy().translateAt(f));
		}
	}
	
	
	protected String getDefaultSelectFields(Class<?> entityClass, String alias){
		return alias;
	}

	protected SelectExtQueryImpl buildJoin() {
		join = new StringBuilder();
		//for count
		couontJoin = new StringBuilder();
		/*buildJoin(join, K.JOIN_FETCH, false);//inner
		buildJoin(join, K.FETCH, false);
		buildJoin(join, K.JOIN, false);
		buildJoin(join, K.LEFT_JOIN, false);//outer
		buildJoin(join, K.JOIN_IN, true);*/
		
		for(Object key : K.JOIN_MAP.keySet()){
			if (!hasParams(key))
				continue;

			Object value = this.getParams().get(key);
			if(K.JOIN_IN.equals(key)){
				buildJoin(join, key, value, true);
				
				//for count
				buildJoin(couontJoin, key, value, true);
				
			}else if(K.SQL_JOIN.equals(key)){
				Object rawSqlObj = this.getParams().get(key);
				if(rawSqlObj==null)
					return this;
				if(!(rawSqlObj instanceof RawSqlWrapper))
					LangUtils.throwBaseException("it must a sql wrapper : " + rawSqlObj);
				RawSqlWrapper wrap = (RawSqlWrapper) rawSqlObj;
				if(!wrap.isBlank()){
					join.append(wrap.getRawSql()).append(" ");

					//for count
					couontJoin.append(wrap.getRawSql()).append(" ");
				}
				getParams().remove(K.SQL_JOIN);
			}else{
				buildJoin(join, key, value, false);

				//for count
				if(K.FETCH.equals(key) || K.LEFT_JOIN_FETCH.equals(key)){
					buildJoin(couontJoin, K.LEFT_JOIN, value, false);
				}else if(K.JOIN_FETCH.equals(key)){
					buildJoin(couontJoin, K.JOIN, value, false);
				}else{
					buildJoin(couontJoin, key, value, false);
				}
			}
			
			this.getParams().remove(key);
		}
		return this;
	}

	
	protected SelectExtQueryImpl buildJoin(StringBuilder joinBuf, Object joinKey, Object value, boolean hasParentheses) {
		String joinWord = K.JOIN_MAP.get(joinKey);
		List<String> fjoin = LangUtils.asList(value);
		if(fjoin==null)
			return this;
		
		// int index = 0;
		boolean hasComma = K.JOIN_IN.equals(joinKey);
		for (Object obj : fjoin) {
			if(obj instanceof String){
				String joinString = obj.toString();
				String[] jstrs = StringUtils.split(joinString, ":");
				if(hasComma){
					joinBuf.append(", ");
				}
				if(jstrs.length>1){//alias
					joinMapped.put(jstrs[1], jstrs[0]);
					joinBuf.append(joinWord).append(hasParentheses?"(":" ").append(getQueryNameStrategy().getJoinFieldName(jstrs[0])).append(hasParentheses?") ":" ").append(jstrs[1]).append(" ");
				}else{
					joinMapped.put(joinString, joinString);
					joinBuf.append(joinWord).append(hasParentheses?"(":" ").append(getQueryNameStrategy().getJoinFieldName(joinString)).append(hasParentheses?") ":" ");
				}
			}else if(obj.getClass().isArray()){
				joinBuf.append("on ( ");
				Map<Object, Object>  onCauses = CUtils.asLinkedMap((Object[])obj);
				int index = 0;
				for(Entry<Object, Object> cause : onCauses.entrySet()){
					if(index!=0)
						joinBuf.append("and ");
					joinBuf.append(getFieldNameIfNecessary(cause.getKey()))
								.append("=")
								.append(getFieldNameIfNecessary(cause.getValue()))
								.append(" ");
					index++;
				}
				joinBuf.append(") ");
			}
		}
		return this;
	}
	
	
	public String getFieldNameIfNecessary(Object field) {
		Assert.notNull(field);
		String f = field.toString();
		if(f.indexOf('.')!=-1)
			return f;
		return getFieldName(f);
	}
	
	
	

	protected ExtQuery buildOrderBy() {
		orderBy = new StringBuilder();
		/*boolean hasAsc = buildOrderby0(K.ASC);
		boolean hasDes = buildOrderby0(K.DESC);*/
		
		boolean hasOrderBy = false;
		List<Object> orderbys = new ArrayList<>(3);
		for(Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>)this.params.entrySet()){
			if(K.ORDER_BY_MAP.containsKey(entry.getKey())){
				orderbys.add(entry.getKey());
			}
		}
		for(Object order : orderbys){
			if(buildOrderby0(order))
				hasOrderBy = true;
		}
		
		if (!hasOrderBy) {
			this.buildDefaultOrderBy();
		}
		 
		return this;
	}
	
	protected void buildDefaultOrderBy(){
		if (!subQuery) {
			String sortField = getDefaultOrderByFieldName();
			if(StringUtils.isNotBlank(sortField))
				orderBy.append("order by ").append(getFieldName(sortField)).append(" desc "); 
		}
	}
	
	protected String getDefaultOrderByFieldName(){
//		return "id";
		return "";
	}
	
	protected boolean buildOrderby0(Object orderKeyword){
		boolean hasOrderBy = false;
		if(!params.keySet().contains(orderKeyword))
			return false;
		
		Object ascValue = params.remove(orderKeyword);
		if(ascValue==null)
			return false;
		
		Object orderValue = K.getMappedValue(orderKeyword);
		Object[] orderList = null;
		if(LangUtils.isMultiple(ascValue))
			orderList = (Object[]) LangUtils.asList(ascValue).toArray();
		else
			orderList = StringUtils.split(ascValue.toString(), ",");
		String orderField = null;

		if(orderBy==null || orderBy.length()<1){
			orderBy.append("order by ");
		}else{
			orderBy.append(", ");
		}
		for (int i = 0; i < orderList.length; i++) {
			orderField = orderList[i].toString().trim();
			if (i == 0) {
				hasOrderBy = true;
			} else{
				orderBy.append(", ");
			}
			int oIndex = orderField.indexOf(':');
			if(oIndex==-1){
				if(K.ORDERBY.equals(orderKeyword))
					orderBy.append(orderField).append(orderValue);
				else
					orderBy.append(getFieldName(orderField)).append(orderValue);
			}else{
				String f = orderField.substring(0, oIndex);
				String nullsOrder = orderField.substring(oIndex+1);
				nullsOrder = symbolManager.getSqlDialet().getNullsOrderby(nullsOrder);
				orderBy.append(getFieldName(f)).append(" ").append(nullsOrder).append(" ").append(orderValue);
			}
		}
		return hasOrderBy;
	}

	public StringBuilder getSelect() {
		return select;
	}


	public StringBuilder getOrderBy() {
		return orderBy;
	}
	

	protected String buildCountSql(String sql){
		String hql = sql;
		String countField = getDefaultCountField();

		if(hql.indexOf("{")!=-1 || hql.indexOf("}")!=-1)
			hql = hql.replace("{", "").replace("}", "");
		
		if(hql.indexOf(" group by ")!=-1){
//			int index = countField.lastIndexOf('.');
//			countField = countField.substring(index+1);
			if(StringUtils.isNotBlank(countValue)){
				countField = countValue;
			}else{
				countField = "count_entity." + countField;
			}
			hql = "select count("+countField+") from (" + hql + ") count_entity ";
		}else{
			hql = StringUtils.substringAfter(hql, "from ");
			hql = StringUtils.substringBefore(hql, " order by ");

			if(StringUtils.isNotBlank(countValue))
				countField = countValue;
			
			hql = "select count(" + countField + ") from " + hql;
			
			/*if(StringUtils.isNotBlank(countValue))
				countField = countValue;
			hql = ExtQueryUtils.buildCountSql(hql, countField);*/
		}
		return hql;
	}
	
	protected String getDefaultCountField(){
		return getFieldName("id");
	}

	public String getCountSql() {
//		String countSql = MyUtils.getCountSql(sql.toString(), getFieldName("id"));
//		String countSql = sql.toString();
//		countSql = buildCountSql(countSql);
		
		StringBuilder sql = new StringBuilder();
		sql.append(select);
		if (couontJoin != null)
			sql.append(couontJoin);
		
		if (!params.isEmpty()) {
			if(where!=null)
				sql.append(where);
		}
		
		String countSql = buildCountSql(sql.toString());
		if (isDebug()) {
			/*logger.trace("generated count sql : " + countSql);
			logger.trace("params : " + (Map) this.paramsValue.getValues());*/
			logger.info("generated countSql : {}, params: {}", countSql, this.paramsValue.getValues());
		}
		return countSql;
	}

	public boolean isSubQuery() {
		return subQuery;
	}

	public void setSubQuery(boolean subQuery) {
		this.subQuery = subQuery;
	}

	public Map<Object, Object> getQueryConfig() {
		if(queryConfig==null){
			return Collections.EMPTY_MAP;
		}
		return queryConfig;
	}


	@Override
	public boolean isCacheable() {
		return cacheable;
	}

	public LockInfo getLockInfo() {
		return lockInfo;
	}

}
