<#assign requestPath="${_globalConfig.requestModulePath}/${_tableContext.propertyName}"/>
<#assign pagePath="${_globalConfig.requestModulePath}/${_tableContext.tableNameWithoutPrefix?replace('_', '-')}"/>

<#assign servicePackage="${_globalConfig.javaModulePackage}.service"/>
<#assign serviceImplPackage="${_globalConfig.javaModulePackage}.service.impl"/>
<#assign daoPackage="${_globalConfig.javaModulePackage}.dao"/>
<#assign entityPackage="${_globalConfig.javaModulePackage}.entity"/>

<#assign voClassName="${_tableContext.className}VO"/>
<#assign entityClassName="${_tableContext.className}Entity"/>
<#assign entityClassName2="${_tableContext.className}"/>
<#assign serviceImplClassName="${_tableContext.className}ServiceImpl"/>
<#assign serviceImplPropertyName="${_tableContext.propertyName}Service"/>
<#assign mapperClassName="${_tableContext.className}Mapper"/>
<#assign mapperPropertyName="${_tableContext.propertyName}Mapper"/>
<#assign idName="${table.primaryKey.javaName}"/>
<#assign idType="${table.primaryKey.javaType.simpleName}"/>


package ${_globalConfig.getJavaLocalPackage(_tableContext.localPackage)};

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;

import org.onetwo.common.data.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.MediaType;

import ${_globalConfig.javaModulePackage}.vo.${voClassName};
import ${serviceImplPackage}.${serviceImplClassName};
import ${_globalConfig.javaModulePackage}.util.WebConstants;

@RestController
@RequestMapping(WebConstants.BASE_PATH+"${requestPath}")
public class ${_tableContext.className}Controller {

    @Autowired
    private ${serviceImplClassName} ${serviceImplPropertyName};
    
    @ApiOperation(value="${(table.comments[0])!''}")
    @ApiResponses(value={
        @ApiResponse(code=200, message="Success"),
        @ApiResponse(code=500, message="Error", response=Result.class)
    })
    @RequestMapping(method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<${voClassName}> list(){
        List<${voClassName}> list = ${serviceImplPropertyName}.findAll();
        return list;
    }
}
