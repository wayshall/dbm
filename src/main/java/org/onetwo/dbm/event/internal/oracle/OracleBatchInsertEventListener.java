package org.onetwo.dbm.event.internal.oracle;

import java.util.List;

import org.onetwo.common.profiling.TimeCounter;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.event.spi.DbmInsertEvent;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.id.IdentifierGenerator;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.onetwo.dbm.mapping.JdbcStatementContext;

public class OracleBatchInsertEventListener extends OracleInsertEventListener {
	
	@Override
	protected void beforeDoInsert(DbmInsertEvent event, DbmMappedEntry entry){
		if (!entry.isEntity()) {
			return ;
		}
		for(DbmMappedField idField : entry.getIdentifyFields()) {
			generateSeqId(event, entry, idField);
		}
	}

	@SuppressWarnings("unchecked")
	protected void generateSeqId(DbmInsertEvent event, DbmMappedEntry entry, DbmMappedField idField){
		if(idField.isSeqStrategy()){
			Object entity = event.getObject();

			List<Object> list = LangUtils.asList(entity);
			
			/*TimeCounter counter = new TimeCounter("select batch seq...");
			counter.start();*/
			IdentifierGenerator<Long> idGenerator = (IdentifierGenerator<Long>)idField.getIdGenerator();
//			Pair<Long, Long> seqs = idGenerator.batchGenerate(event.getEventSource(), list.size());
//			counter.stop();

			/*long startId = seqs.getFirst();
			long maxId = seqs.getSecond();
			for(Object en : list){
//				entry.setId(en, seqs.get(i++));
				if(startId>maxId){
					throw new DbmException("error id, startId can not greater than maxId. startId:"+startId+", maxId:"+maxId);
				}
				entry.setId(en, startId);
				startId++;
			}*/
			List<Long> seqs = idGenerator.batchGenerate(event.getEventSource(), list.size());
			int i = 0;
			for(Object en : list){
				entry.setId(en, seqs.get(i));
				i++;
			}
		}
	}
	
	/********
	 * 不可以根据更新数量的数目来确定是否成功
	 * oracle如果使用了不符合jdbc3.0规范的本地批量更新机制（BatchPerformanceWorkaround=true），
	 * 每次插入的返回值都是{@linkplain java.sql.Statement#SUCCESS_NO_INFO -2}
	 */
	@Override
	protected void doInsert(DbmInsertEvent event, DbmMappedEntry entry) {
		if(!LangUtils.isMultiple(event.getObject())){
			throw new DbmException("batch insert's args must be a Collection or Array!");
		}
		
		this.beforeDoInsert(event, entry);

		JdbcStatementContext<List<Object[]>> insert = entry.makeInsert(event.getObject());
		TimeCounter counter = new TimeCounter("batch insert "+insert.getValue().size()+"...");
		counter.start();
		int count = this.executeJdbcUpdate(true, insert.getSql(), insert.getValue(), event.getEventSource());
		if(count<0){
			count = Math.abs(count)/2;
		}
		counter.stop();
		
		event.setUpdateCount(count);
	}
}
