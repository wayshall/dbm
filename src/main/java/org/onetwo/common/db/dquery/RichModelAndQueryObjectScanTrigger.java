package org.onetwo.common.db.dquery;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Stream;

import org.onetwo.common.db.dquery.repostory.AnnotationScanBasicDynamicQueryObjectRegister;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.spring.utils.ResourcesScanner;
import org.onetwo.common.spring.utils.ScanResourcesCallback;
import org.onetwo.dbm.mapping.ScanedClassContext;
import org.onetwo.dbm.richmodel.PackageScanedProcessor;
import org.onetwo.dbm.richmodel.RichModel;
import org.onetwo.dbm.richmodel.RichModelCheckProcessor;
import org.onetwo.dbm.richmodel.RichModelEnhanceProcessor;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;

/***
 * triggee richmodel and query object that basic @DbmRepository to init.
 * query object that basic file scan is trigger by dbmdao created 
 * @author way
 *
 */
public class RichModelAndQueryObjectScanTrigger /*implements BeanFactoryPostProcessor*/ {
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());

	private final ResourcesScanner scanner = ResourcesScanner.CLASS_CANNER;
	private BeanDefinitionRegistry registry;
//	private ApplicationContext applicationContext;
	private String[] packagesToScan;
	
	private boolean enableRichModel = true;
	private AnnotationScanBasicDynamicQueryObjectRegister register;
	private AnnotationAttributes enableDbmRepositoryAttributes;
	private String useEnableDbmRepositoryClassName;
	
//	private Class<? extends QueryProvideManager> defaultQueryProvideManagerClass;
	
	public RichModelAndQueryObjectScanTrigger(BeanDefinitionRegistry registry) {
		this.registry = registry;
		this.register = new AnnotationScanBasicDynamicQueryObjectRegister(this.registry);
	}

	public RichModelAndQueryObjectScanTrigger(ApplicationContext applicationContext) {
		this(SpringUtils.getBeanDefinitionRegistry(applicationContext));
	}

	public void setRegisterDefaultQueryProvideManager(boolean registerDefaultQueryProvideManager) {
		this.register.setRegisterDefaultQueryProvideManager(registerDefaultQueryProvideManager);
	}
	
	public void setDefaultQueryProvideManagerClass(Class<? extends QueryProvideManager> defaultQueryProvideManagerClass) {
		register.setDefaultQueryProvideManagerClass(defaultQueryProvideManagerClass);
	}

	public void setPackagesToScan(String[] packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

	/*@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.scanAndRegisterBeans(beanFactory);
	}*/
	
	public void setEnableDbmRepositoryAttributes(
			AnnotationAttributes enableDbmRepositoryAttributes) {
		this.enableDbmRepositoryAttributes = enableDbmRepositoryAttributes;
	}

	public void setEnableRichModel(boolean enableRichModel) {
		this.enableRichModel = enableRichModel;
	}

	public void scanAndRegisterBeans(ListableBeanFactory beanFactory) throws BeansException {
//		BeanDefinitionRegistry registry = SpringUtils.getBeanDefinitionRegistry(beanFactory);
//		Collection<String> packs = DbmUtils.getAllDbmPackageNames(beanFactory);
		Collection<String> packs = new HashSet<String>();
		
		if(this.packagesToScan!=null){
			Collections.addAll(packs, this.packagesToScan);
		}

		/*SpringUtils.scanAnnotation(beanFactory, EnableDbm.class, (beanDef, beanClass)->{
			if(logger.isInfoEnabled()){
				logger.info("found EnableDbm class: {}", beanClass);
			}
			EnableDbm enableDbm = beanClass.getAnnotation(EnableDbm.class);
			if(enableDbm!=null){
				enableRichModel = enableDbm.enableRichModel();
			}
		});*/
		
		/*SpringUtils.scanAnnotation(beanFactory, EnableDbmRepository.class, (beanDef, beanClass)->{
			if(logger.isInfoEnabled()){
				logger.info("found EnableDbmRepository class: {}", beanClass);
			}
			EnableDbmRepository enableDbmRepository = beanClass.getAnnotation(EnableDbmRepository.class);
			if(enableDbmRepository!=null){
				register.setDefaultQueryProvideManagerClass(enableDbmRepository.defaultQueryProviderClass());
				register.setRegisterDefaultQueryProvideManager(enableDbmRepository.autoRegister());
				Stream.of(enableDbmRepository.value()).forEach(p->packs.add(p));
				Stream.of(enableDbmRepository.basePackageClasses()).forEach(p->{
					packs.add(p.getPackage().getName());
				});
			}
		});*/
		if(enableDbmRepositoryAttributes!=null){
			register.setDefaultQueryProvideManagerClass(enableDbmRepositoryAttributes.getClass("defaultQueryProviderClass"));
			register.setRegisterDefaultQueryProvideManager(enableDbmRepositoryAttributes.getBoolean("autoRegister"));
			/*Stream.of(enableDbmRepositoryAttributes.getStringArray("value")).forEach(p->packs.add(p));
			Stream.of(enableDbmRepositoryAttributes.getClassArray("basePackageClasses")).forEach(p->{
				packs.add(p.getPackage().getName());
			});*/
			boolean isSpecifyPackages = false;
			for(String p : enableDbmRepositoryAttributes.getStringArray("value")){
				packs.add(p);
				isSpecifyPackages = true;
			}
			for(Class<?> p : enableDbmRepositoryAttributes.getClassArray("basePackageClasses")){
				packs.add(p.getPackage().getName());
				isSpecifyPackages = true;
			}
			if(!isSpecifyPackages && useEnableDbmRepositoryClassName!=null){
				packs.add(ClassUtils.getPackageName(useEnableDbmRepositoryClassName));
			}
		}

		if(!packs.isEmpty()){
			if(enableRichModel){
				this.enhanceRichModel(packs.toArray(new String[0]));
			}
			register.setPackagesToScan(packs.toArray(new String[0]));
			register.registerQueryBeans();
		}
	}
	

	protected void enhanceRichModel(String...packs){
		PackageScanedProcessor processor = null;
		Collection<ScanedClassContext> richModels = this.scanRichModelClasses(packs);
		if(ClassUtils.isPresent("javassist.ClassPool", null)){
			processor = new RichModelEnhanceProcessor();
		}else{
			processor = new RichModelCheckProcessor();
		}
		processor.processClasses(richModels);
	}
	protected Collection<ScanedClassContext> scanRichModelClasses(String...packs){
		Collection<ScanedClassContext> entryClassNameList = scanner.scan(new ScanResourcesCallback<ScanedClassContext>() {

			@Override
			public ScanedClassContext doWithCandidate(MetadataReader metadataReader, Resource resource, int count) {
				ScanedClassContext cls = new ScanedClassContext(metadataReader);
				//暂时忽略非richmodel
				if(cls.isSubClassOf(RichModel.class.getName())){
					return cls;
				}
				return null;
			}

		}, packs);
		return entryClassNameList;
	}

	public void setUseEnableDbmRepositoryClassName(String useEnableDbmRepositoryClassName) {
		this.useEnableDbmRepositoryClassName = useEnableDbmRepositoryClassName;
	}
	
}
