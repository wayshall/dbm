package org.onetwo.dbm.event.spi;

/****
 * 同一个操作可能触发session和jdbc事件
 * @author way
 *
 */
public enum DbmEventAction {
	//for dbm session event action
	insertOrUpdate(true, true),
	insert(true, false),
	update(false, true),
	delete(false, false),
	find(false, false),
	lock(false, false),
	
	extQuery(false, false),
	
	batchInsert(true, false),
	batchUpdate(false, true),
	batchInsertOrUpdate(true, true),
	batchInsertOrIgnore(true, false);
//	saveRef,
//	dropRef
	
	//for jdbc operation event
	/*jdbcAfterUpdate,
	jdbcAfterQuery*/
	
	private boolean inserting;
	private boolean updating;
	private DbmEventAction(boolean inserting, boolean updating) {
		this.inserting = inserting;
		this.updating = updating;
	}
	
	public boolean isInserting() {
		return inserting;
	}
	public boolean isUpdating() {
		return updating;
	}
	
}
