package org.onetwo.dbm.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.onetwo.common.db.TimeRecordableEntity;
import org.onetwo.common.xml.jaxb.DateAdapter;

@XmlRootElement
@MappedSuperclass
abstract public class BaseEntity implements TimeRecordableEntity{
 
	private static final long serialVersionUID = 122579169646461421L;

	@Column(name="CREATE_AT")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date createAt;

	@Column(name="UPDATE_AT")
	@Temporal(TemporalType.TIMESTAMP)
	protected Date updateAt;

	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Date createTime) {
		this.createAt = createTime;
	}

	@XmlJavaTypeAdapter(DateAdapter.class)
	public Date getUpdateAt() {
		return updateAt;
	}

	public void setUpdateAt(Date lastUpdateTime) {
		this.updateAt = lastUpdateTime;
	}

}
	
	