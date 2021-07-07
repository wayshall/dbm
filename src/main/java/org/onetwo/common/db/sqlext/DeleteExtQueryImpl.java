package org.onetwo.common.db.sqlext;

import java.util.List;
import java.util.Map;

public class DeleteExtQueryImpl extends AbstractExtQuery implements DeleteExtQuery {
	
	public DeleteExtQueryImpl(Class<?> entityClass, String alias, Map<Object, Object> params,
			SQLSymbolManager symbolManager, List<ExtQueryListener> listeners) {
		super(entityClass, alias, params, symbolManager, listeners);
		this.getQueryNameStrategy().setAliasMainTableName(false);
	}

	@Override
	public ExtQuery build() {
		beforeBuild();
		
		sql = new StringBuilder();
//		sql.append("delete ").append(this.alias).append(" from ").append(this.getFromName(entityClass)).append(" ").append(this.alias).append(" ");
		sql.append("delete ").append("from ").append(this.getFromName(entityClass)).append(" ");
		
		this.buildWhere();
		if(where!=null)
			sql.append(where);
		
		if (isDebug()) {
			logger.info("generated sql : {}, params: {}", sql, this.paramsValue.getValues());
		}

		this.hasBuilt = true;
		
		afaterBuild();
		return this;
	}

}
