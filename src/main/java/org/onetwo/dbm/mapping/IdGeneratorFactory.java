package org.onetwo.dbm.mapping;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;

import javax.persistence.SequenceGenerator;
import javax.persistence.TableGenerator;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.annotation.AnnotationInfo;
import org.onetwo.common.jackson.JsonMapper;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.spring.Springs;
import org.onetwo.dbm.annotation.DbmIdGenerator;
import org.onetwo.dbm.id.CustomIdGenerator;
import org.onetwo.dbm.id.CustomerIdGeneratorAdapter;
import org.onetwo.dbm.id.IdentifierGenerator;
import org.onetwo.dbm.id.SequenceGeneratorAttrs;
import org.onetwo.dbm.id.SequenceIdGenerator;
import org.onetwo.dbm.id.TableGeneratorAttrs;
import org.onetwo.dbm.id.TableIdGenerator;
import org.slf4j.Logger;

/**
 * @author wayshall
 * <br/>
 */
public class IdGeneratorFactory {
	private static final Logger logger = JFishLoggerFactory.getLogger(IdGeneratorFactory.class);
	
	public static Optional<IdentifierGenerator<Long>> createSequenceGenerator(AnnotationInfo annotationInfo){
		SequenceGenerator sg = annotationInfo.getAnnotation(SequenceGenerator.class);
		if(sg==null){
			return Optional.empty();
		}
		SequenceGeneratorAttrs sgAttrs = new SequenceGeneratorAttrs(sg.name(), sg.sequenceName(), sg.initialValue(), sg.allocationSize());
		SequenceIdGenerator generator = new SequenceIdGenerator(sgAttrs);
		return Optional.of(generator);
	}
	
	public static Optional<IdentifierGenerator<Long>> createTableGenerator(AnnotationInfo annotationInfo){
		TableGenerator tg = annotationInfo.getAnnotation(TableGenerator.class);
		if(tg==null){
			return Optional.empty();
		}
		TableGeneratorAttrs tgAttrs = new TableGeneratorAttrs(tg.name(), 
																tg.allocationSize(), 
																tg.table(), 
																tg.pkColumnName(), 
																tg.valueColumnName(), 
																tg.pkColumnValue(),
																tg.initialValue());
		TableIdGenerator generator = new TableIdGenerator(tgAttrs);
		return Optional.of(generator);
	}
	
	@SuppressWarnings("unchecked")
	public static Optional<IdentifierGenerator<? extends Serializable>> createDbmIdGenerator(AnnotationInfo annotationInfo){
		DbmIdGenerator dg = annotationInfo.getAnnotation(DbmIdGenerator.class);
		if(dg==null){
			return Optional.empty();
		}
		CustomIdGenerator<? extends Serializable> customIdGenerator = null;
		if(Springs.getInstance().isActive()){
			customIdGenerator = Springs.getInstance().getBean(dg.generatorClass());
		}else{
			if(logger.isWarnEnabled()){
				logger.warn("spring application is not active: {}", Springs.getInstance().getAppContext());
			}
		}
		if(customIdGenerator==null){
			customIdGenerator = ReflectUtils.newInstance(dg.generatorClass());
		}
		String attribute = dg.attributes();
		if(StringUtils.isNotBlank(attribute)){
			HashMap<String, Object> jsonMap = JsonMapper.IGNORE_EMPTY.fromJson(attribute, HashMap.class);
			SpringUtils.getMapToBean().injectBeanProperties(jsonMap, customIdGenerator);
		}
		customIdGenerator.initGenerator();
		IdentifierGenerator<? extends Serializable> idGenerator = new CustomerIdGeneratorAdapter<>(dg.name(), customIdGenerator);
		return Optional.of(idGenerator);
	}

}
