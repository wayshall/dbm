package org.onetwo.common.db.generator;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.onetwo.common.db.generator.GlobalConfig.OutfilePathFunc;
import org.onetwo.common.db.generator.dialet.DatabaseMetaDialet;
import org.onetwo.common.db.generator.dialet.DelegateDatabaseMetaDialet;
import org.onetwo.common.db.generator.mapping.ColumnMapping;
import org.onetwo.common.db.generator.mapping.ColumnMapping.ColumnAttrValueFunc;
import org.onetwo.common.db.generator.meta.ColumnMeta;
import org.onetwo.common.db.generator.meta.TableMeta;
import org.onetwo.common.file.FileUtils;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbGenerator {
	//TableContextCreator
//	private static final String TABLE_CONTEXT_KEY = "_tableContext";
//	private static final String TABLE_KEY = "table";
	
	public static DbGenerator newGenerator(DataSource dataSource){
		return new DbGenerator(dataSource);
	}

	private DataSource dataSource;
	private List<DbTableGenerator> tableGenerators = Lists.newArrayList();
	private DatabaseMetaDialet dialet;
	private TemplateEngine templateEngine;
	private GlobalConfig globalConfig = new GlobalConfig(this);
	
	public DbGenerator(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	public DbGenerator(DatabaseMetaDialet dialet) {
		super();
		this.dialet = dialet;
	}

	public DbGenerator(DataSource dataSource, TemplateEngine ftlGenerator) {
		super();
		this.dataSource = dataSource;
		this.templateEngine = ftlGenerator;
		this.dialet = new DelegateDatabaseMetaDialet(dataSource);
		/*DataBase db = JdbcUtils.getDataBase(dataSource);
		if(db==DataBase.MySQL){
			mysql();
		}else if(db==DataBase.Oracle){
			oracle();
		}else{
			throw new DbmException("unsupported database : " + db);
		}*/
	}
	public DbGenerator templateEngine(TemplateEngine templateEngine){
		this.templateEngine = templateEngine;
		return this;
	}
	
	/*final public DbGenerator mysql(){
		this.dialet = new MysqlMetaDialet(dataSource);
		return this;
	}
	
	final public DbGenerator oracle(){
		this.dialet = new OracleMetaDialet(dataSource);
		return this;
	}*/
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public DatabaseMetaDialet databaseMetaDialet() {
		return dialet;
	}

	public DbGenerator dialet(DatabaseMetaDialet dialet){
		this.dialet = dialet;
		return this;
	}
	
	public DbGenerator allColumnMappingAttr(String name, Object value){
		this.dialet.getMetaMapping().getColumnMappings().forEach(map->map.attr(name, value));
		return this;
	}
	
	public DbGenerator allColumnMappingAttr(String name, ColumnAttrValueFunc value){
		this.dialet.getMetaMapping().getColumnMappings().forEach(map->map.attr(name, value));
		return this;
	}
	
	public ColumnMappingBuilder columnMapping(int sqlType){
		ColumnMapping mapping = this.dialet.getColumnMapping(sqlType);
		return new ColumnMappingBuilder(mapping);
	}

	public DbGenerator addTableDbGenerator(DbTableGenerator tableGenerator){
		tableGenerators.add(tableGenerator);
		return this;
	}

	public GlobalConfig globalConfig(){
		return globalConfig;
	}

	public DbGenerator stripTablePrefix(String stripTablePrefix){
//		globalConfig.defaultTableContexts().stripTablePrefix(stripTablePrefix);
		globalConfig.stripTablePrefix(stripTablePrefix);
		return this;
	}

	public DbTableGenerator table(String tableName){
		DbTableGenerator tableg = new DbTableGenerator(tableName);
		tableGenerators.add(tableg);
		return tableg;
	}

	public DbGenerator tables(String... tableNames){
		Stream.of(tableNames).forEach(tableName->{
			DbTableGenerator tableg = new DbTableGenerator(tableName);
			tableGenerators.add(tableg);
		});
		return this;
	}

	/*public DbGenerator generateConfig(String templatePath, String outDir){
		tableGenerators.forEach(tg->{
			String prefix = tg.tableName.replace("_", "-");
			String outfilePath = outDir + "/"+ prefix + "-" + FileUtils.getFileNameWithoutExt(templatePath);
			tg.template(templatePath).outfilePath(outfilePath);
		});
		return this;
	}*/
	public DbGenerator generateConfig(String templatePath, OutfilePathFunc outFunc){
		tableGenerators.forEach(tg->{
			tg.template(templatePath).outfilePathFunc(outFunc);
		});
		return this;
	}
	
	public List<GeneratedResult<File>> generate(Map<String, Object> context){
		templateEngine.afterPropertiesSet();
		Assert.notNull(dialet, "dialet can not be null!");
		List<GeneratedResult<File>> results = Lists.newArrayList();
		tableGenerators.forEach(table->{
			GeneratedResult<File> r = table.generate(dialet, templateEngine, context);
			results.add(r);
		});
		return results;
	}
	
	
	public class ColumnMappingBuilder {
		private ColumnMapping metaMapping;

		public ColumnMappingBuilder(ColumnMapping metaMapping) {
			super();
			this.metaMapping = metaMapping;
		}
		
		public ColumnMappingBuilder javaType(Class<?> javaType){
			this.metaMapping.javaType(javaType);
			return this;
		}
		
		public ColumnMappingBuilder attr(String name, Object value){
			this.metaMapping.attr(name, value);
			return this;
		}
		
		public DbGenerator endColumMapping(){
			return DbGenerator.this;
		}
		
	}
	
	public class DbTableGenerator {
		
		private String tableName;
		private List<TableGeneratedConfig> tableGeneratedConfig = Lists.newArrayList();
		private TableMeta tableMeta = null;
		private Map<String, Object> tableContext = Maps.newHashMap();
		

		public DbTableGenerator(String tableName) {
			super();
			this.tableName = tableName;
			this.tableMeta = dialet.getTableMeta(tableName);
		}
		
		public Map<String, Object> context() {
			return tableContext;
		}
		
		public TableMetaConfig meta(){
			return new TableMetaConfig(tableMeta, this);
		}
		
		public DbTableGenerator addGeneratedConfig(String templatePath, String outfilePath){
			tableGeneratedConfig.add(new TableGeneratedConfig(templatePath, outfilePath));
			return this;
		}
		
		public TableGeneratedConfig template(String templatePath){
			TableGeneratedConfig config = new TableGeneratedConfig(tableName, templatePath);
			tableGeneratedConfig.add(config);
			return config;
		}
		
		public TableGeneratedConfig template(String templatePath, OutfilePathFunc outFileNameFunc){
			TableGeneratedConfig config = new TableGeneratedConfig(tableName, templatePath);
			config.outfilePathFunc(outFileNameFunc);
			tableGeneratedConfig.add(config);
			return config;
		}
		
		public DbTableGenerator pageTemplate(String templatePath){
			OutfilePathFunc outFileNameFunc = ctx->{
				TableGeneratedConfig c = ctx.getConfig();
				String tableShortName = c.tableNameStripStart(c.globalGeneratedConfig().getStripTablePrefix());
				String pageFileBaseDir = c.globalGeneratedConfig().getPageFileBaseDir();
				Assert.notNull(pageFileBaseDir, "pageFileBaseDir can not be null");
				String moduleName = c.globalGeneratedConfig().getModuleName();
				Assert.notNull(moduleName, "moduleName can not be null");
				
				String filePath = pageFileBaseDir +
				"/"+moduleName+"/"+
				tableShortName.replace('_', '-')+
				"-"+FileUtils.getFileNameWithoutExt(templatePath);
				return filePath.toLowerCase();
			};
			/*TableGeneratedConfig config = new TableGeneratedConfig(tableName, templatePath);
			config.outfilePathFunc();
			tableGeneratedConfig.add(config);*/
			return pageTemplate(templatePath, outFileNameFunc);
		}

		public DbTableGenerator pageTemplate(String templatePath, OutfilePathFunc outFileNameFunc){
			TableGeneratedConfig config = new TableGeneratedConfig(tableName, templatePath);
			config.outfilePathFunc(outFileNameFunc);
			tableGeneratedConfig.add(config);
			return this;
		}
		
		
		public DbTableGenerator mybatisDaoXmlTemplate(String templatePath){
			TableGeneratedConfig config = new TableGeneratedConfig(tableName, templatePath);
			config.outfilePathFunc(ctx->{
										TableGeneratedConfig c = ctx.getConfig();
										String filePath = c.globalGeneratedConfig().getFullModulePackageNameAsPath();
										filePath =  "/mybatis/dao/" + filePath + "/dao";
										filePath = this.getResourceDirOutfilePathByModule(c, filePath, templatePath);
										return filePath;
									}
								);
			tableGeneratedConfig.add(config);
			return this;
		}
		
		private String getResourceDirOutfilePathByModule(TableGeneratedConfig c, String typePath, String templatePath){
			String tableShortName = c.tableNameStripStart(c.globalGeneratedConfig().getStripTablePrefix());
			String filePath = c.globalGeneratedConfig().getResourceDir()+typePath+ "/" + 
			StringUtils.toClassName(tableShortName) + FileUtils.getFileNameWithoutExt(templatePath);
			return filePath;
		}
		

		/*
		 * public DbTableGenerator controllerTemplate(String templatePath){ return
		 * controllerTemplate("web", templatePath); }
		
		public DbTableGenerator controllerTemplate(String controllerPackage, String templatePath){
			TableGeneratedConfig config = new TableGeneratedConfig(tableName, templatePath);
			config.outfilePathFunc(c->{
									c.setLocalPackage(controllerPackage);
									String tableShortName = c.tableNameStripStart(c.globalGeneratedConfig().getStripTablePrefix());
									String filePath = c.globalGeneratedConfig().getFullModulePackagePath()+"/"+controllerPackage+"/"+
									StringUtils.toClassName(tableShortName)+
									FileUtils.getFileNameWithoutExt(templatePath);
									return filePath;
								}
							);
			tableGeneratedConfig.add(config);
			return this;
		} */
		
		public DbTableGenerator serviceImplTemplate(String templatePath){
			return javaClassTemplate("service.impl", templatePath);
		}
		
		public DbTableGenerator javaClassTemplate(String javaClassPackage, String templatePath){
			TableGeneratedConfig config = new TableGeneratedConfig(tableName, templatePath);
			config.outfilePathFunc(c->{
									c.getConfig().setLocalPackage(javaClassPackage);
									String tableShortName = c.getConfig().tableNameStripStart(globalConfig.getStripTablePrefix());
									String servicePath = javaClassPackage.replace('.', '/');
									servicePath = StringUtils.appendArroundWith(servicePath, "/");
									String filePath = globalConfig.getFullModulePackagePath() + 
																	servicePath +
																	StringUtils.toClassName(tableShortName)+
																	FileUtils.getFileNameWithoutExt(templatePath);
									return filePath;
								}
							);
			tableGeneratedConfig.add(config);
			return this;
		}
		
		public DbTableGenerator daoTemplate(String templatePath){
			String localPackage = "dao";
			TableGeneratedConfig config = new TableGeneratedConfig(tableName, templatePath);
			config.setLocalPackage(localPackage);
			config.outfilePathFunc(c->getJavaSrcOutfilePathByType(config, "/"+localPackage, templatePath));
			tableGeneratedConfig.add(config);
			return this;
		}
		
		public DbTableGenerator entityTemplate(String templatePath){
			return entityTemplate("entity", templatePath);
		}

		public DbTableGenerator entityTemplate(String entitySubPackage, String templatePath){
			return entityTemplate(entitySubPackage, templatePath, null);
		}
		public DbTableGenerator entityTemplate(String entitySubPackage, String templatePath, String fileNamePostfix){
			TableGeneratedConfig config = new TableGeneratedConfig(tableName, templatePath);
			config.setLocalPackage(entitySubPackage);
			config.outfilePathFunc(c->{
				return getJavaSrcOutfilePathByType(config, "/"+entitySubPackage, templatePath, fileNamePostfix);
			});
			tableGeneratedConfig.add(config);
			return this;
		}
		

		private String getJavaSrcOutfilePathByType(TableGeneratedConfig c, String typePath, String templatePath){
			return getJavaSrcOutfilePathByType(c, typePath, templatePath, null);
		}
		
		private String getJavaSrcOutfilePathByType(TableGeneratedConfig c, String typePath, String templatePath, String fileNamePostfix){
			String tableShortName = c.tableNameStripStart(c.globalGeneratedConfig().getStripTablePrefix());
			String filePath = c.globalGeneratedConfig().getFullModulePackagePath() + 
								typePath+ "/" + 
								StringUtils.toClassName(tableShortName) + 
								//Entity Service Dao
								(fileNamePostfix==null?FileUtils.getFileNameWithoutExt(templatePath):fileNamePostfix);
			return filePath;
		}
		
		public DbGenerator end(){
			return DbGenerator.this;
		}
		
		public String getTableName() {
			return tableName;
		}

		private GeneratedResult<File> generate(DatabaseMetaDialet dialet, TemplateEngine ftlGenerator, Map<String, Object> outContext){
			GeneratedContext genContext = new GeneratedContext();
			genContext.putAll(globalConfig.getRootContext());
			genContext.putAll(tableContext);
			
			TableMeta tableMeta = dialet.getTableMeta(tableName);
			tableMeta.setStripPrefix(globalConfig.getStripTablePrefix());
			genContext.setTable(tableMeta);
			
			List<File> files = Lists.newArrayList();
			tableGeneratedConfig.stream().forEach(config->{
				Assert.hasText(config.templatePath);
				
//				Assert.hasText(config.outfilePath);
				genContext.setConfig(config);
				OutfilePathFunc outFileNameFunc = config.outfilePathFunc==null?globalConfig.getOutFileNameFunc():config.outfilePathFunc;
				Assert.notNull(outFileNameFunc, "outFileNameFunc can not be null");
				
				/*
				 * Map<String, Object> tableContext =
				 * globalConfig.getTableContextCreator().createContexts(config);
				 * if(tableContext!=null){ genContext.setTableContext(tableContext); }
				 */
				String outfilePath = outFileNameFunc.getOutFileName(genContext);
				if(outContext!=null) {
					genContext.putAll(outContext);
				}
				genContext.initBasicContext();
				
				File file = null;
				if (globalConfig.isOverrideExistFile()) {
					file = ftlGenerator.generateFile(genContext, config.templatePath, outfilePath);
				} else {
					if (!new File(outfilePath).exists()) {
						file = ftlGenerator.generateFile(genContext, config.templatePath, outfilePath);
					} else {
						log.info("file[{}] is exist, ignore genenrated!", outfilePath);
					}
				}
				
				if (file!=null) {
					files.add(file);
				}
			});
			return new GeneratedResult<File>(tableName, files);
		}

		
		public class TableMetaConfig {
			final private TableMeta tableMeta;
			final private DbTableGenerator tableGenerator;

			public TableMetaConfig(TableMeta tableMeta, DbTableGenerator tableGenerator) {
				super();
				this.tableMeta = tableMeta;
				this.tableGenerator = tableGenerator;
			}
			public TableMeta tableMeta() {
				return tableMeta;
			}
			public ColumnMetaConfig column(String name){
				ColumnMeta column = this.tableMeta.getColumn(name);
				return new ColumnMetaConfig(column);
			}
			public DbTableGenerator end() {
				return tableGenerator;
			}
			
			public class ColumnMetaConfig {
				private final ColumnMeta column;

				public ColumnMetaConfig(ColumnMeta column) {
					super();
					this.column = column;
				}
				public ColumnMetaConfig javaType(Class<?> javaType){
					this.column.getMapping().javaType(javaType);
					return this;
				}
				public TableMetaConfig end() {
					return TableMetaConfig.this;
				}
			}
		}
		

		public class TableGeneratedConfig {
			private String tableName;
			private String templatePath;
			private OutfilePathFunc outfilePathFunc;
			private String localPackage;
			
			public TableGeneratedConfig(String tableName, String templatePath) {
				super();
				this.tableName = tableName;
				this.templatePath = templatePath;
			}

			public TableGeneratedConfig(String tableName, String templatePath, String outfilePath) {
				super();
				this.tableName = tableName;
				this.templatePath = templatePath;
				this.outfilePathFunc = config->outfilePath;
			}

			public TableGeneratedConfig outfilePath(String outfilePath) {
				this.outfilePathFunc = config->outfilePath;
				return this;
			}

			public TableGeneratedConfig outfilePathFunc(OutfilePathFunc outFileNameFunc) {
				this.outfilePathFunc = outFileNameFunc;
				return this;
			}
			
			public String getLocalPackage() {
				return localPackage;
			}

			public void setLocalPackage(String localPackage) {
				this.localPackage = localPackage;
			}

			public DbTableGenerator end() {
				return DbTableGenerator.this;
			}

			public String getTemplatePath() {
				return templatePath;
			}

			public OutfilePathFunc getOutFileNameFunc() {
				return outfilePathFunc;
			}

			public String getTableName() {
				return tableName;
			}

			public String getTableNameWithoutPrefix() {
				return tableNameStripStart(globalConfig.getStripTablePrefix());
			}

			public String getClassName() {
				return StringUtils.toClassName(getTableNameWithoutPrefix());
			}

			public String getPropertyName() {
				return StringUtils.toPropertyName(getTableNameWithoutPrefix());
			}

			public String tableNameStripStart(String stripChars) {
				if(StringUtils.isBlank(stripChars))
					return tableName;
//				return org.apache.commons.lang3.StringUtils.stripStart(tableName.toLowerCase(), stripChars.toLowerCase());
				if (tableName.startsWith(stripChars)) {
					return tableName.toLowerCase().substring(stripChars.length());
				} else {
					return tableName;
				}
			}
			
			public GlobalConfig globalGeneratedConfig(){
				return globalConfig;
			}
			
		}
	}
	
	
	
}
