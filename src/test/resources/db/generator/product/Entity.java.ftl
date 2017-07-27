<#assign requestPath="/${_globalConfig.getModuleName()}/${_tableContext.className}"/>
<#assign pagePath="/${_globalConfig.getModuleName()}/${_tableContext.tableNameWithoutPrefix}"/>

<#assign entityClassName="${_tableContext.className}Entity"/>
<#assign entityClassName2="${_tableContext.className}"/>
<#assign idName="${table.primaryKey.javaName}"/>
<#assign idType="${table.primaryKey.javaType.simpleName}"/>

package ${_tableContext.localFullPackage};

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

import org.onetwo.dbm.jpa.BaseEntity;

import lombok.Data;

@SuppressWarnings("serial")
@Entity
@Table(name="${table.name}")
@Data
public class ${entityClassName2} extends BaseEntity  {

<#list table.columns as column>
    <#if column.primaryKey>
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    </#if>
    <#if column.nullable>
    @NotNull
    </#if>
    <#if column.mapping.isStringType()>
    @NotBlank
    @Length(max=${column.columnSize})
    @SafeHtml
    <#elseif column.mapping.isEmailType()>
    @Email
    <#elseif column.mapping.isUrlType()>
    @URL
    </#if>
    private ${column.javaType.simpleName} ${column.propertyName};
</#list>
}
