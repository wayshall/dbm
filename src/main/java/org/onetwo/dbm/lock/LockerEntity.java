package org.onetwo.dbm.lock;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

/**
 * @author weishao zeng
 * <br/>
 */
@SuppressWarnings("serial")
@Entity
@Table(name="dbm_lock")
@Data
public class LockerEntity implements Serializable {
	
	@Id
	String id;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date lockAt;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date releaseAt;

}

