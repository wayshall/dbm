package org.onetwo.common.db.builder;

import javax.sql.DataSource;

import org.onetwo.common.db.InnerBaseEntityManager;
import org.onetwo.common.db.builder.QueryBuilderImpl.SubQueryBuilder;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.dbm.utils.Dbms;

/****
 * alias for QueryBuilderFactory 
 * @author way
 *
 */
final public class Querys {

	public static <T> QueryBuilder<T> from(BaseEntityManager baseEntityManager, Class<T> entityClass){
		QueryBuilderImpl<T> q = new QueryBuilderImpl<>(baseEntityManager==null?null:baseEntityManager.narrowAs(InnerBaseEntityManager.class), entityClass);
		return q;
	}

	public static <T> QueryBuilder<T>  from(Class<T> entityClass){
		return from(Dbms.obtainBaseEntityManager(), entityClass);
	}

	public static <T> QueryBuilder<T>  from(DataSource dataSource, Class<T> entityClass){
		return from(Dbms.obtainBaseEntityManager(dataSource), entityClass);
	}

	public static <T> SubQueryBuilder<T> subQuery(){
		SubQueryBuilder<T> q = new SubQueryBuilder<T>();
		return q;
	}

	private Querys(){
	}
}
