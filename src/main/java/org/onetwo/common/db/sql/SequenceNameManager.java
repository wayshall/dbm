package org.onetwo.common.db.sql;

import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.DbmException;

import com.google.common.collect.Maps;

/***
 * 序列管理类
 * @author weishao
 *
 */
public class SequenceNameManager {
	
	
	public static final String SEQ_PREFIX = "SEQ_";
	public static final String CREATE_SEQUENCE = "create sequence ${sequenceName} start with ${initialValue} increment by 1 maxvalue 99999999999";
	
//	public static final String CREATE_SEQUENCE = " connect by rownum <= ?";

	/*private static SequenceNameManager instance = new SequenceNameManager();
	
	public static SequenceNameManager getInstance() {
		return instance;
	}*/

	private Map<SeqKey, String> sequenceSqlCache = Maps.newConcurrentMap();
	
	/*private SequenceNameManager(){
	}*/
	
	public <T> String getSequenceName(Class<T> entityClass){
		String seqName = SEQ_PREFIX + entityClass.getSimpleName().toUpperCase();
		if(StringUtils.isBlank(seqName))
			throw new DbmException("can not find the sequence. class["+entityClass.getName()+"]");
		
		return seqName;
	}
	
	public <T> String getSequenceSql(Class<T> entityClass){
		String seqName = getSequenceName(entityClass);
		return getSequenceSql(seqName, null);
	}
	
	public <T> String getSequenceSql(String seqName){
		return getSequenceSql(seqName, null);
	}
	public <T> String getSequenceSql(String seqName, Integer batchSize){
		SeqKey key = new SeqKey(seqName, batchSize);
		String cacheSql = sequenceSqlCache.get(key);
		if(StringUtils.isNotBlank(cacheSql)){
			return cacheSql;
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("select ").append(seqName).append(".nextval from dual");
		if(batchSize!=null){
			sql.append(" connect by rownum <= ?");
		}
		sequenceSqlCache.put(key, sql.toString());
		
		return sql.toString();
	}
	

	public String getCreateSequence(Class<?> entityClass){
		return getCreateSequence(getSequenceName(entityClass), 1);
	}
	
	public String getCreateSequence(String sequenceName){
		return getCreateSequence(sequenceName, 1);
	}
	
	public String getCreateSequence(String sequenceName, int initialValue){
		String name = sequenceName;
//		String sql = sqlFile.getVariable(CREATE_SEQUENCE_SQL);
		String sql = StrSubstitutor.replace(CREATE_SEQUENCE, CUtils.asMap("sequenceName", sequenceName, "initialValue", initialValue));
		if(StringUtils.isBlank(sql))
			throw new DbmException("sql is blank . can not create squence : " + name);
		sql = sql.replace(":sequenceName", name);
		return sql;
	}
	
	private static class SeqKey {
		final private String seqName;
		final private Integer batchSize;
		public SeqKey(String seqName, Integer batchSize) {
			super();
			this.seqName = seqName;
			this.batchSize = batchSize;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((batchSize == null) ? 0 : batchSize.hashCode());
			result = prime * result
					+ ((seqName == null) ? 0 : seqName.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SeqKey other = (SeqKey) obj;
			if (batchSize == null) {
				if (other.batchSize != null)
					return false;
			} else if (!batchSize.equals(other.batchSize))
				return false;
			if (seqName == null) {
				if (other.seqName != null)
					return false;
			} else if (!seqName.equals(other.seqName))
				return false;
			return true;
		}
		
	}
}
