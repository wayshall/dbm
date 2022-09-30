
package org.onetwo.common.db.generator.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import org.onetwo.dbm.annotation.DbmIdGenerator;
import org.onetwo.dbm.id.SnowflakeGenerator;
import org.onetwo.dbm.jpa.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name="test_user")
@Data
@EqualsAndHashCode(callSuper=true)
public class TestUserEntity extends BaseEntity  {

    @Id
    //@GeneratedValue(strategy=GenerationType.IDENTITY)
    @GeneratedValue(strategy = GenerationType.AUTO, generator="snowflake") 
    @DbmIdGenerator(name="snowflake", generatorClass=SnowflakeGenerator.class)
    @NotNull
    Long id;
    
    /***
     * 
     */
    Date birthday;
    
    /***
     * 
     */
    Integer gender;
    
    /***
     * 
     */
    Long dataVersion;
    
    /***
     * 
     */
    @NotNull
    @NotBlank
    @Length(max=50)
    @SafeHtml
    String userName;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=20)
    @SafeHtml
    String mobile;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=50)
    @SafeHtml
    String password;
    
    /***
     * 类型：json
     */
    @org.onetwo.dbm.annotation.DbmJsonField
    java.util.Map<String, String> addressList;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=50)
    @SafeHtml
    String nickName;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=255)
    @SafeHtml
    String appcode;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=50)
    @SafeHtml
    String email;
    
    /***
     * 
     */
    Integer age;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=20)
    @SafeHtml
    String status;
    
    /***
     * 
     */
    Float height;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=20)
    @SafeHtml
    String appCode;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=255)
    @SafeHtml
    String username;
    
}