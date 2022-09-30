package org.onetwo.dbm.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.onetwo.common.db.sqlext.ExtQueryListener;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.db.sqlext.SelectExtQueryImpl;
import org.onetwo.dbm.mapping.DbmMappedEntry;

public class DbmExtQueryImpl extends SelectExtQueryImpl {

	private DbmMappedEntry entry;
	
	public DbmExtQueryImpl(DbmMappedEntry entry, Class<?> entityClass, String alias, Map<?, ?> params, SQLSymbolManager symbolManager) {
		this(entry, entityClass, alias, params, symbolManager, null);
	}

	public DbmExtQueryImpl(DbmMappedEntry entry, Class<?> entityClass, String alias, Map<?, ?> params, SQLSymbolManager symbolManager, List<ExtQueryListener> listeners) {
		super(entityClass, alias, params, symbolManager, listeners);
		this.entry = entry;
		this.setQueryNameStrategy(new DbmQueryNameStrategy(entry, this.alias, this.joinMapped, true));
	}



	@Override
	public void initQuery(){
		super.initQuery();
//		super.setSqlQuery(true);
	}


	protected String getDefaultSelectFields(Class<?> entityClass, String alias){
		return alias + ".*";
	}

	protected List<String> getSelectFieldsWithExclude(List<String> unselectFields){
		return entry.getMappedFields().entrySet().stream().filter(e->{
			return !unselectFields.contains(e.getKey());
		})
		.map(e->{
			return this.getSelectFieldName(e.getValue().getName());
		})
		.collect(Collectors.toList());
	}
	
	@Override
	protected String getDefaultOrderByFieldName() {
		return "";
	}

	protected String getDefaultCountField(){
		if(entry!=null && !entry.getIdentifyFields().isEmpty()){
			return entry.getIdentifyFields().get(0).getColumn().getName();
		}
		return "*";
	}
	
	@Override
	public String getSelectFieldName(String f) {
		String fname = getFieldName(f);
		return super.getSelectFieldName(fname);
	}

	/*@SuppressWarnings("unchecked")
	protected SelectExtQueryImpl buildJoin(StringBuilder joinBuf, String joinKey, boolean hasParentheses) {
		if (!hasParams(joinKey))
			return this;
		if(!K.LEFT_JOIN.equals(joinKey))//only support left join
			return this;
		String joinWord = K.JOIN_MAP.get(joinKey);
		Object value = this.getParams().get(joinKey);
		List<Object> fjoin = MyUtils.asList(value);
		if(LangUtils.isEmpty(fjoin))
			return this;
		
		//index=0 => left join table 
		String joinTableName = fjoin.get(0).toString();
		String[] jstrs = StringUtils.split(joinTableName, ":");
		if(jstrs.length>1)//alias
			joinBuf.append(joinWord).append(hasParentheses?"(":" ").append(jstrs[0]).append(hasParentheses?") ":" ").append(jstrs[1]).append(" ");
		else
			joinBuf.append(joinWord).append(hasParentheses?"(":" ").append(joinTableName).append(hasParentheses?") ":" ");
		fjoin.remove(0);
		if(fjoin.isEmpty()){
			throw new DbmException("join table without on cause, check it!");
		}
		joinBuf.append("on (");
		for (int i=0; i<fjoin.size(); i++) {
			if(!fjoin.get(i).getClass().isArray())
				throw new DbmException("join cause must be a array");
			if(i!=0)
				joinBuf.append(" and ");
			Object[] onsArray = (Object[]) fjoin.get(i);
			joinBuf.append(getFieldName(onsArray[0].toString())).append(" = ").append(onsArray[1]);
		}
		joinBuf.append(") ");
		this.getParams().remove(joinKey);
		return this;
	}*/
	
}
