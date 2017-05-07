package org.onetwo.common.db;

import java.util.List;

import org.onetwo.common.utils.LangUtils;

public class QueryConfigData {
	
	private List<String> likeQueryFields;
//	private final boolean statful;
	private QueryContextVariable[] variables;
	
	final private String query;
	final private String countQuery;

	public QueryConfigData(String query, String countQuery) {
		super();
		this.query = query;
		this.countQuery = countQuery;
	}

	public boolean isLikeQueryField(String name){
		if(LangUtils.isEmpty(likeQueryFields))
			return false;
		return likeQueryFields.contains(name);
	}

	public void setLikeQueryFields(List<String> likeQueryFields) {
		this.likeQueryFields = likeQueryFields;
	}

	/*public boolean isStatful() {
		return statful;
	}*/

	public QueryContextVariable[] getVariables() {
		return variables;
	}

	public void setVariables(QueryContextVariable... variable) {
		this.variables = variable;
	}

	public String getQuery() {
		return query;
	}

	public String getCountQuery() {
		return countQuery;
	}

}
