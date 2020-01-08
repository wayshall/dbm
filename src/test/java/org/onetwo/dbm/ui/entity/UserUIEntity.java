
package org.onetwo.dbm.ui.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserGenders;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserStatus;
import org.onetwo.dbm.annotation.SnowflakeId;
import org.onetwo.dbm.jpa.BaseEntity;
import org.onetwo.dbm.ui.annotation.UIClass;
import org.onetwo.dbm.ui.annotation.UIField;
import org.onetwo.dbm.ui.annotation.UISelect;

import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 * 租户表
 */
@SuppressWarnings("serial")
@Entity
@Table(name="TEST_USER")
@Data
@EqualsAndHashCode(callSuper=true)
@UIClass(name = "TestUser", label = "用户")
public class UserUIEntity extends BaseEntity {

    @SnowflakeId
    @NotNull

	@Id
	Long id;
  
	/*****
	 * 
	 */
    @UIField(label = "用户名称")
	@Length(min=1, max=50)
	String userName;
  
	/*****
	 * 
	 */
	@Length(min=1, max=50)
	@NotBlank
	String nickName;

	  
	/*****
	 * 
	 */
	String password;
  
	/*****
	 * 
	 */
	@Length(min=0, max=50)
	@Email
	String email;
  
	/*****
	 * 
	 */
	String mobile;
  
	/*****
	 * 
	 */
	@Enumerated(EnumType.ORDINAL)
    @UIField(label = "性别")
    @UISelect(dataEnumClass=UserGenders.class, valueField = "mappingValue")
	UserGenders gender;
	
	@Enumerated(EnumType.STRING)
    @UIField(label = "用户状态", updatable= true, insertable = false)
    @UISelect(dataEnumClass=UserStatus.class)
	UserStatus status;
    
}