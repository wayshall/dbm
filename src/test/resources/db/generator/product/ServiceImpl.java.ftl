<#assign requestPath="/${_globalConfig.getModuleName()}/${_tableContext.className}"/>
<#assign pagePath="/${_globalConfig.getModuleName()}/${_tableContext.tableNameWithoutPrefix}"/>

<#assign servicePackage="${_globalConfig.javaModulePackage}.service"/>
<#assign serviceImplPackage="${_globalConfig.javaModulePackage}.impl.service"/>
<#assign daoPackage="${_globalConfig.javaModulePackage}.dao"/>
<#assign entityPackage="${_globalConfig.javaModulePackage}.entity"/>

<#assign serviceImplClassName="${_tableContext.className}ServiceImpl"/>
<#assign serviceImplPropertyName="${_tableContext.propertyName}ServiceImpl"/>
<#assign mapperClassName="${_tableContext.className}Mapper"/>
<#assign mapperPropertyName="${_tableContext.propertyName}Mapper"/>
<#assign idName="${table.primaryKey.javaName}"/>
<#assign idType="${table.primaryKey.javaType.simpleName}"/>

package ${_globalConfig.getJavaLocalPackage(_tableContext.localPackage)};

import java.util.Collection;

import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.db.builder.Querys;
import org.onetwo.common.utils.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ${entityPackage}.${_tableContext.className};

@Service
@Transactional
public class ${serviceImplClassName} {

    @Autowired
    private BaseEntityManager baseEntityManager;
    
    public Page<${_tableContext.className}> findPage(Page<${_tableContext.className}> page, ${_tableContext.className} ${_tableContext.propertyName}){
        return Querys.from(baseEntityManager, ${_tableContext.className}.class)
                	.where()
            		  .addFields(${_tableContext.propertyName})
            		  .ignoreIfNull()
            		.end()
            		.toQuery()
            		.page(page);
    }
    
    public void save(${_tableContext.className} entity) {
		baseEntityManager.persist(entity);
	}

	public void update(${_tableContext.className} entity) {
		baseEntityManager.update(entity);
	}
    
    public ${_tableContext.className} findById(${idType} id) {
		return baseEntityManager.findById(${_tableContext.className}.class, id);
	}

	public Collection<${_tableContext.className}> removeByIds(${idType}... id) {
		return baseEntityManager.removeByIds(${_tableContext.className}.class, id);
	}
}
