<#assign requestPath="/${_globalConfig.getModuleName()}/${_tableContext.className}"/>
<#assign pagePath="/${_globalConfig.getModuleName()}/${_tableContext.tableNameWithoutPrefix}"/>

<#assign servicePackage="${_globalConfig.javaModulePackage}.service"/>
<#assign serviceImplPackage="${_globalConfig.javaModulePackage}.impl.service"/>
<#assign daoPackage="${_globalConfig.javaModulePackage}.dao"/>
<#assign entityPackage="${_globalConfig.javaModulePackage}.entity"/>

<#assign daoClassName="${_tableContext.className}Dao"/>
<#assign daoPropertyName="${_tableContext.propertyName}Dao"/>
<#assign entityClassName="${_tableContext.className}ExtEntity"/>
<#assign idName="${table.primaryKey.javaName}"/>
<#assign idType="${table.primaryKey.javaType.simpleName}"/>

package ${daoPackage};

import java.util.List;

import ${entityPackage}.${entityClassName};
import ${entityPackage}.${_tableContext.className}Example;


public interface ${daoClassName} {
    
    public List<${entityClassName}> findPage(${_tableContext.className}Example example);
    
    public ${entityClassName} findById(${idType} ${idName});
    
}