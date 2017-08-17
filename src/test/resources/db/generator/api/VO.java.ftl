<#assign requestPath="/${_globalConfig.getModuleName()}/${_tableContext.className}"/>
<#assign pagePath="/${_globalConfig.getModuleName()}/${_tableContext.tableNameWithoutPrefix}"/>

<#assign entityClassName="${_tableContext.className}VO"/>
<#assign entityClassName2="${_tableContext.className}"/>
<#assign idName="${table.primaryKey.javaName}"/>
<#assign idType="${table.primaryKey.javaType.simpleName}"/>

package ${_tableContext.localFullPackage};

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.Date;


import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import lombok.Data;

/***
 * ${(table.comments[0])!''}
 */
@Data
@ApiModel("${_tableContext.className}")
public class ${entityClassName} {

    private ${table.primaryKey.mappingJavaClass.simpleName} ${table.primaryKey.propertyName};
    
<#list table.columns as column>
<#if column.primaryKey == false && !( column.propertyName == 'createAt' || column.propertyName == 'updateAt' ) >
    /***
     * ${(column.comments[0])!''}
     */
    <#if column.nullable>
    @NotNull
    </#if>
    <#if column.mapping.isStringType()>
    @NotBlank
    @Length(max=${column.columnSize})
    @SafeHtml
    <#elseif column.isEmailType()>
    @Email
    <#elseif column.isUrlType()>
    @URL
    </#if>
    @ApiModelProperty(value="${(column.comments[0])!''}")
    private ${column.mappingJavaClass.simpleName} ${column.propertyName};
    
</#if>
</#list>
}
