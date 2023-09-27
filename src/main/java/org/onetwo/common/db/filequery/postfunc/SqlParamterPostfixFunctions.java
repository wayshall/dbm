package org.onetwo.common.db.filequery.postfunc;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.compress.utils.Lists;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.onetwo.common.convert.Types;
import org.onetwo.common.db.spi.SqlParamterPostfixFunctionRegistry;
import org.onetwo.common.db.sqlext.ExtQueryUtils;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.Springs;
import org.onetwo.common.utils.JodatimeUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.onetwo.dbm.exception.DbmException;

import com.google.common.collect.Maps;

/*****
 * 自定义sql语句里，命名参数的后缀函数
 * 比如 where u.userName = :userName.likeString
 * @author way
 *
 */
public class SqlParamterPostfixFunctions implements SqlParamterPostfixFunctionRegistry {

	public static final String FUNC_START = "$";
	public static final String FUNC_END = "_";
	
	public static final String SQL_POST_FIX_FUNC_MARK = "?";
	
	/*final private static SqlParamterPostfixFunctionRegistry instance = new SqlParamterPostfixFunctions();
	public static SqlParamterPostfixFunctionRegistry getInstance() {
		return instance;
	}*/
	
	public String getFuncPostfixMark(){
		return SQL_POST_FIX_FUNC_MARK;
	}

	private Map<String, SqlPostfixFunction> funcMap = LangUtils.newHashMap();
	private Map<Class<?>, Map<String, SqlPostfixFunction>> typeFuncMap = Maps.newLinkedHashMap();

	public SqlParamterPostfixFunctions(){
		// %value%
		register(new String[]{"like", "likeString"}, new SimpleSqlPostfixFunction(){
			@Override
			public Object toSqlParameterValue(String paramName, Object value) {
				if (value==null) {
					value = "";
				}
				return ExtQueryUtils.getLikeString(value.toString());
			}
		});

		// %value
		register(new String[]{"prelike", "preLikeString"}, new SimpleSqlPostfixFunction(){
			@Override
			public Object toSqlParameterValue(String paramName, Object value) {
				if (value==null) {
					value = "";
				}
				return StringUtils.appendStartWith(value.toString(), "%");
			}
		});

		// value%
		register(new String[]{"postlike", "postLikeString"}, new SimpleSqlPostfixFunction(){
			@Override
			public Object toSqlParameterValue(String paramName, Object value) {
				if (value==null) {
					value = "";
				}
				return StringUtils.appendEndWith(value.toString(), "%");
			}
		});

		register(new String[]{"atStartOfDate"}, new SimpleSqlPostfixFunction(){
			@Override
			public Object toSqlParameterValue(String paramName, Object value) {
				if(!Date.class.isInstance(value)){
					throw new DbmException(paramName+" is not a date, can not invoke atStartOfDate");
				}
				Date date = Types.convertValue(value, Date.class);
				return JodatimeUtils.atStartOfDate(date);
			}
		});

		register(new String[]{"atEndOfDate"}, new SimpleSqlPostfixFunction(){
			@Override
			public Object toSqlParameterValue(String paramName, Object value) {
				if(!Date.class.isInstance(value)){
					throw new DbmException(paramName+" is not a date, can not invoke atEndOfDate");
				}
				Date date = Types.convertValue(value, Date.class);
				return JodatimeUtils.atEndOfDate(date);
			}
		});

		register(new String[]{"atStartOfNextDate"}, (paramName, value)->{
			if(!Date.class.isInstance(value)){
				throw new DbmException(paramName+" is not a date, can not invoke atStartOfNextDate");
			}
			Date date = Types.convertValue(value, Date.class);
			return JodatimeUtils.atStartOfDate(date, 1);
		});

		register(new String[]{"encrypt"}, (paramName, value)->{
			if (value==null) {
				return null;
			}
			if(!String.class.isInstance(value)){
				throw new DbmException("the encrypt field[" + paramName + "] must be String type!");
			}
			StandardPBEStringEncryptor encryptor = Springs.getInstance().getBean(StandardPBEStringEncryptor.class);
			return encryptor.encrypt(value.toString());
		});
		
		bindingTypeFunc(Date.class, DateTypeFuncSet.class);

		/*register("inlist", new SqlParamterPostfixFunction(){
			@Override
			public Object toSqlString(String paramName, Object value) {
				return ParserContextFunctionSet.getInstance().inValue(paramName, value);
			}
		});*/
		
	}

	public SqlParamterPostfixFunctionRegistry register(String postfix, SqlPostfixFunction func){
		return register(postfix, func, true);
	}
	
	public SqlParamterPostfixFunctionRegistry register(String funcName, SqlPostfixFunction func, boolean registerSnakeName){
		checkFuncName(funcName);
		funcMap.put(funcName, func);
		
		String snakeName = StringUtils.convert2UnderLineName(funcName);
		if (!funcName.equals(snakeName) && registerSnakeName) {
			checkFuncName(snakeName);
			funcMap.put(snakeName, func);
		}
		return this;
	}
	
