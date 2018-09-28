<#assign apiName="${table.propertyName}Api"/>
<#assign formComponentName="${table.propertyName}MgrForm"/>
<#assign moduleName="${_globalConfig.getModuleName()}"/>
<template>
  <div class="app-container">

    <layout-table
      ref="listTable"
      :list-api="listApi"
      :query-form-model="queryFormModel"
      :delete-api="deleteApi">
      <template slot="queryForm">
  <#list table.columns as column>
    <#if !column.primaryKey>
        <el-form-item label="${(column.comments[0])!''}">
          <el-input v-model="queryFormModel.${column.javaName}" placeholder="${(column.comments[0])!''}"/>
        </el-form-item>
    </#if>
  </#list>
      </template>

      <template slot="toolbar">
        <el-button type="primary" icon="el-icon-edit" @click="handleAdd">
          添加${(table.comments[0])!''}
        </el-button>
      </template>

      <el-table-column align="center" width="80" type="selection"/>
  <#list table.columns as column>
      <el-table-column align="center" label="${(column.comments[0])!''}" width="100">
        <template slot-scope="scope">
        <#if column.isDateType()>
          <span>{{ scope.row.${column.javaName} | formatDateInMillis }}</span>
        <#else>
          <span>{{ scope.row.${column.javaName} }}</span>
        </#if>
        </template>
      </el-table-column>
  </#list>
      <el-table-column align="center" label="操作">
        <template slot-scope="scope">
          <el-dropdown size="medium" split-button type="primary" @command="handleAction">
            操作…
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item :command="{action: 'edit', row: scope.row}">编 辑</el-dropdown-item>
              <el-dropdown-item :command="{action: 'delete', row: scope.row}" divided>删 除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </layout-table>

    <${table.propertyName}-mgr-form :status-mode="dialog.status" :visible.sync="dialog.visible" :data-model="dataModel" @finishHandle="refreshTable"/>

  </div>
</template>

<script>
import layoutTable from '@/components/xui/layoutTable'
import * as ${apiName} from '@/api/${moduleName}/${apiName}'
import ${formComponentName} from './${formComponentName}'

export default {
  name: '${_tableContext.className}Mgr',
  components: {
    ${formComponentName},
    layoutTable
  },
  data() {
    return {
      queryFormModel: {
  <#list table.columns as column>
    <#if !column.primaryKey>
        ${column.javaName}: '',
    </#if>
  </#list>
        ${table.primaryKey.javaName}: null
      },
      dialog: {
        status: '',
        visible: false
      },
      dataModel: this.initDataModel()
    }
  },
  mounted: function() {
  },
  methods: {
    listApi: ${apiName}.getList,
    deleteApi: ${apiName}.remove,
    refreshTable() {
      this.$refs.listTable.getList()
    },
    // 初始化dataModel
    initDataModel() {
      return {
  <#list table.columns as column>
    <#if !column.primaryKey>
      <#if column.isFileType()>
        ${column.javaName}File: null,
      <#else>
        ${column.javaName}: '',
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
        this.handleDelete([data.row.${table.primaryKey.javaName}])
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
      ${apiName}.get(row.id).then(res => {
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

