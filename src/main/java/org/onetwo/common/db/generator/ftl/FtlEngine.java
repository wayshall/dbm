package org.onetwo.common.db.generator.ftl;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import jakarta.annotation.Resource;

import org.onetwo.common.db.generator.TemplateEngine;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.file.FileUtils;
import org.onetwo.common.spring.ftl.DynamicFreemarkerTemplateConfigurer;
import org.onetwo.common.spring.ftl.StringTemplateProvider;
import org.springframework.beans.factory.InitializingBean;

import freemarker.template.Template;

public class FtlEngine extends DynamicFreemarkerTemplateConfigurer implements InitializingBean, TemplateEngine {
	
	public FtlEngine(){
	}

	@Override
	public void afterPropertiesSet() {
		if(isInitialized())
			return ;
		this.setTemplateProvider(new FileTemplateProvider());
		this.initialize();
	}
	

	/* (non-Javadoc)
	 * @see org.onetwo.common.db.generator.ftl.TemplateGenerator#generateString(org.onetwo.common.db.generator.GenerateContext, java.lang.String)
	 */
	@Override
	public String generateString(Object context, String templatePath){
		if(context==null){
			throw new BaseException("context can not be null, igonre generated file.");
		}
		StringWriter sw = new StringWriter();
		try {
			Template template = getConfiguration().getTemplate(templatePath);
			template.process(context, sw);
		}catch(Exception e){
			throw new BaseException("generate file error : " + templatePath, e);
		} 
		return sw.toString();
	}

	/* (non-Javadoc)
	 * @see org.onetwo.common.db.generator.ftl.TemplateGenerator#generateFile(org.onetwo.common.db.generator.GenerateContext, java.lang.String, java.lang.String)
	 */
	@Override
	public File generateFile(Object context, String templatePath, String targetPath){
		if(context==null){
			throw new BaseException("context can not be null, igonre generated file.");
		}
		FileWriter out = null;
		File outfile = new File(targetPath);
		try {
			
			Template template = getConfiguration().getTemplate(templatePath);
			FileUtils.makeDirs(targetPath);

//			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), this.getEncoding()));
			out = new FileWriter(outfile);
			template.process(context, out);
			System.out.println("file is generate : " + targetPath);
			
		}catch(Exception e){
			throw new BaseException("generate file error : " + targetPath, e);
		} finally{
			FileUtils.close(out);
		}
		return outfile;
	}
	
	@Resource
	public void setTemplateProvider(StringTemplateProvider templateProvider) {
		super.setTemplateProvider(templateProvider);
	}
}
