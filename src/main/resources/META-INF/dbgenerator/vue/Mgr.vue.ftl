<#assign apiName="${table.propertyName}Api"/>
<#assign formComponentName="${table.propertyName}Form"/>
<#assign moduleName="${_globalConfig.getModuleName()}"/>
<#assign searchableFields=DUIEntityMeta.searchableFields/>
<template>
  <div class="app-container">

    <layout-table
      ref="listTable"
      id-property="${table.primaryKey.javaName}"
      :list-api="listApi"
      :query-form-model="queryFormModel"
      :refresh.sync="refreshTable"
      :delete-api="deleteApi"
      :operations="operations">
  <#if searchableFields.isEmpty()==false>
      <template slot="queryForm">
    <#list searchableFields as field>
        <el-form-item label="${(field.label)!''}">
          <el-input v-model="queryFormModel.${field.column.javaName}" placeholder="${(field.label)!''}"/>
        </el-form-item>
    </#list>
      </template>
  </#if>

      <template slot="toolbar">
        <el-button type="primary" icon="el-icon-edit" @click="handleAdd">
          添加${(DUIEntityMeta.label)!''}
        </el-button>
      </template>

      <el-table-column align="center" width="80" type="selection"/>
  <#list DUIEntityMeta.listableFields as field>
    <#if field.column.isDateType()>
      <el-table-column align="center" label="${(field.label)!''}" <#if field?counter != DUIEntityMeta.listableFields.size()>width="100"</#if>>
        <template slot-scope="scope">
          <span>{{ scope.row.${field.column.javaName} | formatDateInMillis }}</span>
        </template>
      </el-table-column>
    <#else>
      <el-table-column align="center" label="${(field.label)!''}" prop="${field.listField}" <#if field?counter != DUIEntityMeta.listableFields.size()>width="100"</#if>/>
    </#if>
  </#list>
    </layout-table>

    <el-dialog
      title="${DUIEntityMeta.label}管理"
      :visible.sync="dialog.visible"
      :close-on-click-modal="false"
      :before-close="handleClose">
      <el-tabs type="border-card">
        <el-tab-pane label="${DUIEntityMeta.label}编辑">
          <${table.propertyName}-form :status-mode="dialog.status" :data-model="dataModel" @finishHandle="on${_tableContext.className}Finish"/>
        </el-tab-pane>
      <#list DUIEntityMeta.editableEntities as editableEntity>
        <el-tab-pane label="${editableEntity.label}" :disabled="dataModel.id==null">
          <${editableEntity.table.horizontalBarName}-form :status-mode="dialog.status" :data-model="dataModel"/>
        </el-tab-pane>
      </#list>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import * as ${apiName} from '@/api/${vueModuleName}/${apiName}'
import ${formComponentName} from './${formComponentName}'
<#list DUIEntityMeta.editableEntities as editableEntity>
import ${editableEntity.table.propertyName}Form from './${editableEntity.table.propertyName}Form'
</#list>

export default {
  name: '${_tableContext.className}',
  components: {
<#list DUIEntityMeta.editableEntities as editableEntity>
    ${editableEntity.table.propertyName}Form,
</#list>
    ${formComponentName}
  },
  data() {
    return {
      queryFormModel: {
  <#list searchableFields as field>
        ${field.column.javaName}: '',
  </#list>
        ${table.primaryKey.javaName}: null
      },
      dialog: {
        status: '',
        visible: false
      },
      dataModel: this.initDataModel(),
      refreshTable: false,
      operations: [
        { action: 'edit', text: '编辑', handler: this.handleEdit }
      ]
    }
  },
  mounted: function() {
  },
  methods: {
    handleClose() {
      // 清除验证信息
      // this.$refs.dataForm.resetFields()
      this.dialog.visible = false
      return true
    },
    on${_tableContext.className}Finish() {
      this.refreshTable = true
      this.dialog.visible = false
    },
    listApi: ${apiName}.getList,
    deleteApi: ${apiName}.remove,
    // 初始化dataModel
    initDataModel() {
      return {
  <#list DUIEntityMeta.formFields as field>
    <#if !field.column.primaryKey>
      <#if field.column.isFileType()>
        ${field.column.javaName}File: null,
      <#else>
        ${field.column.javaName}: '',
      </#if>
    </#if>
  </#list>
        ${table.primaryKey.javaName}: null
      }
    },
    // 操作菜单处理，根据command分派到不同方法
    handleAction(data) {
      const command = data.action
      if (command === 'edit') {
        this.handleEdit(data.row)
      } else if (command === 'delete') {
        this.$refs.listTable.handleDelete([data.row.${table.primaryKey.javaName}])
      }
    },
    handleAdd() {
      this.dialog.status = 'Add'
      this.dialog.visible = true
      this.dataModel = this.initDataModel()
    },
    handleEdit(row) {
      this.dialog.status = 'Edit'
      this.dialog.visible = true
      this.dialog.dataId = row.${table.primaryKey.javaName}
      ${apiName}.get(row.${table.primaryKey.javaName}).then(res => {
        this.dataModel = res.data.data
      })
    }
  }
}
</script>

<style lang="scss">
.text-wrapper {
  white-space: pre-wrap;
}
</style>

