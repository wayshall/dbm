package org.onetwo.dbm.id;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.onetwo.common.date.DateUtils;
import org.onetwo.common.date.NiceDate;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.expr.Expression;
import org.onetwo.common.expr.ExpressionFacotry;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.core.spi.DbmSessionImplementor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 利用数据库表生成id
 * @author weishao zeng
 * <br/>
 */
public class TableIdGeneratorManager {

	private Cache<String, TableIdGenerator> idGeneratorCaches = CacheBuilder.newBuilder()
																		.build();
	private BaseEntityManager baseEntityManager;
	private Expression parser = ExpressionFacotry.BRACE;
	private String cachePrefix = "TableIdGen";
	private int allocationSize = 30;
	
	public TableIdGeneratorManager(BaseEntityManager baseEntityManager) {
		this.baseEntityManager = baseEntityManager;
	}
	
	public TableIdGeneratorManager(BaseEntityManager baseEntityManager, int allocationSize) {
		this.allocationSize = allocationSize;
		this.baseEntityManager = baseEntityManager;
	}
	
	public TableIdGeneratorManager(BaseEntityManager baseEntityManager, String cachePrefix, int allocationSize) {
		this.baseEntityManager = baseEntityManager;
		this.cachePrefix = cachePrefix;
		this.allocationSize = allocationSize;
	}

	public Long nextId(String idGeneratorName) {
		try {
			Long genId = idGeneratorCaches.get(idGeneratorName, () -> {
				String name = cacheKey(idGeneratorName);
				TableGeneratorAttrs attr = new TableGeneratorAttrs(name, name, allocationSize);
				TableIdGenerator idGen = new TableIdGenerator(attr);
				return idGen;
			}).generate((DbmSessionImplementor)baseEntityManager.getSessionFactory().getSession());
			return genId;
		} catch (ExecutionException e) {
			throw new BaseException("create TableIdGenerator error for order no: " + e);
		}
	}
	
	public String nextId(String idGeneratorName, String idExprTemplate, Object... vars) {
		if (StringUtils.isBlank(idExprTemplate)) {
			throw new BaseException("idExprTemplate can not be blank");
		}
		Long genId = nextId(idGeneratorName);
		Map<String, Object> varCtx = CUtils.asMap("date", NiceDate.New().format(DateUtils.DATEONLY),
												 "idGeneratorName", idGeneratorName,
												 "genId", genId);
		if (!LangUtils.isEmpty(vars)) {
			Map<String, Object> addVars = CUtils.asMap(vars);
			varCtx.putAll(addVars);
		}
		String nextId = parser.parseByProvider(idExprTemplate, varCtx);
		return nextId;
	}
	
	protected String cacheKey(String key) {
		return cachePrefix + ":" + key;
	}

	public void setBaseEntityManager(BaseEntityManager baseEntityManager) {
		this.baseEntityManager = baseEntityManager;
	}
	
}
