package org.onetwo.common.dbm.model.entity;

import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Length;
import org.onetwo.dbm.annotation.DbmFieldListeners;
import org.onetwo.dbm.core.BaseModel;
import org.onetwo.dbm.event.DbmEntityFieldListenerAdapter;
import org.onetwo.dbm.mapping.DbmMappedField;

/**
 * @author wayshall
 * <br/>
 */
@Entity
@Table(name="TEST_USER")
public class UserWithListenerEntity extends BaseModel<UserWithListenerEntity, Long> {

	@Id
	@DbmFieldListeners(AutoIdListener.class)
	protected Long id;
	@Length(min=1, max=50)
	protected String userName;
	
	public static class AutoIdListener extends DbmEntityFieldListenerAdapter {
		
		public static final long START_VALUE = 10000;
		
		private AtomicLong idGenerator = new AtomicLong(START_VALUE);

		@Override
		public Object beforeFieldInsert(DbmMappedField field, Object fieldValue) {
			return idGenerator.incrementAndGet();
		}

	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	
}
