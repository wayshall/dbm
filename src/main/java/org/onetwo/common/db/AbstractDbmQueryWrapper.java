package org.onetwo.common.db;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.Page;
import org.slf4j.Logger;

abstract public class AbstractDbmQueryWrapper implements DbmQueryWrapper{
	
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	public static final int PARAMETER_START_INDEX = 0;

	
	@SuppressWarnings("rawtypes")
	public DbmQueryWrapper setPageParameter(final Page page) {
		if(!page.isPagination())
			return this;
		return setLimited(page.getFirst()-1, page.getPageSize());
	}
	

	@Override
	public DbmQueryWrapper setParameters(Map<String, Object> params) {
		for(Entry<String, Object> entry : params.entrySet()){
			setParameter(entry.getKey(), entry.getValue());
		}
		return this;
	}
	

	@Override
	public DbmQueryWrapper setParameters(List<Object> params) {
		Assert.notNull(params);
		int size = params.size();
		for(int index=0; index<size; index++){
			setParameter(index, params.get(index));
		}
		return this;
	}
	
	@Override
	public DbmQueryWrapper setParameters(Object[] params){
		Assert.notNull(params);
		for(int index=0; index<params.length; index++){
			setParameter(index, params[index]);
		}
		return this;
	}

}
