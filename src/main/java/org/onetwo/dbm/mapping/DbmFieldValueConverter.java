package org.onetwo.dbm.mapping;
/**
 * @author wayshall
 * <br/>
 */
public interface DbmFieldValueConverter {
	/****
	 * 
	 * @author wayshall
	 * @param field
	 * @param fieldValue StoreType
	 * @return JavaType
	 */
	Object forJava(DbmMappedField field, Object fieldValue);
	
	/****
	 * 
	 * @author wayshall
	 * @param field
	 * @param fieldValue JavaType
	 * @return StoreType
	 */
	Object forStore(DbmMappedField field, Object fieldValue);

}
