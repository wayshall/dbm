package org.onetwo.common.db;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.onetwo.common.db.filequery.JNamedQueryKey;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.Page;
import org.onetwo.dbm.dialet.DBDialect.LockInfo;
import org.slf4j.Logger;

abstract public class AbstractQueryWrapper implements QueryWrapper{
	
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	public static final int PARAMETER_START_INDEX = 0;

	
	@SuppressWarnings("rawtypes")
	public QueryWrapper setPageParameter(final Page page) {
		if(!page.isPagination())
			return this;
		return setLimited(page.getFirst()-1, page.getPageSize());
	}

	public QueryWrapper setLockInfo(LockInfo lockInfo){
		throw new UnsupportedOperationException();
	}

	public LockInfo getLockInfo() {
		return null;
	}

	@Override
	public QueryWrapper setParameters(Map<String, Object> params) {
		for(Entry<String, Object> entry : params.entrySet()){
			setParameter(entry.getKey(), entry.getValue());
		}
		return this;
	}
	

	public void setQueryAttributes(Map<Object, Object> params) {
		Object key;
		for(Entry<Object, Object> entry : params.entrySet()){
			key = entry.getKey();
			if(String.class.isInstance(key)){
				setParameter(key.toString(), entry.getValue());
			}else if(Integer.class.isInstance(key)){
				setParameter((Integer)key, entry.getValue());
			}else if(JNamedQueryKey.class.isInstance(key)){
				this.processQueryKey((JNamedQueryKey)key, entry.getValue());
			}
		}
	}
	

	protected void processQueryKey(JNamedQueryKey qkey, Object value){
	}
	
	@Override
	public QueryWrapper setParameters(List<Object> params) {
		Assert.notNull(params);
		int size = params.size();
		for(int index=0; index<size; index++){
			setParameter(index, params.get(index));
		}
		return this;
	}
	
	@Override
	public QueryWrapper setParameters(Object[] params){
		Assert.notNull(params);
		for(int index=0; index<params.length; index++){
			setParameter(index, params[index]);
		}
		return this;
	}

	@Override
	public QueryWrapper setQueryConfig(Map<Object, Object> configs) {
		return this;
	}
}
