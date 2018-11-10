package org.onetwo.common.dbm.model.entity;

import java.util.Date;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.onetwo.common.db.filter.DataQueryParamaterEnhancer;
import org.onetwo.common.db.filter.IDataQueryParamterEnhancer;
import org.onetwo.common.db.sqlext.ExtQuery;
import org.onetwo.common.dbm.model.entity.UserEntity.UserStatus;
import org.onetwo.common.dbm.model.entity.UserWithDataFilterEntity.AgeIDataQueryParamterEnhancer;
import org.onetwo.common.utils.CUtils;
import org.springframework.format.annotation.DateTimeFormat;


/*****
 * 用户表
 * @Entity
 */
@Entity
@Table(name="TEST_USER")
@DataQueryParamaterEnhancer(AgeIDataQueryParamterEnhancer.class)
public class UserWithDataFilterEntity {

	
	public static class AgeIDataQueryParamterEnhancer implements IDataQueryParamterEnhancer {
		static public final int FIXED_AGE = 34;

		@Override
		public Map<Object, Object> enhanceParameters(ExtQuery query) {
			return CUtils.asMap("age", FIXED_AGE);
		}
		
	}
	
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
	protected Integer gender;
	protected UserStatus status;
	
  
	/*****
	 * 
	 */
	protected Date birthday;

	private Integer age;
	private Float height;

	
	//系统代码
	protected String appCode;
  
	public UserWithDataFilterEntity(){
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
//	@Column(name="GENDER")
	public Integer getGender() {
		return this.gender;
	}
	
	public void setGender(Integer gender) {
		this.gender = gender;
	}
	
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

}