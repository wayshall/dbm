package org.onetwo.common.db;

import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.onetwo.common.db.filter.DataQueryParamaterEnhancer;
import org.onetwo.common.db.filter.IDataQueryParamterEnhancer;
import org.onetwo.common.db.filter.annotation.DataQueryFilter;
import org.onetwo.common.db.sqlext.ExtQuery;

import com.google.common.collect.ImmutableMap;

@Entity
public class Magazine {
	
	@Table(name="t_magazine")
	public static class MagazineWithTable {
		
	}
	

	
	@Table(name="t_user")
	@DataQueryFilter(fields = {"status:!="}, values= {"DISABLED"})
	@DataQueryParamaterEnhancer(TestDataQueryParamterEnhancer.class)
	public static class EntityWithDataFilter {
		
	}

	public static class TestDataQueryParamterEnhancer implements IDataQueryParamterEnhancer {

		@Override
		public Map<Object, Object> enhanceParameters(ExtQuery query) {
			return ImmutableMap.of("testField", new Object[] {1, 2});
		}
		
	}
}
