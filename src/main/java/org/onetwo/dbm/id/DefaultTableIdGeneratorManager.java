package org.onetwo.dbm.id;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.mapping.DbmMappedEntry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 利用数据库表生成id
 * @author weishao zeng
 * <br/>
 */
public class DefaultTableIdGeneratorManager implements TableIdGeneratorManager {

	private Cache<String, TableIdGenerator> idGeneratorCaches = CacheBuilder.newBuilder()
																		.build();
	private BaseEntityManager baseEntityManager;
	private Expression parser = ExpressionFacotry.BRACE;
	private String cachePrefix = "TableIdGen";
	private int allocationSize = 30;
	
	public DefaultTableIdGeneratorManager(BaseEntityManager baseEntityManager) {
		this.baseEntityManager = baseEntityManager;
	}
	
	public DefaultTableIdGeneratorManager(BaseEntityManager baseEntityManager, int allocationSize) {
		this.allocationSize = allocationSize;
		this.baseEntityManager = baseEntityManager;
	}
	
	public DefaultTableIdGeneratorManager(BaseEntityManager baseEntityManager, String cachePrefix, int allocationSize) {
		this.baseEntityManager = baseEntityManager;
		this.cachePrefix = cachePrefix;
		this.allocationSize = allocationSize;
	}
	
	@SuppressWarnings("unchecked")
	public IdentifierGenerator<Long> getIdentifierGenerator(Class<?> entityClass, String idGeneratorName) {
		DbmSessionImplementor dsi = (DbmSessionImplementor)baseEntityManager.getSessionFactory().getSession();
		DbmMappedEntry entry = dsi.getMappedEntryManager().getEntry(entityClass);
		Map<String, IdentifierGenerator<?>> idGenerators = entry.getIdGenerators();
		if (LangUtils.isEmpty(idGenerators)) {
			idGenerators = entry.getIdentifyFields().stream().map(idField -> {
				return (IdentifierGenerator<Long>)idField.getIdGenerator();
			}).collect(Collectors.toMap(idGen -> idGen.getName(), idGen -> idGen));
		}
		IdentifierGenerator<Long> idGener = null;
		if (StringUtils.isBlank(idGeneratorName)) {
			idGener = LangUtils.getFirst(idGenerators.values()); 
			return idGener;
		}
		idGener = (IdentifierGenerator<Long>)idGenerators.get(idGeneratorName);
		if (idGener==null) {
			throw new DbmException("id generateor not found: " + idGeneratorName);
		}
		return idGener;
	}

	public Long nextId(Class<?> entityClass) {
		IdentifierGenerator<Long> idGener = getIdentifierGenerator(entityClass, null);
		Long nextId = idGener.generate((DbmSessionImplementor)baseEntityManager.getSessionFactory().getSession());
		return nextId;
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
