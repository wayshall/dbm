package org.onetwo.common.dbm.model.hib.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;
import org.onetwo.dbm.annotation.DbmBindValueToField;
import org.onetwo.dbm.annotation.DbmSensitiveField;
import org.onetwo.dbm.annotation.DbmSensitiveField.SensitiveOns;
import org.onetwo.dbm.mapping.DbmEnumValueMapping;
import org.springframework.format.annotation.DateTimeFormat;


/*****
 * 用户表
 * @Entity
 */
@Entity
@Table(name="TEST_USER")
//@TableGenerator(table=Constant.SEQ_TABLE_NAME, pkColumnName="GEN_NAME",valueColumnName="GEN_VALUE", pkColumnValue="SEQ_ADMIN_USER", allocationSize=50, initialValue=1, name="UserEntityGenerator")
public class UserEntity {
	
	/*****
	 * 
	 */
	protected Long id;
  
	/*****
	 * 
	 */
	@Length(min=1, max=50)
	protected String userName;
  
	/*****
	 * 
	 */
	@Length(min=1, max=50)
	@NotBlank
	protected String nickName;

	  
	/*****
	 * 
	 */
	protected String password;
  
	/*****
	 * 
	 */
	@Length(min=0, max=50)
	@Email
	protected String email;
  
	/*****
	 * 
	 */
	protected String mobile;
  
	/*****
	 * 
	 */
	protected UserGenders gender;
	protected UserStatus status;
	
  
	/*****
	 * 
	 */
	protected Date birthday;

	private Integer age;
	private Float height;

	
	//系统代码
	protected String appCode;
	protected String appCodeUnsensitive;
  
	public UserEntity(){
	}
	

	public static enum UserGenders implements DbmEnumValueMapping<Double> {
		FEMALE("女性", 10),
		LADYBOY("人妖", 10.5),
		MALE("男性", 11);
		
		final private String label;
		final private double value;
		private UserGenders(String label, double value) {
			this.label = label;
			this.value = value;
		}
		public String getLabel() {
			return label;
		}
		@Override
		public Double getEnumMappingValue() {
			return value;
		}
		
	}
	
	/*****
	 * 
	 * @return
	 */
	@Id
//	@Column(name="ID")
	public Long getId() {
		return this.id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	/*****
	 * 
	 * @return
	 */
	//hibernate 使用了统一的命名策略后 不能再使用@Column注解自定义
//	@Column(name="USER_NAME")
	public String getUserName() {
		return this.userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/*****
	 * 
	 * @return
	 */
//	@Column(name="NICK_NAME")
	public String getNickName() {
		return this.nickName;
	}
	
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	/*****
	 * 
	 * @return
	 */
//	@Column(name="EMAIL")
	public String getEmail() {
		return this.email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	/*****
	 * 
	 * @return
	 */
//	@Column(name="MOBILE")
	public String getMobile() {
		return this.mobile;
	}
	
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	/*****
	 * 
	 * @return
	 */
	
	/*****
	 * 
	 * @return
	 */
	@Temporal(TemporalType.TIMESTAMP)
//	@Column(name="BIRTHDAY")
	@DateTimeFormat(pattern="yyyy-MM-dd")
	public Date getBirthday() {
		return this.birthday;
	}

	@Enumerated(EnumType.ORDINAL)
	public UserGenders getGender() {
		return gender;
	}

	public void setGender(UserGenders gender) {
		this.gender = gender;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

//	@Column(name="PASSWORD")
	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
	/*@Column(name="APP_CODE")
	public String getAppCode() {
		return appCode;
	}
	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}*/

	

	@Enumerated(EnumType.STRING)
	public UserStatus getStatus() {
		return status;
	}


	public Integer getAge() {
		return age;
	}


	public void setAge(Integer age) {
		this.age = age;
	}


	public String getAppCode() {
		return appCode;
	}

	@DbmSensitiveField(leftPlainTextSize=4, on=SensitiveOns.SELECT)
	@DbmBindValueToField(name="appCode")
	@Transient
	public String getAppCodeUnsensitive() {
		return appCodeUnsensitive;
	}

	public void setAppCodeUnsensitive(String appCodeUnsensitive) {
		this.appCodeUnsensitive = appCodeUnsensitive;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}


	public void setStatus(UserStatus status) {
		this.status = status;
	}
	

	public Float getHeight() {
		return height;
	}


	public void setHeight(Float height) {
		this.height = height;
	}


	public static enum UserStatus {
		NORMAL("正常"),
		STOP("停用"),
		DELETE("删除");
		
		private final String label;
		UserStatus(String label){
			this.label = label;
		}
		public String getLabel() {
			return label;
		}
		public String getValue(){
			return toString();
		}

	}
	public static enum UserGender {
		MALE("男"),
		FEMALE("女");
		
		private final String label;
		UserGender(String label){
			this.label = label;
		}
		public String getLabel() {
			return label;
		}
		public int getValue(){
			return ordinal();
		}

	}
}