	/****
	 * 检查函数名是否已存在
	 * @param funcName
	 */
	private void checkFuncName(String funcName) {
		if (funcMap.containsKey(funcName)) {
			throw new DbmException("the sql postfix function already exists, name: " + funcName);
		}
	}
	private SqlParamterPostfixFunctionRegistry register(String[] postfixs, SimpleSqlPostfixFunction func){
		for(String postfix : postfixs){
			register(postfix, func);
		}
		return this;
	}
	
	private SqlParamterPostfixFunctionRegistry bindingTypeFunc(Class<?> type, Class<?> typeFuncClass){
		Map<String, SqlPostfixFunction> funcMap = typeFuncMap.get(type);
		if (funcMap==null) {
			funcMap = Maps.newConcurrentMap();
			typeFuncMap.put(type, funcMap);
		}
		List<Method> staticMethods = ReflectUtils.findAllStaticMethods(typeFuncClass);
		for (Method method : staticMethods) {
			String funcName = method.getName();
			
			SqlPostfixFunction func = new SqlPostfixFunction() {
				@Override
				public Object execute(SqlPostfixFunctionInfo funcInfo, String paramName, Object value) {
					try {
						if (method.getParameterCount()==1) {
							return method.invoke(null, value);
						} else {
							return method.invoke(null, value, funcInfo);
						}
					} catch (Exception e) {
						ReflectUtils.handleReflectionException(e);
					}
					return null;
				}
			};
			register(funcName, func);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.onetwo.common.spring.sql.SqlParamterPostfixFunctionRegistry#getFunc(java.lang.String)
	 */
//	@Override
//	@Deprecated
//	public SqlParamterPostfixFunction getFunc(String postfix){
//		if(!funcMap.containsKey(postfix)){
//			throw new DbmException("no postfix func fund: " + postfix);
//		}
//		return funcMap.get(postfix);
//	}
	
	@Override
//	public SqlParamterPostfixFunction getFunc(Object value, String postfix){
	public Object executeFunc(String property, Object value, String postfix){
		Object res = null;
		SqlPostfixFunctionInfo funcInfo = parseSqlPostfixFunc(postfix);
		
		String funcName = funcInfo.getFunctionName();
		SqlPostfixFunction func = funcMap.get(funcName);
		if (func!=null) {
			res = func.execute(funcInfo, property, value);
			return res;
		}
		if (value==null) {
			throw new DbmException("postfix func not fund for null, postfix:" + postfix);
		}
		for (Entry<Class<?>, Map<String, SqlPostfixFunction>> entry : typeFuncMap.entrySet()) {
			Class<?> type = entry.getKey();
			if (type.isAssignableFrom(value.getClass())) {
				func = entry.getValue().get(funcName);
				if (func!=null) {
					break;
				}
			}
		}
		
		if(func==null){
			throw new DbmException("postfix func not fund for type: " + value.getClass() + ", postfix:" + postfix);
		} else {
			res = func.execute(funcInfo, property, value);
		}

		return res;
	}

	/***
	 * 以下划线“_”分割为字符为数组，$开头的视作参数，其余部分再用下划线“_”连接起来，作为需要调用的方法名
	 * 如：d.upload_time <= :request.uploadTime?$30_minutes_ago
	 * 将会调用minutesAgo(30) 或者 minutes_ago(30)
	 * @see https://github.com/wayshall/dbm/issues/51
	 * @see DateTypeFuncSet#minutesAgo(Date, SqlPostfixFunctionInfo)
	 * @param input
	 * @return
	 */
	public SqlPostfixFunctionInfo parseSqlPostfixFunc(String input) {
		List<String> paramsList = Lists.newArrayList();
        List<String> funcNames = Lists.newArrayList();
        
        String[] strs = StringUtils.split(input, FUNC_END);
        for (String str : strs) {
        	if (str.startsWith(FUNC_START)) {
        		String param = str.substring(FUNC_START.length());
        		if (StringUtils.isNotBlank(param)) {
        			paramsList.add(param);
        		}
        	} else {
        		funcNames.add(str);
        	}
        }
		
        String funcName = StringUtils.join(funcNames, FUNC_END);
		return new SqlPostfixFunctionInfo(funcName, paramsList);
	}

	static public class SqlPostfixFunctionInfo {
	    private String functionName;
	    private List<String> argumentNames;
	    
	    public SqlPostfixFunctionInfo(String functionName, List<String> argumentNames) {
	        this.functionName = functionName;
	        this.argumentNames = argumentNames;
	    }
	    
	    public String getFunctionName() {
	        return functionName;
	    }
	    
	    public List<String> getArgumentNames() {
	        return argumentNames;
	    }
	}
	
}
