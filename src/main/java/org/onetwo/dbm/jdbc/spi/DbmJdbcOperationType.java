package org.onetwo.dbm.jdbc.spi;

public enum DbmJdbcOperationType {

	QUERY(DatabaseOperationType.QUERY),
	QUERY_FOR_UPDATE(DatabaseOperationType.QUERY_FOR_UPDATE),
	UPDATE(DatabaseOperationType.UPDATE),
	INSERT(DatabaseOperationType.UPDATE),
	SAVE(DatabaseOperationType.UPDATE),
	DELETE(DatabaseOperationType.UPDATE),
	EXECUTE(DatabaseOperationType.EXECUTE),
	BATCH_INSERT(DatabaseOperationType.BATCH),
	BATCH_UPDATE(DatabaseOperationType.BATCH);
	
	private final DatabaseOperationType databaseOperationType;
	
	private DbmJdbcOperationType(DatabaseOperationType type){
		this.databaseOperationType = type;
	}
	
	public DatabaseOperationType getDatabaseOperationType() {
		return databaseOperationType;
	}

	public static enum DatabaseOperationType {
		QUERY,
		QUERY_FOR_UPDATE,
		UPDATE,
		BATCH,
		EXECUTE
	}
	
}
