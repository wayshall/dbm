package org.onetwo.common.dbm.model.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.onetwo.common.db.TimeRecordableEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserGenders;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserStatus;

import lombok.Data;


/*****
 * 用户表
 * @Entity
 */
@Entity
@Table(name="TEST_USER")
@Data
public class UserVersionEntity implements TimeRecordableEntity {
	
	/*****
	 * 
	 */
	@Id
	Long id;
  
	/*****
	 * 
	 */
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
	UserGenders gender;
	@Enumerated(EnumType.STRING)
	UserStatus status;
	
  
	/*****
	 * 
	 */
	Date birthday;

	Integer age;
	Float height;

	
	//系统代码
	String appCode;
	
	Date createAt;
	
	@Version
	Date updateAt;
	
}