package org.onetwo.common.dbm.model.entity;

import java.util.Map;

import javax.persistence.MappedSuperclass;

import org.onetwo.common.db.filter.DataQueryParamaterEnhancer;
import org.onetwo.common.db.filter.IDataQueryParamterEnhancer;
import org.onetwo.common.db.sqlext.ExtQuery;
import org.onetwo.common.dbm.model.entity.TenentBaseEntity.TenentQueryParamterEnhancer;
import org.onetwo.dbm.annotation.DbmFieldListeners;
import org.onetwo.dbm.mapping.DbmEntityFieldListener;
import org.onetwo.dbm.mapping.DbmMappedField;

import com.google.common.collect.ImmutableMap;

/**
 * @author wayshall
 * <br/>
 */
@MappedSuperclass
@DataQueryParamaterEnhancer(TenentQueryParamterEnhancer.class)
public class TenentBaseEntity {

	public static Long FIXED_TENENT_ID = 1000L;
	public static String FIXED_CLIENT_ID = "clientId_1000";
	
	@DbmFieldListeners(FixedListener.class)
	private Long tenentId;
	@DbmFieldListeners(FixedListener.class)
	private String clientId;
	
	public Long getTenentId() {
		return tenentId;
	}
	public void setTenentId(Long tenentId) {
		this.tenentId = tenentId;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public static class FixedListener implements DbmEntityFieldListener {

		@Override
		public Object beforeFieldInsert(DbmMappedField field, Object fieldValue) {
			if("tenentId".equals(field.getName())){
				return FIXED_TENENT_ID;
			}else{
				return FIXED_CLIENT_ID;
			}
		}

		@Override
		public Object beforeFieldUpdate(DbmMappedField field, Object fieldValue) {
			if("tenentId".equals(field.getName())){
				return FIXED_TENENT_ID;
			}else{
				return FIXED_CLIENT_ID;
			}
		}
		
	}
	
	public static class TenentQueryParamterEnhancer implements IDataQueryParamterEnhancer {

		@Override
		public Map<Object, Object> enhanceParameters(ExtQuery query) {
			return ImmutableMap.of("tenentId", FIXED_TENENT_ID, "clientId", FIXED_CLIENT_ID);
		}
		

	}

}
