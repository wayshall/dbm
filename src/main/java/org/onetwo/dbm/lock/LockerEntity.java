package org.onetwo.dbm.lock;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

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

