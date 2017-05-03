package org.onetwo.dbm.id;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.onetwo.common.convert.Types;
import org.onetwo.common.db.sql.SequenceNameManager;
import org.onetwo.common.profiling.TimeCounter;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.Assert;
import org.onetwo.dbm.core.spi.DbmSessionImplementor;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * @author wayshall
 * <br/>
 */
public class SequenceIdGenerator implements IdGenerator<Long> {
	
	final private SequenceGeneratorAttrs attrs;
	private Queue<Long> seqQueue;
	
	public SequenceIdGenerator(SequenceGeneratorAttrs attrs) {
		super();
		this.attrs = attrs;
		this.seqQueue = new LinkedList<Long>();
	}
	
	@Override
	public String getName() {
		return attrs.getName();
	}

	@Override
	public synchronized Long generate(DbmSessionImplementor session) {
		Long id = seqQueue.poll();
		if(id==null){
			List<Long> seqs = batchGenerate(session, attrs.getAllocationSize());
			seqQueue.addAll(seqs);
			id = seqQueue.poll();
		}
		return id;
	}
	
	private Long generateOneSeq(DbmSessionImplementor session) {
		SequenceNameManager sequenceNameManager = session.getSequenceNameManager();
		String seqSql = sequenceNameManager.getSequenceSql(attrs.getSequenceName(), null);
		Long id = null;
		try {
			id = session.getDbmJdbcOperations().queryForObject(seqSql, Long.class);
		} catch (BadSqlGrammarException e) {
			if(createSeqIfNecessary(e, session)){
				id = session.getDbmJdbcOperations().queryForObject(seqSql, Long.class);
				if(id==null)
					throw e;
			}
		}
		return id;
	}
	
	private boolean createSeqIfNecessary(BadSqlGrammarException e, DbmSessionImplementor session){
		SequenceNameManager sequenceNameManager = session.getSequenceNameManager();
		//ORA-02289: 序列不存在
		SQLException sqe = e.getSQLException();
		int vendorCode = Types.convertValue(ReflectUtils.getFieldValue(sqe, "vendorCode"), int.class);
		if(vendorCode==2289){
			session.getDbmJdbcOperations().execute(sequenceNameManager.getCreateSequence(attrs.getSequenceName(), attrs.getInitialValue()));
			return true;
		}
		return false;
	}

	@Override
	public List<Long> batchGenerate(DbmSessionImplementor session, int batchSize) {
		Assert.notNull(session);
		Assert.isTrue(batchSize>0);
		
		SequenceNameManager sequenceNameManager = session.getSequenceNameManager();
		TimeCounter counter = new TimeCounter("select batch seq...");
		counter.start();
		String seqSql = sequenceNameManager.getSequenceSql(attrs.getSequenceName(), batchSize);
		List<Long> seqs = null;
		try {
			seqs = session.getDbmJdbcOperations().queryForList(seqSql, Long.class, batchSize);
			Assert.isTrue(batchSize==seqs.size(), "the size of seq is not equals to data, seq size:"+seqs.size()+", data size:"+batchSize);
		} catch (BadSqlGrammarException e) {
			if(createSeqIfNecessary(e, session)){
				seqs = session.getDbmJdbcOperations().queryForList(seqSql, Long.class, batchSize);
				if(seqs==null)
					throw e;
				Assert.isTrue(batchSize==seqs.size(), "the size of seq is not equals to data, seq size:"+seqs.size()+", data size:"+batchSize);
			}
		}
		counter.stop();
		return seqs;
	}

	

}
