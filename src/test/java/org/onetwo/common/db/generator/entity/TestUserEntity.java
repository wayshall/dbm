
package org.onetwo.common.db.generator.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
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
    String userName;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=20)
    String mobile;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=50)
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
    String nickName;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=255)
    String appcode;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=50)
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
    String appCode;
    
    /***
     * 
     */
    @NotBlank
    @Length(max=255)
    String username;
    
}